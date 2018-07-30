package com.sismics.music.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.RemoteControlClient;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.sismics.music.R;
import com.sismics.music.activity.MainActivity;
import com.sismics.music.db.dao.TrackDao;
import com.sismics.music.event.MediaPlayerSeekEvent;
import com.sismics.music.event.MediaPlayerStateChangedEvent;
import com.sismics.music.event.TrackCacheStatusChangedEvent;
import com.sismics.music.model.ApplicationContext;
import com.sismics.music.model.PlaylistTrack;
import com.sismics.music.resource.TrackResource;
import com.sismics.music.util.CacheUtil;
import com.sismics.music.util.PreferenceUtil;
import com.sismics.music.util.ScrobbleUtil;

import org.apache.http.Header;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Music service to download and play the playlist.
 */
public class MusicService extends Service implements OnCompletionListener, OnPreparedListener,
                OnErrorListener, MusicFocusable {

    // The tag we put on debug messages
    final static String TAG = "SismicsMusic";

    // Action intents handled by this service
    public static final String ACTION_TOGGLE_PLAYBACK = "com.sismics.music.action.TOGGLE_PLAYBACK";
    public static final String ACTION_PLAY = "com.sismics.music.action.PLAY";
    public static final String ACTION_PAUSE = "com.sismics.music.action.PAUSE";
    public static final String ACTION_STOP = "com.sismics.music.action.STOP";
    public static final String ACTION_SKIP = "com.sismics.music.action.SKIP";
    public static final String ACTION_REWIND = "com.sismics.music.action.REWIND";

    // Extra to force the playing
    public static final String EXTRA_FORCE = "force";

    // The volume we set the media player to when we lose audio focus, but are allowed to reduce
    // the volume instead of stopping playback.
    public static final float DUCK_VOLUME = 0.1f;

    // Our media player
    MediaPlayer mPlayer = null;

    // Date when the song was started
    long songStartedAt = 0;

    boolean songCompleted = false;

    // Our AudioFocusHelper object, always available since we target API 14
    AudioFocusHelper mAudioFocusHelper = null;

    // indicates the state our service:
    public enum State {
        Stopped,    // media player is stopped and not prepared to play
        Preparing,  // media player is preparing...
        Playing,    // playback active (media player ready!). (but the media player may actually be
                    // paused in this state if we don't have audio focus. But we stay in this state
                    // so that we know we have to resume playback once we get focus back)
        Paused      // playback paused (media player ready!)
    }

    State mState = State.Stopped;

    // do we have audio focus?
    enum AudioFocus {
        NoFocusNoDuck,    // we don't have audio focus, and can't duck
        NoFocusCanDuck,   // we don't have focus, but can play at a low volume ("ducking")
        Focused           // we have full audio focus
    }
    AudioFocus mAudioFocus = AudioFocus.NoFocusNoDuck;

    // Track currently played
    PlaylistTrack currentPlaylistTrack = null;

    // Track currently downloaded
    PlaylistTrack downloadingPlaylistTrack = null;

    // Wifi lock that we hold when streaming files from the internet, in order to prevent the
    // device from shutting off the Wifi radio
    WifiLock mWifiLock;

    // The ID we use for the notification (the onscreen alert that appears at the notification
    // area at the top of the screen as an icon -- and as text as well if the user expands the
    // notification area).
    final int NOTIFICATION_ID = 1;

    // our RemoteControlClient object, which will use remote control APIs available in
    // SDK level >= 14, if they're available.
    RemoteControlClient mRemoteControlClient;

    // The component name of MusicIntentReceiver, for use with media button and remote control APIs
    ComponentName mMediaButtonReceiverComponent;

    AudioManager mAudioManager;
    NotificationManager mNotificationManager;

    // Request handle of the current download
    RequestHandle bufferRequestHandle;

    // Handler to post media player state changes regulary
    Handler mediaPlayerHandler = new Handler();

    /**
     * Makes sure the media player exists and has been reset. This will create the media player
     * if needed, or reset the existing media player if one already exists.
     */
    void createMediaPlayerIfNeeded() {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();

            // Make sure the media player will acquire a wake-lock while playing. If we don't do
            // that, the CPU might go to sleep while the song is playing, causing playback to stop.
            //
            // Remember that to use this, we have to declare the android.permission.WAKE_LOCK
            // permission in AndroidManifest.xml.
            mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

            // we want the media player to notify us when it's ready preparing, and when it's done
            // playing:
            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnErrorListener(this);
        }
        else
            mPlayer.reset();
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "debug: Creating service");

        // Create the Wifi lock (this does not acquire the lock, this just creates it)
        mWifiLock = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE))
                        .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        // create the Audio Focus Helper, if the Audio Focus feature is available (SDK 8 or above)
        mAudioFocusHelper = new AudioFocusHelper(getApplicationContext(), this);

        mMediaButtonReceiverComponent = new ComponentName(this, MusicIntentReceiver.class);

        // Grab media player states regulary
        mediaPlayerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mPlayer != null && mState == State.Playing) {
                    EventBus.getDefault().post(
                            new MediaPlayerStateChangedEvent(mState, songStartedAt, currentPlaylistTrack,
                                    mPlayer.getCurrentPosition(), mPlayer.getDuration()));
                } else {
                    EventBus.getDefault().post(
                            new MediaPlayerStateChangedEvent(mState, -1, currentPlaylistTrack, -1, -1));
                }

                if (mediaPlayerHandler != null) {
                    mediaPlayerHandler.postDelayed(this, 1000);
                }
            }
        }, 1000);

        EventBus.getDefault().register(this);
    }

    /**
     * Called when we receive an Intent. When we receive an intent sent to us via startService(),
     * this is the method that gets called. So here we react appropriately depending on the
     * Intent's action, which specifies what is being requested of us.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        if (action != null) {
            switch (action) {
                case ACTION_TOGGLE_PLAYBACK:
                    processTogglePlaybackRequest();
                    break;
                case ACTION_PLAY:
                    boolean force = intent.getBooleanExtra(EXTRA_FORCE, false);
                    processPlayRequest(force);
                    break;
                case ACTION_PAUSE:
                    processPauseRequest();
                    break;
                case ACTION_SKIP:
                    processSkipRequest();
                    break;
                case ACTION_REWIND:
                    processRewindRequest();
                    break;
                case ACTION_STOP:
                    processStopRequest();
                    break;
            }
        }

        // Means we started the service, but don't want it to restart in case it's killed
        return START_NOT_STICKY;
    }

    /**
     * Toggle playback request.
     */
    void processTogglePlaybackRequest() {
        if (mState == State.Paused || mState == State.Stopped) {
            processPlayRequest(false);
        } else {
            processPauseRequest();
        }
    }

    /**
     * Play request.
     * @param force If true force the playing
     */
    void processPlayRequest(boolean force) {
        tryToGetAudioFocus();

        if (mState == State.Stopped || force) {
            // If we're stopped, just go ahead to the next song and start playing
            playNextSong();
        } else if (mState == State.Paused) {
            // If we're paused, just continue playback and restore the 'foreground service' state.
            mState = State.Playing;
            startForeground(NOTIFICATION_ID, getNotification());
            configAndStartMediaPlayer();
        }

        // Tell any remote controls that our playback state is 'playing'.
        if (mRemoteControlClient != null) {
            mRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
        }
    }

    /**
     * Pause request.
     */
    void processPauseRequest() {
        if (mState == State.Playing) {
            // Pause media player and cancel the 'foreground service' state.
            mState = State.Paused;
            mPlayer.pause();
            relaxResources(false); // Wwhile paused, we always retain the MediaPlayer

            // Update the notification
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, getNotification());
        }

        // Tell any remote controls that our playback state is 'paused'.
        if (mRemoteControlClient != null) {
            mRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
        }
    }

    /**
     * Rewind request.
     */
    void processRewindRequest() {
        if (mState == State.Playing || mState == State.Paused) {
            mPlayer.seekTo(0);
        }
    }

    /**
     * Skip request.
     */
    void processSkipRequest() {
        if (mState == State.Playing || mState == State.Paused) {
            tryToGetAudioFocus();
            playNextSong();
        }
    }

    /**
     * Stop request.
     */
    void processStopRequest() {
        mState = State.Stopped;

        // Stop the playlist
        ApplicationContext.getInstance().getPlaylistService().stop();

        // let go of all resources...
        relaxResources(true);
        giveUpAudioFocus();

        // Tell any remote controls that our playback state is 'paused'.
        if (mRemoteControlClient != null) {
            mRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
        }

        // service is no longer necessary. Will be started again if needed.
        stopSelf();
    }

    /**
     * Releases resources used by the service for playback. This includes the "foreground service"
     * status and notification, the wake locks and possibly the MediaPlayer.
     *
     * @param releaseMediaPlayer Indicates whether the Media Player should also be released or not
     */
    void relaxResources(boolean releaseMediaPlayer) {
        // Stop being a foreground service
        stopForeground(false);

        // Stop and release the Media Player, if it's available
        if (releaseMediaPlayer && mPlayer != null) {
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }

        if (bufferRequestHandle != null) {
            // We are buffering something else, cancel it
            Log.d("SismicsMusic", "Cancelling a previous download");
            bufferRequestHandle.cancel(true);
            downloadingPlaylistTrack.setCacheStatus(PlaylistTrack.CacheStatus.NONE);
            EventBus.getDefault().post(new TrackCacheStatusChangedEvent(downloadingPlaylistTrack));
            bufferRequestHandle = null;
            downloadingPlaylistTrack = null;
        }

        // We can also release the Wifi lock, if we're holding it
        if (mWifiLock.isHeld()) mWifiLock.release();
    }

    void giveUpAudioFocus() {
        if (mAudioFocus == AudioFocus.Focused && mAudioFocusHelper != null
                                && mAudioFocusHelper.abandonFocus())
            mAudioFocus = AudioFocus.NoFocusNoDuck;
    }

    /**
     * Reconfigures MediaPlayer according to audio focus settings and starts/restarts it. This
     * method starts/restarts the MediaPlayer respecting the current audio focus state. So if
     * we have focus, it will play normally; if we don't have focus, it will either leave the
     * MediaPlayer paused or set it to a low volume, depending on what is allowed by the
     * current focus settings. This method assumes mPlayer != null, so if you are calling it,
     * you have to do so from a context where you are sure this is the case.
     */
    void configAndStartMediaPlayer() {
        if (mAudioFocus == AudioFocus.NoFocusNoDuck) {
            // If we don't have audio focus and can't duck, we have to pause, even if mState
            // is State.Playing. But we stay in the Playing state so that we know we have to resume
            // playback once we get the focus back.
            if (mPlayer.isPlaying()) mPlayer.pause();
            return;
        } else if (mAudioFocus == AudioFocus.NoFocusCanDuck)
            mPlayer.setVolume(DUCK_VOLUME, DUCK_VOLUME);  // we'll be relatively quiet
        else
            mPlayer.setVolume(1.0f, 1.0f); // we can be loud

        if (!mPlayer.isPlaying()) mPlayer.start();
    }

    void tryToGetAudioFocus() {
        if (mAudioFocus != AudioFocus.Focused && mAudioFocusHelper != null
                        && mAudioFocusHelper.requestFocus())
            mAudioFocus = AudioFocus.Focused;
    }

    /**
     * Starts playing the next song.
     */
    void playNextSong() {
        mState = State.Stopped;
        relaxResources(false); // release everything except MediaPlayer

        PlaylistTrack nextPlaylistTrack = ApplicationContext.getInstance().getPlaylistService().next(true);
        if (nextPlaylistTrack == null) {
            return;
        }

        // set the source of the media player to a manual URL or path
        downloadTrack(nextPlaylistTrack, true);
    }

    /**
     * Download a playlistTrack.
     * @param playlistTrack PlaylistTrack to download
     * @param play If true, play it
     */
    void downloadTrack(final PlaylistTrack playlistTrack, final boolean play) {
        Log.d("SismicsMusic", "Start downloading " + playlistTrack.getTrack().getTitle());
        if (bufferRequestHandle != null) {
            // We are buffering something else, cancel it
            Log.d("SismicsMusic", "Cancelling a previous download");
            bufferRequestHandle.cancel(true);
            downloadingPlaylistTrack.setCacheStatus(PlaylistTrack.CacheStatus.NONE);
            EventBus.getDefault().post(new TrackCacheStatusChangedEvent(downloadingPlaylistTrack));
            bufferRequestHandle = null;
            downloadingPlaylistTrack = null;
        }

        final File incompleteCacheFile = CacheUtil.getIncompleteCacheFile(this, playlistTrack);

        FileAsyncHttpResponseHandler responseHandler = new FileAsyncHttpResponseHandler(incompleteCacheFile) {
            private float lastProgress = 0;

            @Override
            public void onStart() {
                playlistTrack.setCacheStatus(PlaylistTrack.CacheStatus.DOWNLOADING);
                EventBus.getDefault().post(new TrackCacheStatusChangedEvent(playlistTrack));
            }

            @Override
            public void onFailure(Throwable e, File response) {
                playlistTrack.setCacheStatus(PlaylistTrack.CacheStatus.FAILURE);
                EventBus.getDefault().post(new TrackCacheStatusChangedEvent(playlistTrack));
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, File file) {
                if (CacheUtil.setComplete(MusicService.this, playlistTrack, file)) {
                    playlistTrack.setCacheStatus(PlaylistTrack.CacheStatus.COMPLETE);
                    EventBus.getDefault().post(new TrackCacheStatusChangedEvent(playlistTrack));
                    if (play) {
                        doPlay(playlistTrack);
                    }
                }
            }

            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                float progress = (float) bytesWritten / (float) totalSize;
                playlistTrack.setProgress(progress);
                if (progress - lastProgress > 0.05f) {
                    EventBus.getDefault().post(new TrackCacheStatusChangedEvent(playlistTrack));
                    lastProgress = progress;
                }
            }

            @Override
            public void onFinish() {
                // Request is finished (and not cancelled), let's buffer the next song without playing it
                bufferRequestHandle = null;
                downloadingPlaylistTrack = null;
                PlaylistTrack nextPlaylistTrack = ApplicationContext.getInstance().getPlaylistService().after(playlistTrack);
                if (nextPlaylistTrack != null) {
                    Log.d("SismicsMusic", "Downloading the next playlistTrack " + nextPlaylistTrack.getTrack().getTitle());
                    downloadTrack(nextPlaylistTrack, false);
                }
            }
        };

        if (TrackDao.hasTrack(this, playlistTrack.getTrack().getId())) {
            Log.d("SismicsMusic", "This playlistTrack is already complete, output: " + play);

            // Nothing to buffer, the playlistTrack is already complete in the cache
            if (play) {
                doPlay(playlistTrack);
            }

            responseHandler.onFinish();
            return;
        }

        bufferRequestHandle = TrackResource.download(this, playlistTrack.getTrack().getId(), responseHandler);
        downloadingPlaylistTrack = playlistTrack;
    }

    /**
     * Play a downloaded playlistTrack.
     * @param playlistTrack PlaylistTrack to play
     */
    void doPlay(PlaylistTrack playlistTrack) {
        try {
            createMediaPlayerIfNeeded();
            songStartedAt = new Date().getTime();
            songCompleted = false;
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            File file = CacheUtil.getCompleteCacheFile(this, playlistTrack);
            mPlayer.setDataSource(this, Uri.fromFile(file));

            currentPlaylistTrack = playlistTrack;

            mState = State.Preparing;
            startForeground(NOTIFICATION_ID, getNotification());

                    // Use the media button APIs (if available) to register ourselves for media button
            // events
            mAudioManager.registerMediaButtonEventReceiver(mMediaButtonReceiverComponent);

            // Use the remote control APIs (if available) to set the playback state
            if (mRemoteControlClient == null) {
                Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                intent.setComponent(mMediaButtonReceiverComponent);
                mRemoteControlClient = new RemoteControlClient(
                        PendingIntent.getBroadcast(this /*context*/,
                                0 /*requestCode, ignored*/, intent /*intent*/, 0 /*flags*/));
                mAudioManager.registerRemoteControlClient(mRemoteControlClient);
            }

            mRemoteControlClient.setPlaybackState(
                    RemoteControlClient.PLAYSTATE_PLAYING);

            mRemoteControlClient.setTransportControlFlags(
                    RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
                            RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
                            RemoteControlClient.FLAG_KEY_MEDIA_NEXT |
                            RemoteControlClient.FLAG_KEY_MEDIA_STOP);

            // Update the remote controls
            mRemoteControlClient.editMetadata(true)
                    .putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, playlistTrack.getArtist().getName())
                    .putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, playlistTrack.getAlbum().getName())
                    .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, playlistTrack.getTrack().getTitle())
                    .putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, playlistTrack.getTrack().getLength())
                    .apply();

            // starts preparing the media player in the background. When it's done, it will call
            // our OnPreparedListener (that is, the onPrepared() method on this class, since we set
            // the listener to 'this').
            //
            // Until the media player is prepared, we *cannot* call start() on it!
            mPlayer.prepareAsync();

            // If we are streaming from the internet, we want to hold a Wifi lock, which prevents
            // the Wifi radio from going to sleep while the song is playing.
            mWifiLock.acquire();
        } catch (IOException e) {
            Log.e("SismicsMusic", "Error playing song", e);
        }
    }

    /**
     * Called when media player is done playing current song.
    */
    @Override
    public void onCompletion(MediaPlayer player) {
        // The media player finished playing the current song, so we go ahead and start the next.
        playNextSong();
    }

    /**
     * Called when media player is done preparing.
    */
    @Override
    public void onPrepared(MediaPlayer player) {
        // The media player is done preparing. That means we can start playing!
        mState = State.Playing;
        configAndStartMediaPlayer();
    }

    /**
     * Return a notification based on the current music playback state.
     * @return Notification
     */
    public Notification getNotification() {
        // Get the cached cover image
        String coverUrl = PreferenceUtil.getServerUrl(this) + "/api/album/" + currentPlaylistTrack.getAlbum().getId() + "/albumart/small";
        Bitmap coverBitmap = new AQuery(this).getCachedImage(coverUrl, 96);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(currentPlaylistTrack.getTrack().getTitle())
                .setContentText(currentPlaylistTrack.getArtist().getName())
                .setSubText(currentPlaylistTrack.getAlbum().getName())
                .setTicker(currentPlaylistTrack.getTrack().getTitle() + " - " + currentPlaylistTrack.getArtist().getName())
                .setLargeIcon(coverBitmap)
                .setOngoing(true)
                .setContentIntent(PendingIntent.getActivity(this, 0,
                        new Intent(getApplicationContext(), MainActivity.class),
                        PendingIntent.FLAG_UPDATE_CURRENT));

        // Play/pause actions
        if (mState == State.Paused) {
            builder.addAction(R.drawable.ic_action_play_dark, getString(R.string.play),
                    PendingIntent.getService(this, 0, new Intent(MusicService.ACTION_PLAY, null, this, MusicService.class),
                            PendingIntent.FLAG_UPDATE_CURRENT));
        } else {
            builder.addAction(R.drawable.ic_action_pause_dark, getString(R.string.pause),
                    PendingIntent.getService(this, 0, new Intent(MusicService.ACTION_PAUSE, null, this, MusicService.class),
                            PendingIntent.FLAG_UPDATE_CURRENT));
        }

        // Next/stop actions
        builder.addAction(R.drawable.ic_action_next_dark, getString(R.string.next),
                PendingIntent.getService(this, 0, new Intent(MusicService.ACTION_SKIP, null, this, MusicService.class),
                        PendingIntent.FLAG_UPDATE_CURRENT))
                .addAction(R.drawable.ic_action_stop_dark, getString(R.string.stop),
                        PendingIntent.getService(this, 0, new Intent(MusicService.ACTION_STOP, null, this, MusicService.class),
                                PendingIntent.FLAG_UPDATE_CURRENT));

        return builder.build();
    }

    /**
     * Called when there's an error playing media. When this happens, the media player goes to
     * the Error state. We warn the user about the error and reset the media player.
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(this, "Media player error! Resetting.", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Error: what=" + String.valueOf(what) + ", extra=" + String.valueOf(extra));

        mState = State.Stopped;
        relaxResources(true);
        giveUpAudioFocus();
        return true; // true indicates we handled the error
    }

    @Override
    public void onGainedAudioFocus() {
        Toast.makeText(this, "gained audio focus.", Toast.LENGTH_SHORT).show();
        mAudioFocus = AudioFocus.Focused;

        // restart media player with new focus settings
        if (mState == State.Playing)
            configAndStartMediaPlayer();
    }

    @Override
    public void onLostAudioFocus(boolean canDuck) {
        Toast.makeText(this, "lost audio focus." + (canDuck ? "can duck" :
            "no duck"), Toast.LENGTH_SHORT).show();
        mAudioFocus = canDuck ? AudioFocus.NoFocusCanDuck : AudioFocus.NoFocusNoDuck;

        // start/restart/pause media player with new focus settings
        if (mPlayer != null && mPlayer.isPlaying())
            configAndStartMediaPlayer();
    }

    @Override
    public void onDestroy() {
        mediaPlayerHandler = null;
        EventBus.getDefault().unregister(this);

        // Service is being killed, so make sure we release our resources
        mState = State.Stopped;
        relaxResources(true);
        giveUpAudioFocus();
    }

    /**
     * Media player state has changed.
     * @param event Event
     */
    @Subscribe
    public void onEvent(MediaPlayerStateChangedEvent event) {
        if (event.getPlaylistTrack() == null) {
            return;
        }

        if (event.getDuration() < 0) {
            return;
        }

        Log.d("MusicService", "Media player is progressing: " + event.getCurrentPosition() + "/" + event.getDuration());
        if (event.getCurrentPosition() > event.getDuration() / 2 && !songCompleted) {
            // The song is considered completed
            songCompleted = true;
            ScrobbleUtil.trackCompleted(this, event.getPlaylistTrack().getTrack().getId(), event.getSongStartedAt());
        }
    }

    /**
     * Media player seeking.
     * @param event Event
     */
    @Subscribe
    public void onEvent(MediaPlayerSeekEvent event) {
        if (mState == State.Playing || mState == State.Paused) {
            mPlayer.seekTo(event.getPosition());
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
