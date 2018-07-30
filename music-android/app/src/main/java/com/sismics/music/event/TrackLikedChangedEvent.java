package com.sismics.music.event;

import com.sismics.music.model.Track;

/**
 * Track liked status changed.
 *
 * @author bgamard.
 */
public class TrackLikedChangedEvent {

    private Track track;

    public TrackLikedChangedEvent(Track track) {
        this.track = track;
    }

    public Track getTrack() {
        return track;
    }
}
