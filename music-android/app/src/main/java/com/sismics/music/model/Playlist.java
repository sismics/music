package com.sismics.music.model;

import android.content.Context;
import android.widget.BaseAdapter;

import com.sismics.music.util.CacheUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author bgamard.
 */
public class Playlist {

    public static class Track {

        public static enum CacheStatus {
            NONE, DOWNLOADING, COMPLETE
        }

        JSONObject track;

        CacheStatus cacheStatus;

        public Track(Context context, JSONObject track) {
            this.track = track;
            computeCacheStatus(context);
        }

        private void computeCacheStatus(Context context) {
            cacheStatus = CacheUtil.isComplete(context, this) ? CacheStatus.COMPLETE : CacheStatus.NONE;
        }

        public String getTitle() {
            return track.optString("title");
        }

        public String getId() {
            return track.optString("id");
        }

        public String getArtistName() {
            return "";
        }

        public String getAlbumName() {
            return "";
        }

        public long getLength() {
            return track.optInt("length");
        }

        public CacheStatus getCacheStatus() {
            return cacheStatus;
        }

        public void setCacheStatus(CacheStatus cacheStatus) {
            this.cacheStatus = cacheStatus;
        }
    }

    private static List<Track> trackList = new ArrayList<>();

    private static Set<BaseAdapter> adapterList = new HashSet<>();

    private static int currentTrackIndex = -1;

    public static void stop() {
        currentTrackIndex = -1;
        notifyAdapters();
    }

    public static Track next(boolean advance) {
        int index = currentTrackIndex;

        if (trackList.size() == 0) {
            return null;
        }
        if (index == -1 || index == trackList.size() - 1) {
            // Nothing was played or this is the end, start at the beginning
            index = 0;
        } else {
            index++;
        }

        if (advance) {
            currentTrackIndex = index;
            notifyAdapters();
        }

        return trackList.get(index);
    }

    public static Track after(Track before) {
        if (!trackList.contains(before)) {
            // The track has been delete since
            return null;
        }

        int beforeIndex = trackList.indexOf(before);
        return getAt(++beforeIndex);
    }

    public static void change(int position) {
        if (getAt(position) != null) {
            currentTrackIndex = position;
        } else {
            // The given track index is wrong, reset
            currentTrackIndex = -1;
        }
    }

    public static Track currentTrack() {
        if (trackList.size() > currentTrackIndex && currentTrackIndex >= 0) {
            return trackList.get(currentTrackIndex);
        }
        return null;
    }

    public static Track getAt(int position) {
        if (position > -1 && position < trackList.size()) {
            return trackList.get(position);
        }
        return  null;
    }

    public static int length() {
        return trackList.size();
    }

    public static void add(Context context, JSONObject json) {
        Track track = new Track(context, json);
        trackList.add(track);
        notifyAdapters();
    }

    public static void updateTrackCacheStatus(Track track, Track.CacheStatus status) {
        if (!trackList.contains(track)) {
            // The track has been delete since
            return;
        }
        track.setCacheStatus(status);
        notifyAdapters();
    }

    public static void registerAdapter(BaseAdapter adapter) {
        adapterList.add(adapter);
    }

    public static void unregisterAdapter(BaseAdapter adapter) {
        adapterList.remove(adapter);
    }

    private static void notifyAdapters() {
        for (BaseAdapter adapter : adapterList) {
            adapter.notifyDataSetChanged();
        }
    }
}
