package com.sismics.music.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.sismics.music.R;
import com.sismics.music.util.PreferenceUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Set;

/**
 * Adapter for albums list.
 * 
 * @author bgamard
 */
public class AlbumAdapter extends BaseAdapter implements Filterable {

    private AQuery aq;
    private Activity activity;
    private JSONArray albums;
    private String authToken;
    private String serverUrl;
    private Set<String> cachedAlbumSet;

    /**
     * Constructor.
     * @param activity Context activity
     */
    public AlbumAdapter(Activity activity, JSONArray albums, Set<String> cachedAlbumSet) {
        this.activity = activity;
        this.albums = albums;
        this.cachedAlbumSet = cachedAlbumSet;
        this.aq = new AQuery(activity);
        this.authToken = PreferenceUtil.getAuthToken(activity);
        this.serverUrl = PreferenceUtil.getServerUrl(activity);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.list_item_album, null);
            aq.recycle(view);
            holder = new ViewHolder();
            holder.albumName = aq.id(R.id.albumName).getTextView();
            holder.artistName = aq.id(R.id.artistName).getTextView();
            holder.imgCover = aq.id(R.id.imgCover).getImageView();
            holder.cached = aq.id(R.id.cached).getImageView();
            view.setTag(holder);
        } else {
            aq.recycle(view);
            holder = (ViewHolder) view.getTag();
        }

        JSONObject album = getItem(position);

        // Album cover
        String albumId = album.optString("id");
        String coverUrl = serverUrl + "/api/album/" + albumId + "/albumart/small";
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
        holder.albumName.setText(album.optString("name"));
        JSONObject artist = album.optJSONObject("artist");
        holder.artistName.setText(artist.optString("name"));
        holder.cached.setVisibility(cachedAlbumSet.contains(albumId) ? View.VISIBLE : View.GONE);

        return view;
    }

    @Override
    public int getCount() {
        return albums.length();
    }

    @Override
    public JSONObject getItem(int position) {
        return albums.optJSONObject(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setAlbums(JSONArray albums) {
        this.albums = albums;
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint == null || constraint.length() == 0) {
                    results.values = albums;
                    results.count = albums.length();
                    return  results;
                }

                JSONArray values = new JSONArray();
                for (int i = 0; i < albums.length(); i++) {
                    JSONObject album = albums.optJSONObject(i);
                    if (album.optString("name").contains(constraint)) {
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
    }
}
