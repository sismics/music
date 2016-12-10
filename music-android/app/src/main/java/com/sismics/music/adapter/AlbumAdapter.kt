package com.sismics.music.adapter

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.androidquery.AQuery
import com.androidquery.callback.BitmapAjaxCallback
import com.sismics.music.R
import com.sismics.music.event.TrackCacheStatusChangedEvent
import com.sismics.music.util.CacheUtil
import com.sismics.music.util.PreferenceUtil
import de.greenrobot.event.EventBus
import org.json.JSONArray
import org.json.JSONObject

/**
 * Adapter for albums list.
 *
 * @author bgamard
 */
class AlbumAdapter(
        private val activity: Activity,
        private var originalAlbums: JSONArray?,
        private val cachedAlbumSet: Set<String>,
        private var offlineMode: Boolean) : BaseAdapter(), Filterable {

    private val aq: AQuery
    private var allAlbums: JSONArray? = null
    private var albums: JSONArray? = null
    private val authToken: String?
    private val serverUrl: String?

    init {
        this.aq = AQuery(activity)
        this.authToken = PreferenceUtil.getAuthToken(activity)
        this.serverUrl = PreferenceUtil.getServerUrl(activity)
        computeAlbumList()
        this.albums = this.allAlbums
    }

    private fun computeAlbumList() {
        if (offlineMode) {
            allAlbums = JSONArray()
            for (i in 0..originalAlbums!!.length() - 1) {
                val album = originalAlbums!!.optJSONObject(i)
                if (cachedAlbumSet.contains(album.optString("id"))) {
                    allAlbums!!.put(album)
                }
            }
        } else {
            allAlbums = originalAlbums
        }
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var view = view
        val holder: ViewHolder

        if (view == null) {
            val vi = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = vi.inflate(R.layout.list_item_album, null)
            aq.recycle(view)
            holder = ViewHolder()
            holder.albumName = aq.id(R.id.albumName).textView
            holder.artistName = aq.id(R.id.artistName).textView
            holder.imgCover = aq.id(R.id.imgCover).imageView
            holder.cached = aq.id(R.id.cached).imageView
            holder.overflow = aq.id(R.id.overflow).view
            view!!.tag = holder
        } else {
            aq.recycle(view)
            holder = view.tag as ViewHolder
        }

        val album = getItem(position)

        // Album cover
        val albumId = album.optString("id")
        val coverUrl = "$serverUrl/api/album/$albumId/albumart/small"
        if (aq.shouldDelay(position, view, parent, coverUrl)) {
            aq.id(holder.imgCover).image(null as Bitmap?)
        } else {
            aq.id(holder.imgCover).image(BitmapAjaxCallback().url(coverUrl).animation(AQuery.FADE_IN_NETWORK).cookie("auth_token", authToken))
        }

        // Filling album data
        holder.albumName!!.text = album.optString("name")
        val artist = album.optJSONObject("artist")
        holder.artistName!!.text = artist.optString("name")
        val cached = holder.cached
        cached!!.visibility = if (cachedAlbumSet.contains(albumId)) View.VISIBLE else View.GONE

        // Configuring popup menu
        aq.id(holder.overflow).clicked { v ->
            val popup = PopupMenu(activity, v)
            popup.inflate(R.menu.list_item_album)

            // Menu actions
            popup.setOnMenuItemClickListener {
                CacheUtil.removeAlbum(albumId)
                EventBus.getDefault().post(TrackCacheStatusChangedEvent(null))
                true
            }

            popup.show()
        }

        return view
    }

    override fun getCount(): Int {
        return albums!!.length()
    }

    override fun getItem(position: Int): JSONObject {
        return albums!!.optJSONObject(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun setAlbums(albums: JSONArray) {
        this.originalAlbums = albums
        computeAlbumList()
        this.albums = allAlbums
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {
                val results = Filter.FilterResults()
                if (constraint == null || constraint.isEmpty()) {
                    results.values = allAlbums
                    results.count = allAlbums!!.length()
                    return results
                }

                // Search in album name and artist name
                val values = JSONArray()
                val filter = constraint.toString().toLowerCase()
                for (i in 0..allAlbums!!.length() - 1) {
                    val album = allAlbums!!.optJSONObject(i)
                    if (album.optString("name").toLowerCase().contains(filter) || album.optJSONObject("artist").optString("name").toLowerCase().contains(filter)) {
                        values.put(album)
                    }
                }
                results.values = values
                results.count = values.length()
                return results
            }

            override fun publishResults(constraint: CharSequence, results: Filter.FilterResults) {
                albums = results.values as JSONArray
                notifyDataSetChanged()
            }
        }
    }

    fun setOfflineMode(offlineMode: Boolean) {
        this.offlineMode = offlineMode
        computeAlbumList()
        notifyDataSetChanged()
    }

    /**
     * Album ViewHolder.
     * @author bgamard
     */
    private class ViewHolder {
        internal var albumName: TextView? = null
        internal var artistName: TextView? = null
        internal var imgCover: ImageView? = null
        internal var cached: ImageView? = null
        internal var overflow: View? = null
    }
}
