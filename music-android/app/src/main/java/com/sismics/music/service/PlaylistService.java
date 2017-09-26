package com.sismics.music.service;

import android.content.Context;

import com.sismics.music.event.PlaylistChangedEvent;
import com.sismics.music.model.Album;
import com.sismics.music.model.PlaylistTrack;
import com.sismics.music.model.Track;

import java.util.ArrayList;
import java.util.List;

import org.greenrobot.eventbus.EventBus;

/**
 * Playlist service.
 *
 * @author bgamard.
 */
public class PlaylistService {
    /**
     * Tracks in the playlist.
     */
    private static List<PlaylistTrack> playlistTrackList = new ArrayList<>();

    /**
     * Current track index.
     */
    private static int currentTrackIndex = -1;

    /**
     * Stop the playlist.
     */
    public static void stop() {
        currentTrackIndex = -1;
        EventBus.getDefault().post(new PlaylistChangedEvent());
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
            EventBus.getDefault().post(new PlaylistChangedEvent());
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
    public static void add(Context context, Album album, Track track) {
        PlaylistTrack playlistTrack = new PlaylistTrack(context, album, track);
        playlistTrackList.add(playlistTrack);
        EventBus.getDefault().post(new PlaylistChangedEvent());
    }

    public static int getCurrentTrackIndex() {
        return currentTrackIndex;
    }

    /**
     * Clear the playlist.
     */
    public static void clear(boolean notify) {
        playlistTrackList.clear();
        currentTrackIndex = -1;
        if (notify) {
            EventBus.getDefault().post(new PlaylistChangedEvent());
        }
    }

    /**
     * Add a list of tracks.
     * @param album Album linked to all tracks
     * @param trackList Tracks
     */
    public static void addAll(Context context, Album album, List<Track> trackList) {
        for (Track track : trackList) {
            PlaylistTrack playlistTrack = new PlaylistTrack(context, album, track);
            playlistTrackList.add(playlistTrack);
        }
        EventBus.getDefault().post(new PlaylistChangedEvent());
    }

    /**
     * Remove a track.
     * @param position Track position
     */
    public static void remove(int position) {
        if (position < currentTrackIndex) {
            currentTrackIndex--;
        }
        playlistTrackList.remove(position);
        EventBus.getDefault().post(new PlaylistChangedEvent());
    }

    /**
     * Move a track.
     * @param oldposition Old position
     * @param position New position
     */
    public static void move(int oldposition, int position) {
        if (oldposition < currentTrackIndex && position >= currentTrackIndex) {
            currentTrackIndex--;
        } else if (oldposition > currentTrackIndex && position <= currentTrackIndex) {
            currentTrackIndex++;
        } else if (oldposition == currentTrackIndex) {
            currentTrackIndex = position;
        }
        playlistTrackList.add(position, playlistTrackList.remove(oldposition));
        EventBus.getDefault().post(new PlaylistChangedEvent());
    }
}
