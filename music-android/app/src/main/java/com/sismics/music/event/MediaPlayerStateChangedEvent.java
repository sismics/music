package com.sismics.music.event;

import com.sismics.music.model.PlaylistTrack;
import com.sismics.music.service.MusicService;

/**
 * Media player state changed event.
 *
 * @author bgamard.
 */
public class MediaPlayerStateChangedEvent {

    private MusicService.State state;
    private long songStartedAt;
    private PlaylistTrack playlistTrack;
    private int currentPosition;
    private int duration;

    public MediaPlayerStateChangedEvent(MusicService.State state, long songStartedAt, PlaylistTrack playlistTrack, int currentPosition, int duration) {
        this.state = state;
        this.songStartedAt = songStartedAt;
        this.playlistTrack = playlistTrack;
        this.currentPosition = currentPosition;
        this.duration = duration;
    }

    public MusicService.State getState() {
        return state;
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
