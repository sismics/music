package com.sismics.music.event;

import com.sismics.music.model.PlaylistTrack;

/**
 * Track cache status changed.
 * This event can specify a track or not.
 * TODO Split this event in {Artist,Album,Track}Cache{Removed,Added}Event events
 *
 * @author bgamard.
 */
public class TrackCacheStatusChangedEvent {

    private PlaylistTrack playlistTrack;

    public TrackCacheStatusChangedEvent(PlaylistTrack playlistTrack) {
        this.playlistTrack = playlistTrack;
    }

    public PlaylistTrack getPlaylistTrack() {
        return playlistTrack;
    }
}
