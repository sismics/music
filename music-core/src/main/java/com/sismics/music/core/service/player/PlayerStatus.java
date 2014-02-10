package com.sismics.music.core.service.player;

import com.sismics.music.core.model.dbi.Track;

import java.util.Date;

/**
 * Player status.
 *
 * @author jtremeaux
 */
public class PlayerStatus {

    /**
     * Current track.
     */
    private Track track;

    /**
     * Date the track started playing.
     */
    private Date startDate;

    /**
     * Current duration into the track.
     */
    private Integer duration;

    /**
     * True if this track is considered as played (= played more than halfway).
     */
    private boolean commited;

    public PlayerStatus(Track track, Date startDate, Integer duration) {
        this.track = track;
        this.startDate = startDate;
        this.duration = duration;
    }

    public Track getTrack() {
        return track;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public boolean isCommited() {
        return commited;
    }

    public void setCommited(boolean commited) {
        this.commited = commited;
    }
}
