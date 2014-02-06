package com.sismics.music.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.sismics.music.R;
import com.sismics.music.model.Album;
import com.sismics.music.service.PlaylistService;
import com.sismics.music.model.Track;

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
            view = vi.inflate(R.layout.list_item_track, null);
            aq.recycle(view);
            holder = new ViewHolder();
            holder.trackName = aq.id(R.id.trackName).getTextView();
            holder.addBtn = aq.id(R.id.add).getButton();
            view.setTag(holder);
        } else {
            aq.recycle(view);
            holder = (ViewHolder) view.getTag();
        }
        
        // Filling track data
        final Track track = getItem(position);
        holder.trackName.setText(track.getTitle());
        holder.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlaylistService.add(album, track);
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

    /**
     * Article ViewHolder.
     * 
     * @author bgamard
     */
    private static class ViewHolder {
        TextView trackName;
        Button addBtn;
    }
}
