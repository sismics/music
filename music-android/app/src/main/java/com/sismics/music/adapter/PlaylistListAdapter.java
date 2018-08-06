package com.sismics.music.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.sismics.music.R;
import com.sismics.music.model.Playlist;
import com.sismics.music.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for playlists list.
 * 
 * @author jtremeaux
 */
public class PlaylistListAdapter extends BaseAdapter implements Filterable {

    private AQuery aq;
    private Activity activity;
    private List<Playlist> onlinePlaylists; // TODO Merge those 2 lists in one
    private List<Playlist> cachedPlaylists;
    private List<Playlist> displayedPlaylists;
    private String authToken;
    private String serverUrl;
    private boolean offlineMode;

    /**
     * Constructor.
     * @param activity Context activity
     */
    public PlaylistListAdapter(Activity activity, List<Playlist> onlinePlaylists, List<Playlist> cachedPlaylists, boolean offlineMode) {
        this.activity = activity;
        this.offlineMode = offlineMode;
        this.aq = new AQuery(activity);
        this.authToken = PreferenceUtil.getAuthToken(activity);
        this.serverUrl = PreferenceUtil.getServerUrl(activity);
        this.onlinePlaylists = onlinePlaylists;
        this.cachedPlaylists = cachedPlaylists;
        resetDisplayedPlaylists();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.list_item_playlist, parent, false);
            aq.recycle(view);
            holder = new ViewHolder();
            holder.playlistName = aq.id(R.id.playlistName).getTextView();
//            holder.imgCover = aq.id(R.id.imgCover).getImageView();
            holder.cached = aq.id(R.id.cached).getImageView();
            holder.overflow = aq.id(R.id.overflow).getView();
            view.setTag(holder);
        } else {
            aq.recycle(view);
            holder = (ViewHolder) view.getTag();
        }

        Playlist playlist = getItem(position);

//        // Playlist cover
//        String coverUrl = serverUrl + "/api/playlist/" + playlist.getPlaylist().getId() + "/playlistart/small";
//        aq.id(holder.imgCover).image(new BitmapAjaxCallback()
//                .url(coverUrl)
//                .animation(AQuery.FADE_IN_NETWORK)
//                .cookie("auth_token", authToken)
//        );

        // Filling playlist data
        holder.playlistName.setText(playlist.getName());
        final View cached = holder.cached;
//        cached.setVisibility(PlaylistDao.hasPlaylist(activity, playlist.getPlaylist().getId()) ? View.VISIBLE : View.GONE);
//
//        // Configuring popup menu
//        aq.id(holder.overflow).clicked(v -> {
//            PopupMenu popup = new PopupMenu(activity, v);
//            popup.inflate(R.menu.list_item_playlist);
//
//            // Menu actions
//            popup.setOnMenuItemClickListener(item -> {
//                CacheUtil.removePlaylist(activity, playlist.getArtist().getId(), playlist.getPlaylist().getId());
//                EventBus.getDefault().post(new TrackCacheStatusChangedEvent(null));
//                return true;
//            });
//
//            popup.show();
//        });

        return view;
    }

    @Override
    public int getCount() {
        return displayedPlaylists.size();
    }

    @Override
    public Playlist getItem(int position) {
        return displayedPlaylists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void resetOnlinePlaylists() {
        onlinePlaylists = new ArrayList<>();
    }

    public void addOnlinePlaylists(List<Playlist> playlists) {
        onlinePlaylists.addAll(playlists);
        resetDisplayedPlaylists();
        notifyDataSetChanged();
    }

    public void setCachedPlaylists(List<Playlist> playlists) {
        cachedPlaylists = playlists;
        resetDisplayedPlaylists();
        notifyDataSetChanged();
    }

    private void resetDisplayedPlaylists() {
        displayedPlaylists = offlineMode ? cachedPlaylists : onlinePlaylists;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                if (!offlineMode) {
                    return null;
                }
                FilterResults results = new FilterResults();
                if (constraint == null || constraint.length() == 0) {
                    results.values = cachedPlaylists;
                    results.count = cachedPlaylists.size();
                    return results;
                }

                // Search in playlist name and artist name
                List<Playlist> values = new ArrayList<>();
                String filter = constraint.toString().toLowerCase();
                for (Playlist playlist : cachedPlaylists) {
                    if (playlist.getName().toLowerCase().contains(filter)) {
                        values.add(playlist);
                    }
                }
                results.values = values;
                results.count = values.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null) {
                    displayedPlaylists = (List<Playlist>) results.values;
                    notifyDataSetChanged();
                }
            }
        };
    }

    public void setOfflineMode(boolean offlineMode) {
        this.offlineMode = offlineMode;
        resetDisplayedPlaylists();
        notifyDataSetChanged();
    }

    /**
     * Playlist ViewHolder.
     * 
     * @author bgamard
     */
    private static class ViewHolder {
        TextView playlistName;
        ImageView imgCover;
        ImageView cached;
        View overflow;
    }
}
