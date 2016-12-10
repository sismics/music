package com.sismics.music.adapter

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.androidquery.AQuery
import com.androidquery.callback.BitmapAjaxCallback
import com.sismics.music.R
import com.sismics.music.model.PlaylistTrack
import com.sismics.music.service.PlaylistService
import com.sismics.music.util.PreferenceUtil

/**
 * Adapter for tracks list.
 *
 * @author bgamard
 */
class PlaylistAdapter
/**
 * Constructor.
 * @param activity Context activity
 */
(private val activity: Activity, private val absListView: AbsListView) : BaseAdapter() {
    private val authToken: String?
    private val serverUrl: String?
    private val aq: AQuery

    init {
        this.aq = AQuery(activity)
        this.authToken = PreferenceUtil.getAuthToken(activity)
        this.serverUrl = PreferenceUtil.getServerUrl(activity)
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var view = view
        val holder: ViewHolder

        if (view == null) {
            val vi = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = vi.inflate(R.layout.list_item_playlist, null)
            aq.recycle(view)
            holder = ViewHolder()
            holder.artistName = aq.id(R.id.artistName).textView
            holder.trackName = aq.id(R.id.trackName).textView
            holder.cached = aq.id(R.id.cached).imageView
            holder.playing = aq.id(R.id.playing).imageView
            holder.progress = aq.id(R.id.progress).progressBar
            holder.imgCover = aq.id(R.id.imgCover).imageView
            view!!.tag = holder
        } else {
            aq.recycle(view)
            holder = view.tag as ViewHolder
        }

        // Filling playlistTrack data
        val playlistTrack = getItem(position)
        holder.artistName!!.text = playlistTrack?.artistName
        holder.trackName!!.text = playlistTrack?.title
        when (playlistTrack?.cacheStatus) {
            PlaylistTrack.CacheStatus.NONE -> {
                holder.cached!!.visibility = View.GONE
                holder.progress!!.visibility = View.GONE
            }
            PlaylistTrack.CacheStatus.COMPLETE -> {
                holder.cached!!.visibility = View.VISIBLE
                holder.progress!!.visibility = View.GONE
            }
            PlaylistTrack.CacheStatus.DOWNLOADING -> {
                holder.cached!!.visibility = View.GONE
                holder.progress!!.visibility = View.VISIBLE
            }
        }

        // Playing status
        if (PlaylistService.currentTrack() === playlistTrack) {
            holder.playing!!.visibility = View.VISIBLE
            view.setBackgroundColor(Color.argb(32, 255, 136, 0))
        } else {
            holder.playing!!.visibility = View.INVISIBLE
            view.setBackgroundColor(Color.argb(0, 0, 0, 0))
        }

        // Album cover
        val albumId = playlistTrack?.albumId
        val coverUrl = "$serverUrl/api/album/$albumId/albumart/small"
        if (aq.shouldDelay(position, view, absListView, coverUrl)) {
            aq.id(holder.imgCover).image(null as Bitmap?)
        } else {
            aq.id(holder.imgCover).image(BitmapAjaxCallback().url(coverUrl).animation(AQuery.FADE_IN_NETWORK).cookie("auth_token", authToken))
        }

        return view
    }

    override fun getCount(): Int {
        return PlaylistService.length()
    }

    override fun getItem(position: Int): PlaylistTrack? {
        return PlaylistService.getAt(position)
    }

    override fun getItemId(position: Int): Long {
        return PlaylistService.getAt(position)!!.id.hashCode().toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    /**
     * PlaylistTrack ViewHolder.
     *
     * @author bgamard
     */
    private class ViewHolder {
        internal var artistName: TextView? = null
        internal var trackName: TextView? = null
        internal var cached: ImageView? = null
        internal var playing: ImageView? = null
        internal var imgCover: ImageView? = null
        internal var progress: ProgressBar? = null
    }
}
