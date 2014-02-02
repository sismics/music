package com.sismics.music.model;

import android.widget.BaseAdapter;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Global playlist manager.
 *
 * @author bgamard.
 */
public class Playlist {
    /**
     * Tracks in the playlist.
     */
    private static List<PlaylistTrack> playlistTrackList = new ArrayList<>();

    /**
     * Adapters registered to the playlist changes.
     */
    private static Set<BaseAdapter> adapterList = new HashSet<>();

    /**
     * Current track index.
     */
    private static int currentTrackIndex = -1;

    /**
     * Stop the playlist.
     */
    public static void stop() {
        currentTrackIndex = -1;
        notifyAdapters();
    }

    /**
     * Returns the next track.
     * @param advance If true, advance the current track accordingly
     * @return Next track
     */
    public static PlaylistTrack next(boolean advance) {
        int index = currentTrackIndex;

        if (playlistTrackList.size() == 0) {
            return null;
        }
        if (index == -1 || index == playlistTrackList.size() - 1) {
            // Nothing was played or this is the end, start at the beginning
            index = 0;
        } else {
            index++;
        }

        if (advance) {
            currentTrackIndex = index;
            notifyAdapters();
        }

        return playlistTrackList.get(index);
    }

    /**
     * Returns the track after the given one.
     * @param before Previous track
     * @return Next track
     */
    public static PlaylistTrack after(PlaylistTrack before) {
        if (!playlistTrackList.contains(before)) {
            // The track has been delete since
            return null;
        }

        int beforeIndex = playlistTrackList.indexOf(before);
        return getAt(++beforeIndex);
    }

    /**
     * Change the current played track.
     * @param position Position of the track
     */
    public static void change(int position) {
        if (getAt(position) != null) {
            currentTrackIndex = position;
        } else {
            // The given track index is wrong, reset
            currentTrackIndex = -1;
        }
    }

    /**
     * Returns the current track.
     * @return Current track
     */
    public static PlaylistTrack currentTrack() {
        if (playlistTrackList.size() > currentTrackIndex && currentTrackIndex >= 0) {
            return playlistTrackList.get(currentTrackIndex);
        }
        return null;
    }

    /**
     * Get the track at the given position.
     * @param position PlaylistTrack position
     * @return PlaylistTrack or null if there is no track at this position
     */
    public static PlaylistTrack getAt(int position) {
        if (position > -1 && position < playlistTrackList.size()) {
            return playlistTrackList.get(position);
        }
        return  null;
    }

    /**
     * Returns the playlist length.
     * @return Playlist length
     */
    public static int length() {
        return playlistTrackList.size();
    }

    /**
     * Add a track at the end of the playlist.
     * @param album Album
     * @param track Track data
     */
    public static void add(Album album, Track track) {
        PlaylistTrack playlistTrack = new PlaylistTrack(album, track);
        playlistTrackList.add(playlistTrack);
        notifyAdapters();
    }

    /**
     * Update a playlist track cache status.
     * @param playlistTrack PlaylistTrack to update
     * @param status New status
     */
    public static void updateTrackCacheStatus(PlaylistTrack playlistTrack, PlaylistTrack.CacheStatus status) {
        if (!playlistTrackList.contains(playlistTrack)) {
            // The playlistTrack has been delete since
            return;
        }
        playlistTrack.setCacheStatus(status);
        notifyAdapters();
    }

    /**
     * Register a new adapter.
     * @param adapter New adapter
     */
    public static void registerAdapter(BaseAdapter adapter) {
        adapterList.add(adapter);
    }

    /**
     * Unregister an adapter.
     * @param adapter Adapter to unregister
     */
    public static void unregisterAdapter(BaseAdapter adapter) {
        adapterList.remove(adapter);
    }

    /**
     * Notify adapters after a change in the playlist.
     */
    private static void notifyAdapters() {
        for (BaseAdapter adapter : adapterList) {
            adapter.notifyDataSetChanged();
        }
    }
}
