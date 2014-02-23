package com.sismics.music.event;

import com.sismics.music.model.PlaylistTrack;

/**
 * Track cache status changed.
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
