package com.sismics.music.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.sismics.music.R;
import com.sismics.music.model.Album;
import com.sismics.music.model.FullAlbum;
import com.sismics.music.util.PreferenceUtil;

import java.util.List;

/**
 * Adapter for albums list.
 * 
 * @author bgamard
 */
public class AlbumAdapter extends BaseAdapter {

    private AQuery aq;
    private Activity activity;
    private List<FullAlbum> onlineAlbums;
    private List<FullAlbum> cachedAlbums;
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
        if (aq.shouldDelay(position, view, parent, coverUrl)) {
            aq.id(holder.imgCover).image((Bitmap) null);
        } else {
            aq.id(holder.imgCover).image(new BitmapAjaxCallback()
                    .url(coverUrl)
                    .animation(AQuery.FADE_IN_NETWORK)
                    .cookie("auth_token", authToken)
            );
        }

        // Filling album data
        holder.albumName.setText(album.getAlbum().getName());
        holder.artistName.setText(album.getArtist().getName());
        final View cached = holder.cached;
        cached.setVisibility(cachedAlbums.contains(album) ? View.VISIBLE : View.GONE);

        // Configuring popup menu
        aq.id(holder.overflow).clicked(v -> {
            PopupMenu popup = new PopupMenu(activity, v);
            popup.inflate(R.menu.list_item_album);

            // Menu actions
            // TODO Remove album from cache
            /*
            popup.setOnMenuItemClickListener(item -> {
                CacheUtil.removeAlbum(activity, artist.getId(), album.getId());
                EventBus.getDefault().post(new TrackCacheStatusChangedEvent(null));
                return true;
            });
            */

            popup.show();
        });

        return view;
    }

    @Override
    public int getCount() {
        if (offlineMode) {
            return cachedAlbums.size();
        } else {
            return onlineAlbums.size();
        }
    }

    @Override
    public FullAlbum getItem(int position) {
        if (offlineMode) {
            return cachedAlbums.get(position);
        } else {
            return onlineAlbums.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setOnlineAlbums(List<FullAlbum> albums) {
        this.onlineAlbums = albums;
        notifyDataSetChanged();
    }

    /*
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint == null || constraint.length() == 0) {
                    results.values = allAlbums;
                    results.count = allAlbums.length();
                    return results;
                }

                // Search in album name and artist name
                JSONArray values = new JSONArray();
                String filter = constraint.toString().toLowerCase();
                for (int i = 0; i < allAlbums.length(); i++) {
                    JSONObject album = allAlbums.optJSONObject(i);
                    if (album.optString("name").toLowerCase().contains(filter)
                            || album.optJSONObject("artist").optString("name").toLowerCase().contains(filter)) {
                        values.put(album);
                    }
                }
                results.values = values;
                results.count = values.length();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                albums = (JSONArray) results.values;
                notifyDataSetChanged();
            }
        };
    }
    */

    public void setOfflineMode(boolean offlineMode) {
        this.offlineMode = offlineMode;
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
