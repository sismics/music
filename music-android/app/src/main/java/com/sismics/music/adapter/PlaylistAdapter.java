package com.sismics.music.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.sismics.music.R;
import com.sismics.music.model.PlaylistTrack;
import com.sismics.music.service.PlaylistService;
import com.sismics.music.util.PreferenceUtil;

/**
 * Adapter for tracks list.
 * 
 * @author bgamard
 */
public class PlaylistAdapter extends BaseAdapter {

    private Activity activity;
    private String authToken;
    private String serverUrl;
    private AQuery aq;
    private AbsListView absListView;

    /**
     * Constructor.
     * @param activity Context activity
     */
    public PlaylistAdapter(Activity activity, AbsListView absListView) {
        this.activity = activity;
        this.aq = new AQuery(activity);
        this.authToken = PreferenceUtil.getAuthToken(activity);
        this.serverUrl = PreferenceUtil.getServerUrl(activity);
        this.absListView = absListView;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.list_item_playlist, parent, false);
            aq.recycle(view);
            holder = new ViewHolder();
            holder.artistName = aq.id(R.id.artistName).getTextView();
            holder.trackName = aq.id(R.id.trackName).getTextView();
            holder.cached  = aq.id(R.id.cached).getImageView();
            holder.playing = aq.id(R.id.playing).getImageView();
            holder.failed = aq.id(R.id.failed).getImageView();
            holder.progress = aq.id(R.id.progress).getProgressBar();
            holder.downloadProgress = aq.id(R.id.downloadProgress).getView();
            holder.imgCover = aq.id(R.id.imgCover).getImageView();
            view.setTag(holder);
        } else {
            aq.recycle(view);
            holder = (ViewHolder) view.getTag();
        }

        // Filling playlistTrack data
        PlaylistTrack playlistTrack = getItem(position);
        holder.artistName.setText(playlistTrack.getArtist().getName());
        holder.trackName.setText(playlistTrack.getTrack().getTitle());
        switch (playlistTrack.getCacheStatus()) {
            case FAILURE:
                holder.cached.setVisibility(View.GONE);
                holder.progress.setVisibility(View.GONE);
                holder.failed.setVisibility(View.VISIBLE);
                break;
            case NONE:
                holder.cached.setVisibility(View.GONE);
                holder.progress.setVisibility(View.GONE);
                holder.failed.setVisibility(View.GONE);
                holder.downloadProgress.setVisibility(View.GONE);
                break;
            case COMPLETE:
                holder.cached.setVisibility(View.VISIBLE);
                holder.progress.setVisibility(View.GONE);
                holder.failed.setVisibility(View.GONE);
                holder.downloadProgress.setVisibility(View.GONE);
                break;
            case DOWNLOADING:
                holder.cached.setVisibility(View.GONE);
                holder.progress.setVisibility(View.VISIBLE);
                holder.failed.setVisibility(View.GONE);
                holder.downloadProgress.setVisibility(View.VISIBLE);
                ((LinearLayout.LayoutParams) holder.downloadProgress.getLayoutParams()).weight = playlistTrack.getProgress();
                break;
        }

        // Playing status
        if (PlaylistService.currentTrack() == playlistTrack) {
            holder.playing.setVisibility(View.VISIBLE);
            view.setBackgroundColor(Color.argb(32, 255, 136, 0));
        } else {
            holder.playing.setVisibility(View.INVISIBLE);
            view.setBackgroundColor(Color.argb(0, 0, 0, 0));
        }

        // Album cover
        String albumId = playlistTrack.getAlbum().getId();
        String coverUrl = serverUrl + "/api/album/" + albumId + "/albumart/small";
        if (aq.shouldDelay(position, view, absListView, coverUrl)) {
            aq.id(holder.imgCover).image((Bitmap) null);
        } else {
            aq.id(holder.imgCover).image(new BitmapAjaxCallback()
                    .url(coverUrl)
                    .animation(AQuery.FADE_IN_NETWORK)
                    .cookie("auth_token", authToken)
            );
        }

        return view;
    }

    @Override
    public int getCount() {
        return PlaylistService.length();
    }

    @Override
    public PlaylistTrack getItem(int position) {
        return PlaylistService.getAt(position);
    }

    @Override
    public long getItemId(int position) {
        return PlaylistService.getAt(position).getTrack().getId().hashCode();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    /**
     * PlaylistTrack ViewHolder.
     * 
     * @author bgamard
     */
    private static class ViewHolder {
        TextView artistName;
        TextView trackName;
        ImageView cached;
        ImageView playing;
        ImageView failed;
        ImageView imgCover;
        ProgressBar progress;
        View downloadProgress;
    }
}
