package com.sismics.music.fragment

import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import com.sismics.music.R
import com.sismics.music.adapter.PlaylistAdapter
import com.sismics.music.event.MediaPlayerSeekEvent
import com.sismics.music.event.MediaPlayerStateChangedEvent
import com.sismics.music.event.PlaylistChangedEvent
import com.sismics.music.event.TrackCacheStatusChangedEvent
import com.sismics.music.service.MusicService
import com.sismics.music.service.PlaylistService
import de.greenrobot.event.EventBus
import kotlinx.android.synthetic.main.fragment_playlist.*

/**
 * Playlist fragment.
 */
class PlaylistFragment : Fragment() {

    private var playlistAdapter: PlaylistAdapter? = null
    private var eventBus: EventBus? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.playlist, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.empty_playlist -> {
                // Stop the music and clear the playlist
                val intent = Intent(MusicService.ACTION_STOP, null, activity, MusicService::class.java)
                activity.startService(intent)
                PlaylistService.clear(true)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_playlist, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        playlistPause.visibility = View.GONE

        // Create a new playlist adapter
        playlistAdapter = PlaylistAdapter(activity, listTracks)

        // Configure the tracks list
        listTracks.setOnItemClickListener { parent, view, position, id ->
            PlaylistService.change(position - 1)
            val intent = Intent(MusicService.ACTION_PLAY, null, activity, MusicService::class.java)
            intent.putExtra(MusicService.EXTRA_FORCE, true)
            activity.startService(intent)
        }
        listTracks.emptyView = emptyPlaylist

        // Play button
        playlistPlay.setOnClickListener {
            val intent = Intent(MusicService.ACTION_PLAY, null, activity, MusicService::class.java)
            activity.startService(intent)
        }

        // Pause button
        playlistPause.setOnClickListener {
            val intent = Intent(MusicService.ACTION_PAUSE, null, activity, MusicService::class.java)
            activity.startService(intent)
        }

        // Stop button
        playlistStop.setOnClickListener {
            val intent = Intent(MusicService.ACTION_STOP, null, activity, MusicService::class.java)
            activity.startService(intent)
        }

        // Track removed
        listTracks.setRemoveListener { position ->
            if (PlaylistService.currentTrackIndex == position) {
                val intent = Intent(MusicService.ACTION_STOP, null, activity, MusicService::class.java)
                activity.startService(intent)
            }
            PlaylistService.remove(position)
        }

        // Track moved
        listTracks.setDropListener { from, to -> PlaylistService.move(from, to) }

        // Seekbar moved
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    eventBus!!.post(MediaPlayerSeekEvent(progress))
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })

        eventBus = EventBus.getDefault()
        eventBus!!.register(this)
    }

    /**
     * The playlist has changed.
     * @param event Event
     */
    fun onEvent(event: PlaylistChangedEvent) {
        playlistAdapter!!.notifyDataSetChanged()
    }

    /**
     * The cache status of a track has changed.
     * @param event Event
     */
    fun onEvent(event: TrackCacheStatusChangedEvent) {
        playlistAdapter!!.notifyDataSetChanged()
    }

    /**
     * Media player state has changed.
     * @param event Event
     */
    fun onEvent(event: MediaPlayerStateChangedEvent) {
        if (event.state == MusicService.State.Playing) {
            playlistPause.visibility = View.VISIBLE
            playlistPlay.visibility = View.GONE
        } else {
            playlistPause.visibility = View.GONE
            playlistPlay.visibility = View.VISIBLE
        }

        if (event.state == MusicService.State.Playing || event.state == MusicService.State.Paused) {
            seekBar!!.isEnabled = true
        } else {
            seekBar!!.isEnabled = false
            seekBar!!.progress = 0
        }

        if (event.duration < 0) {
            return
        }

        seekBar!!.progress = event.currentPosition
        seekBar!!.max = event.duration
    }

    override fun onDestroyView() {
        eventBus!!.unregister(this)
        super.onDestroyView()
    }

    companion object {

        /**
         * Returns a new instance of this fragment.
         */
        fun newInstance(): PlaylistFragment {
            val fragment = PlaylistFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}