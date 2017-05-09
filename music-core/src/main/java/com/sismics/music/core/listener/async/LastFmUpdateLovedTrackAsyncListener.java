package com.sismics.music.core.listener.async;

import com.google.common.base.Stopwatch;
import com.google.common.eventbus.Subscribe;
import com.sismics.music.core.event.async.LastFmUpdateLovedTrackAsyncEvent;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.dbi.User;
import com.sismics.music.core.service.lastfm.LastFmService;
import com.sismics.music.core.util.TransactionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * Last.fm update loved tracks listener.
 *
 * @author jtremeaux
 */
public class LastFmUpdateLovedTrackAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(LastFmUpdateLovedTrackAsyncListener.class);

    /**
     * Process the event.
     *
     * @param lastFmUpdateLovedTrackAsyncEvent Update loved track event
     */
    @Subscribe
    public void onLastFmUpdateLovedTrack(final LastFmUpdateLovedTrackAsyncEvent lastFmUpdateLovedTrackAsyncEvent) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("Last.fm update loved track event: " + lastFmUpdateLovedTrackAsyncEvent.toString());
        }
        Stopwatch stopwatch = Stopwatch.createStarted();

        final User user = lastFmUpdateLovedTrackAsyncEvent.getUser();

        TransactionUtil.handle(() -> {
            final LastFmService lastFmService = AppContext.getInstance().getLastFmService();
            lastFmService.importLovedTrack(user);
        });

        if (log.isInfoEnabled()) {
            log.info(MessageFormat.format("Last.fm update loved track event completed in {0}", stopwatch));
        }
    }
}
