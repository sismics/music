package com.sismics.music.core.listener.async;

import com.google.common.base.Stopwatch;
import com.google.common.eventbus.Subscribe;
import com.sismics.music.core.event.async.TrackLikedAsyncEvent;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.dbi.Track;
import com.sismics.music.core.model.dbi.User;
import com.sismics.music.core.service.lastfm.LastFmService;
import com.sismics.music.core.util.TransactionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * Track liked listener.
 *
 * @author jtremeaux
 */
public class TrackLikedAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(TrackLikedAsyncListener.class);

    /**
     * Process the event.
     *
     * @param trackLikedAsyncEvent New directory created event
     */
    @Subscribe
    public void onTrackLiked(final TrackLikedAsyncEvent trackLikedAsyncEvent) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("Track liked event: " + trackLikedAsyncEvent.toString());
        }
        Stopwatch stopwatch = Stopwatch.createStarted();

        final User user = trackLikedAsyncEvent.getUser();
        final Track track = trackLikedAsyncEvent.getTrack();

        TransactionUtil.handle(() -> {
        if (user.getLastFmSessionToken() != null) {
            final LastFmService lastFmService = AppContext.getInstance().getLastFmService();
            lastFmService.loveTrack(user, track);
        }
        });

        if (log.isInfoEnabled()) {
            log.info(MessageFormat.format("Track liked completed in {0}", stopwatch));
        }
    }
}
