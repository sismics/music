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
import android.widget.PopupMenu;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.sismics.music.R;
import com.sismics.music.event.TrackCacheStatusChangedEvent;
import com.sismics.music.model.FullAlbum;
import com.sismics.music.util.CacheUtil;
import com.sismics.music.util.PreferenceUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for albums list.
 * 
 * @author bgamard
 */
public class AlbumAdapter extends BaseAdapter implements Filterable {

    private AQuery aq;
    private Activity activity;
    private List<FullAlbum> onlineAlbums; // TODO Merge those 2 lists in one
    private List<FullAlbum> cachedAlbums;
    private List<FullAlbum> displayedAlbums;
    private String authToken;
    private String serverUrl;
    private boolean offlineMode;

    /**
     * Constructor.
     * @param activity Context activity
     */
    public AlbumAdapter(Activity activity, List<FullAlbum> onlineAlbums, List<FullAlbum> cachedAlbums, boolean offlineMode) {
        this.activity = activity;
        this.offlineMode = offlineMode;
        this.aq = new AQuery(activity);
        this.authToken = PreferenceUtil.getAuthToken(activity);
        this.serverUrl = PreferenceUtil.getServerUrl(activity);
        this.onlineAlbums = onlineAlbums;
        this.cachedAlbums = cachedAlbums;
        resetDisplayedAlbums();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.list_item_album, parent, false);
            aq.recycle(view);
            holder = new ViewHolder();
            holder.albumName = aq.id(R.id.albumName).getTextView();
            holder.artistName = aq.id(R.id.artistName).getTextView();
            holder.imgCover = aq.id(R.id.imgCover).getImageView();
            holder.cached = aq.id(R.id.cached).getImageView();
            holder.overflow = aq.id(R.id.overflow).getView();
            view.setTag(holder);
        } else {
            aq.recycle(view);
            holder = (ViewHolder) view.getTag();
        }

        FullAlbum album = getItem(position);

        // Album cover
        String coverUrl = serverUrl + "/api/album/" + album.getAlbum().getId() + "/albumart/small";
        aq.id(holder.imgCover).image(new BitmapAjaxCallback()
                .url(coverUrl)
                .animation(AQuery.FADE_IN_NETWORK)
                .cookie("auth_token", authToken)
        );

        // Filling album data
        holder.albumName.setText(album.getAlbum().getName());
        holder.artistName.setText(album.getArtist().getName());
        final View cached = holder.cached;
        cached.setVisibility(CacheUtil.isAlbumCached(activity, album.getAlbum().getId()) ? View.VISIBLE : View.GONE);

        // Configuring popup menu
        aq.id(holder.overflow).clicked(v -> {
            PopupMenu popup = new PopupMenu(activity, v);
            popup.inflate(R.menu.list_item_album);

            // Menu actions
            popup.setOnMenuItemClickListener(item -> {
                CacheUtil.removeAlbum(activity, album.getArtist().getId(), album.getAlbum().getId());
                EventBus.getDefault().post(new TrackCacheStatusChangedEvent(null));
                return true;
            });

            popup.show();
        });

        return view;
    }

    @Override
    public int getCount() {
        return displayedAlbums.size();
    }

    @Override
    public FullAlbum getItem(int position) {
        return displayedAlbums.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void resetOnlineAlbums() {
        onlineAlbums = new ArrayList<>();
    }

    public void addOnlineAlbums(List<FullAlbum> albums) {
        onlineAlbums.addAll(albums);
        resetDisplayedAlbums();
        notifyDataSetChanged();
    }

    public void setCachedAlbums(List<FullAlbum> albums) {
        cachedAlbums = albums;
        resetDisplayedAlbums();
        notifyDataSetChanged();
    }

    private void resetDisplayedAlbums() {
        displayedAlbums = offlineMode ? cachedAlbums : onlineAlbums;
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
                    results.values = cachedAlbums;
                    results.count = cachedAlbums.size();
                    return results;
                }

                // Search in album name and artist name
                List<FullAlbum> values = new ArrayList<>();
                String filter = constraint.toString().toLowerCase();
                for (FullAlbum album : cachedAlbums) {
                    if (album.getAlbum().getName().toLowerCase().contains(filter)
                            || album.getArtist().getName().toLowerCase().contains(filter)) {
                        values.add(album);
                    }
                }
                results.values = values;
                results.count = values.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null) {
                    displayedAlbums = (List<FullAlbum>) results.values;
                    notifyDataSetChanged();
                }
            }
        };
    }

    public void setOfflineMode(boolean offlineMode) {
        this.offlineMode = offlineMode;
        resetDisplayedAlbums();
        notifyDataSetChanged();
    }

    /**
     * Album ViewHolder.
     * 
     * @author bgamard
     */
    private static class ViewHolder {
        TextView albumName;
        TextView artistName;
        ImageView imgCover;
        ImageView cached;
        View overflow;
    }
}
