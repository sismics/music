package com.sismics.music.core.service.player;

import com.sismics.music.core.event.async.PlayCompletedEvent;
import com.sismics.music.core.event.async.PlayStartedEvent;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.dbi.Track;

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
     * Map of <user id, playing status>.
     */
    private Map<String, PlayerStatus> currentlyPlayerStatus;

    public PlayerService() {
        currentlyPlayerStatus = new HashMap<>();
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
            // The user started playing a new track
            status = new PlayerStatus(track, startDate, duration);
            currentlyPlayerStatus.put(userId, status);

            // Dispatch a new play started event
            PlayStartedEvent event = new PlayStartedEvent(userId, track);
            AppContext.getInstance().getLastFmEventBus().post(event);
        } else {
            status.setDuration(duration);
        }

        if (!status.isCommited() && duration >= track.getLength() / 2) {
            status.setCommited(true);

            // Dispatch a new play completed event
            PlayCompletedEvent event = new PlayCompletedEvent(userId, track);
            AppContext.getInstance().getLastFmEventBus().post(event);
        }
    }
}
