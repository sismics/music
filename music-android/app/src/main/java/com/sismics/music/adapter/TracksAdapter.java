package com.sismics.music.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
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

import org.greenrobot.eventbus.EventBus;

import java.util.List;

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
            view = vi.inflate(R.layout.list_item_track, parent, false);
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
        holder.cached.setVisibility(CacheUtil.isComplete(activity, album, track) ? View.VISIBLE : View.INVISIBLE);

        // Configuring popup menu
        aq.id(holder.overflow).clicked(v -> {
            PopupMenu popup = new PopupMenu(activity, v);
            popup.inflate(R.menu.list_item_track);

            // Menu actions
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.unpin:
                        CacheUtil.removeTrack(activity, album, track);
                        EventBus.getDefault().post(new TrackCacheStatusChangedEvent(null));
                        return true;

                    case R.id.remote_play:
                        String command = RemoteControlUtil.buildCommand(RemoteControlUtil.Command.PLAY_TRACK, track.getId());
                        RemoteControlUtil.sendCommand(activity, command, R.string.remote_play_track);
                        return true;
                }

                return false;
            });

            popup.show();
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
