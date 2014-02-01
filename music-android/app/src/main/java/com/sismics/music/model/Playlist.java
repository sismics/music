package com.sismics.music.model;

import android.content.Context;
import android.widget.Adapter;
import android.widget.BaseAdapter;

import com.sismics.music.util.PreferenceUtil;

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

        JSONObject track;

        public Track(JSONObject track) {
            this.track = track;
        }

        public String getUrl(Context context) {
            return PreferenceUtil.getServerUrl(context) + "/api/track/" + track.optString("id");
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
    }

    private static List<Track> trackList = new ArrayList<>();

    private static Set<BaseAdapter> adapterList = new HashSet<>();

    private static int currentTrackIndex = -1;

    public static void stop() {
        currentTrackIndex = -1;
        notifyAdapters();
    }

    public static Track next() {
        if (trackList.size() == 0) {
            return null;
        }
        if (currentTrackIndex == -1 || currentTrackIndex == trackList.size() - 1) {
            // Nothing was played or this is the end, start at the beginning
            currentTrackIndex = 0;
        } else {
            currentTrackIndex++;
        }

        notifyAdapters();
        return trackList.get(currentTrackIndex);
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

    public static void add(JSONObject json) {
        Track track = new Track(json);
        trackList.add(track);
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
