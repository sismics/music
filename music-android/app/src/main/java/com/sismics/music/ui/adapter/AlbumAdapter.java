package com.sismics.music.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.sismics.music.R;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Adapter for albums list.
 * 
 * @author bgamard
 */
public class AlbumAdapter extends BaseAdapter {
    /**
     * Context.
     */
    private Activity activity;
    
    /**
     * AQuery.
     */
    private AQuery aq;

    private JSONArray albums;
    
    /**
     * Constructor.
     * @param activity Context activity
     */
    public AlbumAdapter(Activity activity, JSONArray albums) {
        this.activity = activity;
        this.albums = albums;
        this.aq = new AQuery(activity);
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
            view.setTag(holder);
        } else {
            aq.recycle(view);
            holder = (ViewHolder) view.getTag();
        }
        
        // Filling album data
        JSONObject album = getItem(position);
        holder.albumName.setText(album.optString("name"));

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

    /**
     * Article ViewHolder.
     * 
     * @author bgamard
     */
    private static class ViewHolder {
        TextView albumName;
    }
}
