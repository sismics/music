package com.sismics.music.core.listener.async;

import com.google.common.base.Stopwatch;
import com.google.common.eventbus.Subscribe;
import com.sismics.music.core.event.async.LastFmUpdateTrackPlayCountAsyncEvent;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.dbi.User;
import com.sismics.music.core.service.lastfm.LastFmService;
import com.sismics.music.core.util.TransactionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * Last.fm registered listener.
 *
 * @author jtremeaux
 */
public class LastFmUpdateTrackPlayCountAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(LastFmUpdateTrackPlayCountAsyncListener.class);

    /**
     * Process the event.
     *
     * @param lastFmUpdateTrackPlayCountAsyncEvent Update track play count event
     */
    @Subscribe
    public void onLastFmUpdateTrackPlayCount(final LastFmUpdateTrackPlayCountAsyncEvent lastFmUpdateTrackPlayCountAsyncEvent) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("Last.fm update track play count event: " + lastFmUpdateTrackPlayCountAsyncEvent.toString());
        }
        Stopwatch stopwatch = Stopwatch.createStarted();

        final User user = lastFmUpdateTrackPlayCountAsyncEvent.getUser();

        TransactionUtil.handle(() -> {
            final LastFmService lastFmService = AppContext.getInstance().getLastFmService();
            lastFmService.importTrackPlayCount(user);
        });

        if (log.isInfoEnabled()) {
            log.info(MessageFormat.format("Last.fm update track play count event completed in {0}", stopwatch));
        }
    }
}
