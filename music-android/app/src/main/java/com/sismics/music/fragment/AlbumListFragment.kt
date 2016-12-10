package com.sismics.music.fragment

import android.app.Fragment
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import com.loopj.android.http.JsonHttpResponseHandler
import com.sismics.music.R
import com.sismics.music.adapter.AlbumAdapter
import com.sismics.music.event.AlbumOpenedEvent
import com.sismics.music.event.MyMusicMenuVisibilityChangedEvent
import com.sismics.music.event.OfflineModeChangedEvent
import com.sismics.music.event.TrackCacheStatusChangedEvent
import com.sismics.music.model.Album
import com.sismics.music.resource.AlbumResource
import com.sismics.music.util.CacheUtil
import com.sismics.music.util.PreferenceUtil
import de.greenrobot.event.EventBus
import kotlinx.android.synthetic.main.fragment_album_list.*
import org.json.JSONObject

/**
 * Albums list fragments.
 *
 * @author bgamard
 */
class AlbumListFragment : Fragment() {

    private var offlineMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.my_music, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.refresh -> {
                refreshAlbumList(true)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_album_list, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        offlineMode = PreferenceUtil.getBooleanPreference(activity, PreferenceUtil.Pref.OFFLINE_MODE, false)

        refreshAlbumList(false)

        // Clear the search input
        clearSearch.setOnClickListener { search.setText("") }

        // Filter the albums when the search input changes
        search.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val adapter = listAlbum.adapter as AlbumAdapter
                adapter.filter.filter(s)
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable) {
            }
        })

        // Open the album details on click
        listAlbum.setOnItemClickListener { parent, view, position, id ->
            val adapter = listAlbum.adapter as AlbumAdapter
            EventBus.getDefault().post(AlbumOpenedEvent(Album.fromJson(adapter.getItem(position))))
        }

        EventBus.getDefault().register(this)
    }

    /**
     * Refresh album list.
     * @param forceRefresh Force a refresh from the server even if there is cached data
     */
    private fun refreshAlbumList(forceRefresh: Boolean) {
        // Get cached albums
        val cachedAlbumSet = CacheUtil.cachedAlbumSet

        // Grab the data from the cache first
        val cache = PreferenceUtil.getCachedJson(activity, PreferenceUtil.Pref.CACHED_ALBUMS_LIST_JSON)
        if (cache != null) {
            val adapter = AlbumAdapter(activity, cache.optJSONArray("albums"), cachedAlbumSet, offlineMode)
            listAlbum.adapter = adapter
            adapter.filter.filter(search.text)
        }

        if (cache == null || forceRefresh) {
            // Download the album list from server
            AlbumResource.list(activity, object : JsonHttpResponseHandler() {
                override fun onSuccess(json: JSONObject?) {
                    if (activity == null) {
                        // The activity is dead, and this fragment has been detached
                        return
                    }

                    // Cache the albums list
                    val albums = json!!.optJSONArray("albums")
                    PreferenceUtil.setCachedJson(activity, PreferenceUtil.Pref.CACHED_ALBUMS_LIST_JSON, json)

                    // Publish the new albums to the adapter
                    var adapter: AlbumAdapter? = listAlbum.adapter as? AlbumAdapter
                    if (adapter != null) {
                        adapter.setAlbums(albums)
                    } else {
                        adapter = AlbumAdapter(activity, albums, cachedAlbumSet, offlineMode)
                        listAlbum.adapter = adapter
                    }

                    // Apply the filter on the new result set
                    adapter.filter.filter(search.text)
                }
            })
        }
    }

    override fun onDestroyView() {
        EventBus.getDefault().unregister(this)
        super.onDestroyView()
    }

    /**
     * My music menu visibility fragment has changed.
     * @param event Event
     */
    fun onEvent(event: MyMusicMenuVisibilityChangedEvent) {
        setMenuVisibility(event.isMenuVisible)
    }

    /**
     * Offline mode has changed.
     * @param event Event
     */
    fun onEvent(event: OfflineModeChangedEvent) {
        offlineMode = event.isOfflineMode
        val adapter = listAlbum.adapter as AlbumAdapter
        adapter.setOfflineMode(offlineMode)
        search.setText("")
    }

    /**
     * A track cache status has changed.
     * @param event Event
     */
    fun onEvent(event: TrackCacheStatusChangedEvent) {
        refreshAlbumList(false)
    }

    companion object {
        /**
         * Returns a new instance of this fragment.
         */
        fun newInstance(): AlbumListFragment {
            val fragment = AlbumListFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}