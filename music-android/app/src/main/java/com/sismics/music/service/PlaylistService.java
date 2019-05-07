package com.sismics.music.service;

import android.content.Context;

import com.sismics.music.event.PlaylistChangedEvent;
import com.sismics.music.event.TrackLikedChangedEvent;
import com.sismics.music.model.Album;
import com.sismics.music.model.Artist;
import com.sismics.music.model.PlaylistTrack;
import com.sismics.music.model.Track;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * Playlist service.
 *
 * @author bgamard.
 */
public class PlaylistService {
    /**
     * Tracks in the playlist.
     */
    private List<PlaylistTrack> playlistTrackList = new ArrayList<>();

    /**
     * Current track index.
     */
    private int currentTrackIndex = -1;

    public PlaylistService() {
        EventBus.getDefault().register(this);
    }

    /**
     * Stop the playlist.
     */
    public void stop() {
        currentTrackIndex = -1;
        EventBus.getDefault().post(new PlaylistChangedEvent());
    }

    /**
     * Returns the next track.
     * @param advance If true, advance the current track accordingly
     * @return Next track
     */
    public PlaylistTrack next(boolean advance) {
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
    public PlaylistTrack after(PlaylistTrack before) {
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
    public void change(int position) {
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
    public PlaylistTrack currentTrack() {
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
    public PlaylistTrack getAt(int position) {
        if (position > -1 && position < playlistTrackList.size()) {
            return playlistTrackList.get(position);
        }
        return  null;
    }

    /**
     * Returns the playlist length.
     * @return Playlist length
     */
    public int length() {
        return playlistTrackList.size();
    }

    /**
     * Add a track at the end of the playlist.
     * @param artist Artist
     * @param album Album
     * @param track Track data
     */
    public void add(Context context, Artist artist, Album album, Track track) {
        PlaylistTrack playlistTrack = new PlaylistTrack(context, artist, album, track);
        playlistTrackList.add(playlistTrack);
        EventBus.getDefault().post(new PlaylistChangedEvent());
    }

    public int getCurrentTrackIndex() {
        return currentTrackIndex;
    }

    /**
     * Clear the playlist.
     */
    public void clear(boolean notify) {
        playlistTrackList.clear();
        currentTrackIndex = -1;
        if (notify) {
            EventBus.getDefault().post(new PlaylistChangedEvent());
        }
    }

    /**
     * Add a list of tracks.
     * @param artist Artist linked to all tracks
     * @param album Album linked to all tracks
     * @param trackList Tracks
     */
    public void addAll(Context context, Artist artist, Album album, List<Track> trackList) {
        for (Track track : trackList) {
            PlaylistTrack playlistTrack = new PlaylistTrack(context, artist, album, track);
            playlistTrackList.add(playlistTrack);
        }
        EventBus.getDefault().post(new PlaylistChangedEvent());
    }

    /**
     * Add a list of tracks.
     * @param playlistTrack The list of tracks
     */
    public void addAll(List<PlaylistTrack> playlistTrack) {
        playlistTrackList.addAll(playlistTrack);
    }

    /**
     * Remove a track.
     * @param position Track position
     */
    public void remove(int position) {
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
    public void move(int oldposition, int position) {
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

    @Subscribe(priority = -1)
    public void onEvent(TrackLikedChangedEvent event) {
        playlistTrackList.stream()
                .filter(playlistTrack -> playlistTrack.getTrack().getId().equals(event.getTrack().getId()))
                .forEach(playlistTrack -> playlistTrack.setTrack(event.getTrack()));
    }

    public List<PlaylistTrack> getPlaylistTrackList() {
        return playlistTrackList;
    }
}
