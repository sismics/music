package com.sismics.music.fragment

import android.app.Activity
import android.app.Fragment
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.Toast
import com.androidquery.AQuery
import com.androidquery.callback.BitmapAjaxCallback
import com.loopj.android.http.JsonHttpResponseHandler
import com.sismics.music.R
import com.sismics.music.adapter.TracksAdapter
import com.sismics.music.event.OfflineModeChangedEvent
import com.sismics.music.event.TrackCacheStatusChangedEvent
import com.sismics.music.model.Album
import com.sismics.music.model.Track
import com.sismics.music.resource.AlbumResource
import com.sismics.music.service.MusicService
import com.sismics.music.service.PlaylistService
import com.sismics.music.util.CacheUtil
import com.sismics.music.util.PreferenceUtil
import de.greenrobot.event.EventBus
import org.json.JSONObject
import java.util.*

/**
 * Album details fragment.
 *
 * @author bgamard
 */
class AlbumFragment : Fragment() {
    private var cacheTask: AsyncTask<*, *, *>? = null
    private var eventBus: EventBus? = null
    private var tracksAdapter: TracksAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        eventBus = EventBus.getDefault()
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        activity.actionBar!!.setDisplayHomeAsUpEnabled(true)
        activity.actionBar!!.setHomeButtonEnabled(true)
    }

    override fun onDetach() {
        activity.actionBar!!.setDisplayHomeAsUpEnabled(false)
        activity.actionBar!!.setHomeButtonEnabled(false)
        super.onDetach()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity.fragmentManager.popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val album = arguments.getSerializable(ARG_ALBUM) as Album

        // Inflate the view
        val view = inflater.inflate(R.layout.fragment_album, container, false)
        val aq = AQuery(view)

        // Populate the view with the given data
        aq.id(R.id.albumName).text(album.name)
        aq.id(R.id.artistName).text(album.artistName)
        val coverUrl = PreferenceUtil.getServerUrl(activity) + "/api/album/" + album.id + "/albumart/small"
        aq.id(R.id.imgCover).image(BitmapAjaxCallback().url(coverUrl).animation(AQuery.FADE_IN_NETWORK).cookie("auth_token", PreferenceUtil.getAuthToken(activity)))

        // Set a new adapter to the tracks list, and attach the header to the ListView
        val listTracks = aq.id(R.id.listTracks).listView
        listTracks.emptyView = view.findViewById(R.id.progress)
        val header = aq.id(R.id.header).view
        (header.parent as ViewGroup).removeView(header)
        header.layoutParams = AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        listTracks.addHeaderView(header, null, false)
        tracksAdapter = TracksAdapter(activity, album, ArrayList<Track>())
        listTracks.adapter = tracksAdapter

        // Add to queue on click
        listTracks.onItemClickListener = object: AdapterView.OnItemClickListener {
            override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                PlaylistService.add(album, tracksAdapter!!.getItem(position - 1))
                Toast.makeText(activity, R.string.add_toast, Toast.LENGTH_SHORT).show()
            }
        }

        // Play all
        aq.id(R.id.btnPlayAll).clicked {
            val trackList = tracksAdapter!!.getTracks()
            PlaylistService.clear(false)
            PlaylistService.addAll(album, trackList!!)
            val intent = Intent(MusicService.ACTION_PLAY, null, activity, MusicService::class.java)
            intent.putExtra(MusicService.EXTRA_FORCE, true)
            activity.startService(intent)
            Toast.makeText(activity, R.string.play_all_toast, Toast.LENGTH_SHORT).show()
        }

        // Add all
        aq.id(R.id.btnAddAll).clicked {
            val trackList = tracksAdapter!!.getTracks()
            PlaylistService.addAll(album, trackList!!)
            Toast.makeText(activity, R.string.add_all_toast, Toast.LENGTH_SHORT).show()
        }

        loadTracks()
        eventBus!!.register(this)
        return view
    }

    /**
     * Load tracks from local device and/or server, depending on offline mode.
     */
    private fun loadTracks() {
        val offlineMode = PreferenceUtil.getBooleanPreference(activity, PreferenceUtil.Pref.OFFLINE_MODE, false)
        val album = arguments.getSerializable(ARG_ALBUM) as Album

        // Grab cached tracks for this album
        cacheTask = object : AsyncTask<Album, Void, List<Track>>() {
            override fun doInBackground(vararg params: Album): List<Track> {
                return CacheUtil.getCachedTrack(params[0])
            }

            override fun onPostExecute(tracks: List<Track>) {
                tracksAdapter!!.setTracks(tracks)
            }
        }.execute(album)

        if (!offlineMode) {
            // We are in online mode, download the album details from the server
            AlbumResource.info(activity, album.id, object : JsonHttpResponseHandler() {
                override fun onSuccess(json: JSONObject?) {
                    if (activity == null) {
                        // The activity is dead, and this fragment has been detached
                        return
                    }

                    // Cancel the cache request, should not happen
                    cacheTask!!.cancel(true)

                    // Assemble tracks
                    val tracks = ArrayList<Track>()
                    val tracksJson = json!!.optJSONArray("tracks")
                    for (i in 0..tracksJson.length() - 1) {
                        tracks.add(Track.fromJson(tracksJson.optJSONObject(i)))
                    }

                    // Populate the adapter
                    tracksAdapter!!.setTracks(tracks)
                }
            })
        }
    }

    /**
     * A track cache status has changed.
     * @param event Event
     */
    fun onEvent(event: TrackCacheStatusChangedEvent) {
        tracksAdapter!!.notifyDataSetChanged()
    }

    /**
     * Offline mode has changed.
     * @param event Event
     */
    fun onEvent(event: OfflineModeChangedEvent) {
        loadTracks()
    }

    override fun onDestroyView() {
        if (cacheTask != null) {
            cacheTask!!.cancel(true)
        }
        eventBus!!.unregister(this)
        super.onDestroyView()
    }

    companion object {

        private val ARG_ALBUM = "album"

        /**
         * Returns a new instance of this fragment.
         */
        fun newInstance(album: Album): AlbumFragment {
            val fragment = AlbumFragment()
            val args = Bundle()
            args.putSerializable(ARG_ALBUM, album)
            fragment.arguments = args
            return fragment
        }
    }
}