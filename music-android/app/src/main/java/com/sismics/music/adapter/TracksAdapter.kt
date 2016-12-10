package com.sismics.music.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import com.androidquery.AQuery
import com.sismics.music.R
import com.sismics.music.event.TrackCacheStatusChangedEvent
import com.sismics.music.model.Album
import com.sismics.music.model.Track
import com.sismics.music.util.CacheUtil
import com.sismics.music.util.RemoteControlUtil
import de.greenrobot.event.EventBus

/**
 * Adapter for tracks list.
 *
 * @author bgamard
 */
class TracksAdapter(
        /**
         * Context.
         */
        private val activity: Activity,

        /**
         * Album.
         */
        private val album: Album,

        /**
         * Tracks.
         */
        private var tracks: List<Track>?) : BaseAdapter() {

    /**
     * AQuery.
     */
    private val aq: AQuery

    init {
        this.aq = AQuery(activity)
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var view = view
        val holder: ViewHolder

        if (view == null) {
            val vi = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = vi.inflate(R.layout.list_item_track, null)
            aq.recycle(view)
            holder = ViewHolder()
            holder.trackName = aq.id(R.id.trackName).textView
            holder.cached = aq.id(R.id.cached).imageView
            holder.overflow = aq.id(R.id.overflow).view
            view!!.tag = holder
        } else {
            aq.recycle(view)
            holder = view.tag as ViewHolder
        }

        // Filling track data
        val track = getItem(position)
        holder.trackName!!.text = track.title
        holder.cached!!.visibility = if (CacheUtil.isComplete(album, track)) View.VISIBLE else View.INVISIBLE

        // Configuring popup menu
        aq.id(holder.overflow).clicked { v ->
            val popup = PopupMenu(activity, v)
            popup.inflate(R.menu.list_item_track)

            // Menu actions
            popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.unpin -> {
                        CacheUtil.removeTrack(album, track)
                        EventBus.getDefault().post(TrackCacheStatusChangedEvent(null))
                        return@OnMenuItemClickListener true
                    }

                    R.id.remote_play -> {
                        val command = RemoteControlUtil.buildCommand(RemoteControlUtil.Command.PLAY_TRACK, track.id)
                        RemoteControlUtil.sendCommand(activity, command, R.string.remote_play_track)
                        return@OnMenuItemClickListener true
                    }
                }

                false
            })

            popup.show()
        }

        return view
    }

    override fun getCount(): Int {
        return tracks!!.size
    }

    override fun getItem(position: Int): Track {
        return tracks!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun setTracks(tracks: List<Track>) {
        this.tracks = tracks
        notifyDataSetChanged()
    }

    fun getTracks(): List<Track>? {
        return tracks
    }

    /**
     * Article ViewHolder.
     * @author bgamard
     */
    private class ViewHolder {
        internal var trackName: TextView? = null
        internal var cached: ImageView? = null
        internal var overflow: View? = null
    }
}
