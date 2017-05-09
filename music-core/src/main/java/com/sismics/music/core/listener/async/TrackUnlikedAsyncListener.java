package com.sismics.music.core.listener.async;

import com.google.common.base.Stopwatch;
import com.google.common.eventbus.Subscribe;
import com.sismics.music.core.event.async.TrackUnlikedAsyncEvent;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.dbi.Track;
import com.sismics.music.core.model.dbi.User;
import com.sismics.music.core.service.lastfm.LastFmService;
import com.sismics.music.core.util.TransactionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * Track unliked listener.
 *
 * @author jtremeaux
 */
public class TrackUnlikedAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(TrackUnlikedAsyncListener.class);

    /**
     * Process the event.
     *
     * @param trackUnlikedAsyncEvent New directory created event
     */
    @Subscribe
    public void onTrackLiked(final TrackUnlikedAsyncEvent trackUnlikedAsyncEvent) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("Track unliked event: " + trackUnlikedAsyncEvent.toString());
        }
        Stopwatch stopwatch = Stopwatch.createStarted();

        final User user = trackUnlikedAsyncEvent.getUser();
        final Track track = trackUnlikedAsyncEvent.getTrack();

        TransactionUtil.handle(() -> {
        if (user.getLastFmSessionToken() != null) {
            final LastFmService lastFmService = AppContext.getInstance().getLastFmService();
            lastFmService.unloveTrack(user, track);
        }
        });

        if (log.isInfoEnabled()) {
            log.info(MessageFormat.format("Track unliked completed in {0}", stopwatch));
        }
    }
}
