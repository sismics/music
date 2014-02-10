package com.sismics.music.core.event.async;

import com.google.common.base.Objects;
import com.sismics.music.core.model.dbi.Track;

/**
 * Play started event.
 *
 * @author jtremeaux
 */
public class PlayStartedEvent {
    /**
     * User ID.
     */
    private String userId;

    /**
     * Track.
     */
    private Track track;

    public PlayStartedEvent(String userId, Track track) {
        this.userId = userId;
        this.track = track;
    }

    public String getUserId() {
        return userId;
    }

    public Track getTrack() {
        return track;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("userId", userId)
                .add("trackId", track.getId())
                .toString();
    }
}
