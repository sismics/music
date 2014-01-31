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

    private JSONArray tracks;

    /**
     * Constructor.
     * @param activity Context activity
     */
    public TracksAdapter(Activity activity, JSONArray tracks) {
        this.activity = activity;
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
            view.setTag(holder);
        } else {
            aq.recycle(view);
            holder = (ViewHolder) view.getTag();
        }
        
        // Filling track data
        JSONObject track = getItem(position);
        holder.trackName.setText(track.optString("title"));

        return view;
    }

    @Override
    public int getCount() {
        return tracks.length();
    }

    @Override
    public JSONObject getItem(int position) {
        return tracks.optJSONObject(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Article ViewHolder.
     * 
     * @author bgamard
     */
    private static class ViewHolder {
        TextView trackName;
    }
}
