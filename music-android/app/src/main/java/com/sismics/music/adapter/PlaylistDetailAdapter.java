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
import com.loopj.android.http.JsonHttpResponseHandler;
import com.sismics.music.R;
import com.sismics.music.db.dao.TrackDao;
import com.sismics.music.event.TrackLikedChangedEvent;
import com.sismics.music.model.Playlist;
import com.sismics.music.model.PlaylistTrack;
import com.sismics.music.model.Track;
import com.sismics.music.resource.TrackResource;
import com.sismics.music.util.RemoteControlUtil;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.List;

/**
 * Adapter for playlist details.
 * 
 * @author jtremeaux
 */
public class PlaylistDetailAdapter extends BaseAdapter {
    /**
     * Context.
     */
    private Activity activity;

    /**
     * AQuery.
     */
    private AQuery aq;

    private Playlist playlist;

    private List<PlaylistTrack> playlistTracks;

    /**
     * Constructor.
     * @param activity Context activity
     */
    public PlaylistDetailAdapter(Activity activity, Playlist playlist, List<PlaylistTrack> playlistTracks) {
        this.activity = activity;
        this.playlist = playlist;
        this.playlistTracks = playlistTracks;
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
            holder.liked = aq.id(R.id.liked).getImageView();
            holder.overflow = aq.id(R.id.overflow).getView();
            view.setTag(holder);
        } else {
            aq.recycle(view);
            holder = (ViewHolder) view.getTag();
        }
        
        // Filling track data
        final PlaylistTrack playlistTrack = getItem(position);
        final Track track = playlistTrack.getTrack();
        holder.trackName.setText(track.getTitle());
        boolean isCached = TrackDao.hasTrack(activity, track.getId());
        holder.cached.setVisibility(isCached ? View.VISIBLE : View.GONE);
        holder.liked.setVisibility(track.isLiked() ? View.VISIBLE : View.GONE);

        // Configuring popup menu
        aq.id(holder.overflow).clicked(v -> {
            PopupMenu popup = new PopupMenu(activity, v);
            popup.inflate(R.menu.list_item_track);
            MenuItem unpinMenuItem = popup.getMenu().findItem(R.id.unpin);
            unpinMenuItem.setVisible(isCached);
            MenuItem likeToggleMenuItem = popup.getMenu().findItem(R.id.likeToggle);
            likeToggleMenuItem.setTitle(track.isLiked() ? R.string.unlike_track : R.string.like_track);

            // Menu actions
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.likeToggle:
                        JsonHttpResponseHandler callback = new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(final JSONObject json) {
                                track.setLiked(!track.isLiked());
                                TrackDao.updateTrack(activity, track);
                                EventBus.getDefault().post(new TrackLikedChangedEvent(track));
                            }
                        };

                        if (track.isLiked()) {
                            TrackResource.unlike(activity, track.getId(), callback);
                        } else {
                            TrackResource.like(activity, track.getId(), callback);
                        }
                        return true;

//                    case R.id.unpin:
//                        CacheUtil.removeTrack(activity, artist.getId(), album.getId(), track.getId());
//                        EventBus.getDefault().post(new TrackCacheStatusChangedEvent(null));
//                        return true;

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
        return playlistTracks.size();
    }

    @Override
    public PlaylistTrack getItem(int position) {
        return playlistTracks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setPlaylistTracks(List<PlaylistTrack> playlistTracks) {
        this.playlistTracks = playlistTracks;
        notifyDataSetChanged();
    }

    public List<PlaylistTrack> getPlaylistTracks() {
        return playlistTracks;
    }

    /**
     * Article ViewHolder.
     * 
     * @author bgamard
     */
    private static class ViewHolder {
        TextView trackName;
        ImageView cached;
        ImageView liked;
        View overflow;
    }
}
