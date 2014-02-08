package com.sismics.music.core.service.player;

import com.sismics.music.core.model.jpa.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Player service.
 *
 * @author jtremeaux
 */
public class PlayerService {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(PlayerService.class);

    /**
     * Map of <user id, playing status>.
     */
    private Map<String, PlayerStatus> currentlyPlayerStatus;

    public PlayerService() {
        currentlyPlayerStatus = new HashMap<String, PlayerStatus>();
    }

    /**
     * Update the currently playing track.
     *
     * @param userId User ID
     * @param track Track
     * @param startDate Date the track was started
     * @param duration Duration into the track in seconds
     */
    public void notifyPlaying(String userId, Track track, Date startDate, Integer duration) {
        PlayerStatus status = currentlyPlayerStatus.get(userId);
        if (status == null || !status.getTrack().getId().equals(track.getId()) || !status.getStartDate().equals(startDate)) {
            status = new PlayerStatus(track, startDate, duration);
            currentlyPlayerStatus.put(userId, status);

            // TODO Notify Last.fm
        } else {
            status.setDuration(duration);
        }

        if (!status.isCommited() && duration >= track.getLength() / 2) {
            // TODO to local DB + Last.fm
        }
    }
}
