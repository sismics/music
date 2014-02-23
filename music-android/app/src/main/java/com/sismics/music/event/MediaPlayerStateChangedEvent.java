package com.sismics.music.event;

import com.sismics.music.model.PlaylistTrack;

/**
 * Media player state changed event.
 *
 * @author bgamard.
 */
public class MediaPlayerStateChangedEvent {

    private long songStartedAt;
    private PlaylistTrack playlistTrack;
    private int currentPosition;
    private int duration;

    public MediaPlayerStateChangedEvent(long songStartedAt, PlaylistTrack playlistTrack, int currentPosition, int duration) {
        this.songStartedAt = songStartedAt;
        this.playlistTrack = playlistTrack;
        this.currentPosition = currentPosition;
        this.duration = duration;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public int getDuration() {
        return duration;
    }

    public long getSongStartedAt() {
        return songStartedAt;
    }

    public PlaylistTrack getPlaylistTrack() {
        return playlistTrack;
    }
}
