package com.sismics.music.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaPlayer.*
import android.media.RemoteControlClient
import android.net.Uri
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.WifiLock
import android.os.Handler
import android.os.IBinder
import android.os.PowerManager
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.widget.Toast
import com.androidquery.AQuery
import com.loopj.android.http.FileAsyncHttpResponseHandler
import com.loopj.android.http.RequestHandle
import com.sismics.music.R
import com.sismics.music.activity.MainActivity
import com.sismics.music.event.MediaPlayerSeekEvent
import com.sismics.music.event.MediaPlayerStateChangedEvent
import com.sismics.music.event.TrackCacheStatusChangedEvent
import com.sismics.music.model.PlaylistTrack
import com.sismics.music.resource.TrackResource
import com.sismics.music.util.CacheUtil
import com.sismics.music.util.PreferenceUtil
import com.sismics.music.util.ScrobbleUtil
import de.greenrobot.event.EventBus
import org.apache.http.Header
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Music service to download and play the playlist.
 */
class MusicService : Service(), OnCompletionListener, OnPreparedListener, OnErrorListener, MusicFocusable {

    // Our media player
    internal var mPlayer: MediaPlayer? = null

    // Date when the song was started
    internal var songStartedAt: Long = 0

    internal var songCompleted = false

    // Our AudioFocusHelper object, always available since we target API 14
    internal var mAudioFocusHelper: AudioFocusHelper? = null

    // indicates the state our service:
    enum class State {
        Stopped, // media player is stopped and not prepared to play
        Preparing, // media player is preparing...
        Playing, // playback active (media player ready!). (but the media player may actually be
        // paused in this state if we don't have audio focus. But we stay in this state
        // so that we know we have to resume playback once we get focus back)
        Paused      // playback paused (media player ready!)
    }

    internal var mState = State.Stopped

    // do we have audio focus?
    internal enum class AudioFocus {
        NoFocusNoDuck, // we don't have audio focus, and can't duck
        NoFocusCanDuck, // we don't have focus, but can play at a low volume ("ducking")
        Focused           // we have full audio focus
    }

    internal var mAudioFocus = AudioFocus.NoFocusNoDuck

    // Track currently played
    internal var currentPlaylistTrack: PlaylistTrack? = null

    // Track currently downloaded
    internal var downloadingPlaylistTrack: PlaylistTrack? = null

    // Wifi lock that we hold when streaming files from the internet, in order to prevent the
    // device from shutting off the Wifi radio
    internal var mWifiLock: WifiLock? = null

    // The ID we use for the notification (the onscreen alert that appears at the notification
    // area at the top of the screen as an icon -- and as text as well if the user expands the
    // notification area).
    internal val NOTIFICATION_ID = 1

    // our RemoteControlClient object, which will use remote control APIs available in
    // SDK level >= 14, if they're available.
    internal var mRemoteControlClient: RemoteControlClient? = null

    // The component name of MusicIntentReceiver, for use with media button and remote control APIs
    internal var mMediaButtonReceiverComponent: ComponentName? = null

    internal var mAudioManager: AudioManager? = null
    internal var mNotificationManager: NotificationManager? = null

    // Request handle of the current download
    internal var bufferRequestHandle: RequestHandle? = null

    // Handler to post media player state changes regulary
    internal var mediaPlayerHandler: Handler? = Handler()

