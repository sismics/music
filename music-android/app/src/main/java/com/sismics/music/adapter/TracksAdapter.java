package com.sismics.music.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.sismics.music.R;
import com.sismics.music.event.TrackCacheStatusChangedEvent;
import com.sismics.music.model.Album;
import com.sismics.music.model.Track;
import com.sismics.music.util.CacheUtil;
import com.sismics.music.util.RemoteControlUtil;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Adapter for tracks list.
 * 
 * @author bgamard
 */
public class TracksAdapter extends BaseAdapter {
    /**
     * Context.
     */
    private Activity activity;

    /**
     * AQuery.
     */
    private AQuery aq;

    /**
     * Album.
     */
    private Album album;

    /**
     * Tracks.
     */
    private List<Track> tracks;

    /**
     * Constructor.
     * @param activity Context activity
     */
    public TracksAdapter(Activity activity, Album album, List<Track> tracks) {
        this.activity = activity;
        this.album = album;
        this.tracks = tracks;
        this.aq = new AQuery(activity);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.list_item_track, null);
            aq.recycle(view);
            holder = new ViewHolder();
            holder.trackName = aq.id(R.id.trackName).getTextView();
            holder.cached = aq.id(R.id.cached).getImageView();
            holder.overflow = aq.id(R.id.overflow).getView();
            view.setTag(holder);
        } else {
            aq.recycle(view);
            holder = (ViewHolder) view.getTag();
        }
        
        // Filling track data
        final Track track = getItem(position);
        holder.trackName.setText(track.getTitle());
        holder.cached.setVisibility(CacheUtil.isComplete(album, track) ? View.VISIBLE : View.INVISIBLE);

        // Configuring popup menu
        aq.id(holder.overflow).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(activity, v);
                popup.inflate(R.menu.list_item_track);

                // Menu actions
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.unpin:
                                CacheUtil.removeTrack(album, track);
                                EventBus.getDefault().post(new TrackCacheStatusChangedEvent(null));
                                return true;

                            case R.id.remote_play:
                                RemoteControlUtil.commandPlayTrack(activity, track.getId());
                                return true;
                        }

                        return false;
                    }
                });

                popup.show();
            }
        });

        return view;
    }

    @Override
    public int getCount() {
        return tracks.size();
    }

    @Override
    public Track getItem(int position) {
        return tracks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
        notifyDataSetChanged();
    }

    public List<Track> getTracks() {
        return tracks;
    }

    /**
     * Article ViewHolder.
     * 
     * @author bgamard
     */
    private static class ViewHolder {
        TextView trackName;
        ImageView cached;
        View overflow;
    }
}
