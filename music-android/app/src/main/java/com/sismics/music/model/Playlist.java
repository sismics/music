package com.sismics.music.model;

import android.content.Context;

import com.sismics.music.util.PreferenceUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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

        public String getArtist() {
            return "";
        }

        public String getAlbum() {
            return "";
        }

        public long getDuration() {
            return 0l;
        }
    }

    private static List<Track> trackList = new ArrayList<>();

    private static int currentTrackIndex = 0;

    public static Track next() {
        if (trackList.size() == 0) {
            return null;
        }
        return trackList.get(currentTrackIndex);
    }
}