    /**
     * Makes sure the media player exists and has been reset. This will create the media player
     * if needed, or reset the existing media player if one already exists.
     */
    internal fun createMediaPlayerIfNeeded() {
        if (mPlayer == null) {
            mPlayer = MediaPlayer()

            // Make sure the media player will acquire a wake-lock while playing. If we don't do
            // that, the CPU might go to sleep while the song is playing, causing playback to stop.
            //
            // Remember that to use this, we have to declare the android.permission.WAKE_LOCK
            // permission in AndroidManifest.xml.
            mPlayer!!.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)

            // we want the media player to notify us when it's ready preparing, and when it's done
            // playing:
            mPlayer!!.setOnPreparedListener(this)
            mPlayer!!.setOnCompletionListener(this)
            mPlayer!!.setOnErrorListener(this)
        } else
            mPlayer!!.reset()
    }

    override fun onCreate() {
        Log.i(TAG, "debug: Creating service")

        // Create the Wifi lock (this does not acquire the lock, this just creates it)
        mWifiLock = (getSystemService(Context.WIFI_SERVICE) as WifiManager).createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock")

        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // create the Audio Focus Helper, if the Audio Focus feature is available (SDK 8 or above)
        mAudioFocusHelper = AudioFocusHelper(applicationContext, this)

        mMediaButtonReceiverComponent = ComponentName(this, MusicIntentReceiver::class.java)

        // Grab media player states regulary
        mediaPlayerHandler!!.postDelayed(object : Runnable {
            override fun run() {
                if (mPlayer != null && mState == State.Playing) {
                    EventBus.getDefault().post(
                            MediaPlayerStateChangedEvent(mState, songStartedAt, currentPlaylistTrack,
                                    mPlayer!!.currentPosition, mPlayer!!.duration))
                } else {
                    EventBus.getDefault().post(
                            MediaPlayerStateChangedEvent(mState, -1, currentPlaylistTrack, -1, -1))
                }

                if (mediaPlayerHandler != null) {
                    mediaPlayerHandler!!.postDelayed(this, 1000)
                }
            }
        }, 1000)

        EventBus.getDefault().register(this)
    }

    /**
     * Called when we receive an Intent. When we receive an intent sent to us via startService(),
     * this is the method that gets called. So here we react appropriately depending on the
     * Intent's action, which specifies what is being requested of us.
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val action = intent.action

        if (action != null) {
            when (action) {
                ACTION_TOGGLE_PLAYBACK -> processTogglePlaybackRequest()
                ACTION_PLAY -> {
                    val force = intent.getBooleanExtra(EXTRA_FORCE, false)
                    processPlayRequest(force)
                }
                ACTION_PAUSE -> processPauseRequest()
                ACTION_SKIP -> processSkipRequest()
                ACTION_REWIND -> processRewindRequest()
                ACTION_STOP -> processStopRequest()
            }
        }

        // Means we started the service, but don't want it to restart in case it's killed
        return Service.START_NOT_STICKY
    }

    /**
     * Toggle playback request.
     */
    internal fun processTogglePlaybackRequest() {
        if (mState == State.Paused || mState == State.Stopped) {
            processPlayRequest(false)
        } else {
            processPauseRequest()
        }
    }

    /**
     * Play request.
     * @param force If true force the playing
     */
    internal fun processPlayRequest(force: Boolean) {
        tryToGetAudioFocus()

        if (mState == State.Stopped || force) {
            // If we're stopped, just go ahead to the next song and start playing
            playNextSong()
        } else if (mState == State.Paused) {
            // If we're paused, just continue playback and restore the 'foreground service' state.
            mState = State.Playing
            startForeground(NOTIFICATION_ID, notification)
            configAndStartMediaPlayer()
        }

        // Tell any remote controls that our playback state is 'playing'.
        if (mRemoteControlClient != null) {
            mRemoteControlClient!!.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING)
        }
    }

    /**
     * Pause request.
     */
    internal fun processPauseRequest() {
        if (mState == State.Playing) {
            // Pause media player and cancel the 'foreground service' state.
            mState = State.Paused
            mPlayer!!.pause()
            relaxResources(false) // Wwhile paused, we always retain the MediaPlayer

            // Update the notification
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)
        }

        // Tell any remote controls that our playback state is 'paused'.
        if (mRemoteControlClient != null) {
            mRemoteControlClient!!.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED)
        }
    }

    /**
     * Rewind request.
     */
    internal fun processRewindRequest() {
        if (mState == State.Playing || mState == State.Paused) {
            mPlayer!!.seekTo(0)
        }
    }

    /**
     * Skip request.
     */
    internal fun processSkipRequest() {
        if (mState == State.Playing || mState == State.Paused) {
            tryToGetAudioFocus()
            playNextSong()
        }
    }

    /**
     * Stop request.
     */
    internal fun processStopRequest() {
        mState = State.Stopped

        // Stop the playlist
        PlaylistService.stop()

        // let go of all resources...
        relaxResources(true)
        giveUpAudioFocus()

        // Tell any remote controls that our playback state is 'paused'.
        if (mRemoteControlClient != null) {
            mRemoteControlClient!!.setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED)
        }

        // service is no longer necessary. Will be started again if needed.
        stopSelf()
    }

    /**
     * Releases resources used by the service for playback. This includes the "foreground service"
     * status and notification, the wake locks and possibly the MediaPlayer.

     * @param releaseMediaPlayer Indicates whether the Media Player should also be released or not
     */
    internal fun relaxResources(releaseMediaPlayer: Boolean) {
        // Stop being a foreground service
        stopForeground(false)

        // Stop and release the Media Player, if it's available
        if (releaseMediaPlayer && mPlayer != null) {
            mPlayer!!.reset()
            mPlayer!!.release()
            mPlayer = null
        }

        if (bufferRequestHandle != null) {
            // We are buffering something else, cancel it
            Log.d("SismicsMusic", "Cancelling a previous download")
            bufferRequestHandle!!.cancel(true)
            downloadingPlaylistTrack!!.cacheStatus = PlaylistTrack.CacheStatus.NONE
            EventBus.getDefault().post(TrackCacheStatusChangedEvent(downloadingPlaylistTrack))
            bufferRequestHandle = null
            downloadingPlaylistTrack = null
        }

        // We can also release the Wifi lock, if we're holding it
        if (mWifiLock!!.isHeld) {
            mWifiLock?.release()
        }
    }

    internal fun giveUpAudioFocus() {
        if (mAudioFocus == AudioFocus.Focused && mAudioFocusHelper != null
                && mAudioFocusHelper!!.abandonFocus())
            mAudioFocus = AudioFocus.NoFocusNoDuck
    }

    /**
     * Reconfigures MediaPlayer according to audio focus settings and starts/restarts it. This
     * method starts/restarts the MediaPlayer respecting the current audio focus state. So if
     * we have focus, it will play normally; if we don't have focus, it will either leave the
     * MediaPlayer paused or set it to a low volume, depending on what is allowed by the
     * current focus settings. This method assumes mPlayer != null, so if you are calling it,
     * you have to do so from a context where you are sure this is the case.
     */
    internal fun configAndStartMediaPlayer() {
        if (mAudioFocus == AudioFocus.NoFocusNoDuck) {
            // If we don't have audio focus and can't duck, we have to pause, even if mState
            // is State.Playing. But we stay in the Playing state so that we know we have to resume
            // playback once we get the focus back.
            if (mPlayer!!.isPlaying) mPlayer!!.pause()
            return
        } else if (mAudioFocus == AudioFocus.NoFocusCanDuck)
            mPlayer!!.setVolume(DUCK_VOLUME, DUCK_VOLUME)  // we'll be relatively quiet
        else
            mPlayer!!.setVolume(1.0f, 1.0f) // we can be loud

        if (!mPlayer!!.isPlaying) mPlayer!!.start()
    }

    internal fun tryToGetAudioFocus() {
        if (mAudioFocus != AudioFocus.Focused && mAudioFocusHelper != null
                && mAudioFocusHelper!!.requestFocus())
            mAudioFocus = AudioFocus.Focused
    }

    /**
     * Starts playing the next song.
     */
    internal fun playNextSong() {
        mState = State.Stopped
        relaxResources(false) // release everything except MediaPlayer

        val nextPlaylistTrack = PlaylistService.next(true) ?: return

        // set the source of the media player to a manual URL or path
        downloadTrack(nextPlaylistTrack, true)
    }

    /**
     * Download a playlistTrack.
     * @param playlistTrack PlaylistTrack to download
     * *
     * @param play If true, play it
     */
    internal fun downloadTrack(playlistTrack: PlaylistTrack, play: Boolean) {
        Log.d("SismicsMusic", "Start downloading " + playlistTrack.title)
        if (bufferRequestHandle != null) {
            // We are buffering something else, cancel it
            Log.d("SismicsMusic", "Cancelling a previous download")
            bufferRequestHandle!!.cancel(true)
            downloadingPlaylistTrack!!.cacheStatus = PlaylistTrack.CacheStatus.NONE
            EventBus.getDefault().post(TrackCacheStatusChangedEvent(downloadingPlaylistTrack))
            bufferRequestHandle = null
            downloadingPlaylistTrack = null
        }

        val incompleteCacheFile = CacheUtil.getIncompleteCacheFile(playlistTrack)

        val responseHandler = object : FileAsyncHttpResponseHandler(incompleteCacheFile) {
            override fun onStart() {
                playlistTrack.cacheStatus = PlaylistTrack.CacheStatus.DOWNLOADING
                EventBus.getDefault().post(TrackCacheStatusChangedEvent(playlistTrack))
            }

            override fun onSuccess(statusCode: Int, headers: Array<Header>?, file: File) {
                if (CacheUtil.setComplete(file)) {
                    playlistTrack.cacheStatus = PlaylistTrack.CacheStatus.COMPLETE
                    EventBus.getDefault().post(TrackCacheStatusChangedEvent(playlistTrack))
                    if (play) {
                        doPlay(playlistTrack)
                    }
                }
            }

            override fun onFinish() {
                // Request is finished (and not cancelled), let's buffer the next song without playing it
                bufferRequestHandle = null
                downloadingPlaylistTrack = null
                val nextPlaylistTrack = PlaylistService.after(playlistTrack)
                if (nextPlaylistTrack != null) {
                    Log.d("SismicsMusic", "Downloading the next playlistTrack " + nextPlaylistTrack.title)
                    downloadTrack(nextPlaylistTrack, false)
                }
            }
        }

        if (CacheUtil.isComplete(playlistTrack)) {
            Log.d("SismicsMusic", "This playlistTrack is already complete, output: " + play)

            // Nothing to buffer, the playlistTrack is already complete in the cache
            if (play) {
                doPlay(playlistTrack)
            }

            responseHandler.onFinish()
            return
        }

        bufferRequestHandle = TrackResource.download(this, playlistTrack.id, responseHandler)
        downloadingPlaylistTrack = playlistTrack
    }

    /**
     * Play a downloaded playlistTrack.
     * @param playlistTrack PlaylistTrack to play
     */
    internal fun doPlay(playlistTrack: PlaylistTrack) {
        try {
            createMediaPlayerIfNeeded()
            songStartedAt = Date().time
            songCompleted = false
            mPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
            val file = CacheUtil.getCompleteCacheFile(playlistTrack)
            mPlayer!!.setDataSource(this, Uri.fromFile(file))

            currentPlaylistTrack = playlistTrack

            mState = State.Preparing
            startForeground(NOTIFICATION_ID, notification)

            // Use the media button APIs (if available) to register ourselves for media button
            // events
            mAudioManager?.registerMediaButtonEventReceiver(mMediaButtonReceiverComponent)

            // Use the remote control APIs (if available) to set the playback state
            if (mRemoteControlClient == null) {
                val intent = Intent(Intent.ACTION_MEDIA_BUTTON)
                intent.component = mMediaButtonReceiverComponent
                mRemoteControlClient = RemoteControlClient(
                        PendingIntent.getBroadcast(this /*context*/,
                                0 /*requestCode, ignored*/, intent /*intent*/, 0 /*flags*/))
                mAudioManager?.registerRemoteControlClient(mRemoteControlClient)
            }

            mRemoteControlClient!!.setPlaybackState(
                    RemoteControlClient.PLAYSTATE_PLAYING)

            mRemoteControlClient!!.setTransportControlFlags(
                    RemoteControlClient.FLAG_KEY_MEDIA_PLAY or
                            RemoteControlClient.FLAG_KEY_MEDIA_PAUSE or
                            RemoteControlClient.FLAG_KEY_MEDIA_NEXT or
                            RemoteControlClient.FLAG_KEY_MEDIA_STOP)

            // Update the remote controls
            mRemoteControlClient!!.editMetadata(true).putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, playlistTrack.artistName).putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, playlistTrack.albumName).putString(MediaMetadataRetriever.METADATA_KEY_TITLE, playlistTrack.title).putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, playlistTrack.length).apply()

            // starts preparing the media player in the background. When it's done, it will call
            // our OnPreparedListener (that is, the onPrepared() method on this class, since we set
            // the listener to 'this').
            //
            // Until the media player is prepared, we *cannot* call start() on it!
            mPlayer!!.prepareAsync()

            // If we are streaming from the internet, we want to hold a Wifi lock, which prevents
            // the Wifi radio from going to sleep while the song is playing.
            mWifiLock?.acquire()
        } catch (e: IOException) {
            Log.e("SismicsMusic", "Error playing song", e)
        }

    }

    /**
     * Called when media player is done playing current song.
     */
    override fun onCompletion(player: MediaPlayer) {
        // The media player finished playing the current song, so we go ahead and start the next.
        playNextSong()
    }

    /**
     * Called when media player is done preparing.
     */
    override fun onPrepared(player: MediaPlayer) {
        // The media player is done preparing. That means we can start playing!
        mState = State.Playing
        configAndStartMediaPlayer()
    }

    /**
     * Return a notification based on the current music playback state.
     * @return Notification
     */
    // Get the cached cover image
    // Build the notification
    // Play/pause actions
    // Next/stop actions
    val notification: Notification
        get() {
            val coverUrl = PreferenceUtil.getServerUrl(this) + "/api/album/" + currentPlaylistTrack!!.albumId + "/albumart/small"
            val coverBitmap = AQuery(this).getCachedImage(coverUrl, 96)
            val builder = NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_notification).setContentTitle(currentPlaylistTrack!!.title).setContentText(currentPlaylistTrack!!.artistName).setSubText(currentPlaylistTrack!!.albumName).setTicker(currentPlaylistTrack!!.title + " - " + currentPlaylistTrack!!.artistName).setLargeIcon(coverBitmap).setOngoing(true).setContentIntent(PendingIntent.getActivity(this, 0,
                    Intent(applicationContext, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT))
            if (mState == State.Paused) {
                builder.addAction(R.drawable.ic_action_play_dark, getString(R.string.play),
                        PendingIntent.getService(this, 0, Intent(MusicService.ACTION_PLAY, null, this, MusicService::class.java),
                                PendingIntent.FLAG_UPDATE_CURRENT))
            } else {
                builder.addAction(R.drawable.ic_action_pause_dark, getString(R.string.pause),
                        PendingIntent.getService(this, 0, Intent(MusicService.ACTION_PAUSE, null, this, MusicService::class.java),
                                PendingIntent.FLAG_UPDATE_CURRENT))
            }
            builder.addAction(R.drawable.ic_action_next_dark, getString(R.string.next),
                    PendingIntent.getService(this, 0, Intent(MusicService.ACTION_SKIP, null, this, MusicService::class.java),
                            PendingIntent.FLAG_UPDATE_CURRENT)).addAction(R.drawable.ic_action_stop_dark, getString(R.string.stop),
                    PendingIntent.getService(this, 0, Intent(MusicService.ACTION_STOP, null, this, MusicService::class.java),
                            PendingIntent.FLAG_UPDATE_CURRENT))

            return builder.build()
        }

    /**
     * Called when there's an error playing media. When this happens, the media player goes to
     * the Error state. We warn the user about the error and reset the media player.
     */
    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        Toast.makeText(this, "Media player error! Resetting.", Toast.LENGTH_SHORT).show()
        Log.e(TAG, "Error: what=" + what.toString() + ", extra=" + extra.toString())

        mState = State.Stopped
        relaxResources(true)
        giveUpAudioFocus()
        return true // true indicates we handled the error
    }

    override fun onGainedAudioFocus() {
        Toast.makeText(this, "gained audio focus.", Toast.LENGTH_SHORT).show()
        mAudioFocus = AudioFocus.Focused

        // restart media player with new focus settings
        if (mState == State.Playing)
            configAndStartMediaPlayer()
    }

    override fun onLostAudioFocus(canDuck: Boolean) {
        Toast.makeText(this, "lost audio focus." + if (canDuck)
            "can duck"
        else
            "no duck", Toast.LENGTH_SHORT).show()
        mAudioFocus = if (canDuck) AudioFocus.NoFocusCanDuck else AudioFocus.NoFocusNoDuck

        // start/restart/pause media player with new focus settings
        if (mPlayer != null && mPlayer!!.isPlaying)
            configAndStartMediaPlayer()
    }

    override fun onDestroy() {
        mediaPlayerHandler = null
        EventBus.getDefault().unregister(this)

        // Service is being killed, so make sure we release our resources
        mState = State.Stopped
        relaxResources(true)
        giveUpAudioFocus()
    }

    /**
     * Media player state has changed.
     * @param event Event
     */
    fun onEvent(event: MediaPlayerStateChangedEvent) {
        if (event.playlistTrack == null) {
            return
        }

        if (event.duration < 0) {
            return
        }

        Log.d("MusicService", "Media player is progressing: " + event.currentPosition + "/" + event.duration)
        if (event.currentPosition > event.duration / 2 && !songCompleted) {
            // The song is considered completed
            songCompleted = true
            ScrobbleUtil.trackCompleted(this, event.playlistTrack.id, event.songStartedAt)
        }
    }

    /**
     * Media player seeking.
     * @param event Event
     */
    fun onEvent(event: MediaPlayerSeekEvent) {
        if (mState == State.Playing || mState == State.Paused) {
            mPlayer!!.seekTo(event.position)
        }
    }

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    companion object {

        // The tag we put on debug messages
        internal val TAG = "SismicsMusic"

        // Action intents handled by this service
        val ACTION_TOGGLE_PLAYBACK = "com.sismics.music.action.TOGGLE_PLAYBACK"
        val ACTION_PLAY = "com.sismics.music.action.PLAY"
        val ACTION_PAUSE = "com.sismics.music.action.PAUSE"
        val ACTION_STOP = "com.sismics.music.action.STOP"
        val ACTION_SKIP = "com.sismics.music.action.SKIP"
        val ACTION_REWIND = "com.sismics.music.action.REWIND"

        // Extra to force the playing
        val EXTRA_FORCE = "force"

        // The volume we set the media player to when we lose audio focus, but are allowed to reduce
        // the volume instead of stopping playback.
        val DUCK_VOLUME = 0.1f
    }
}
