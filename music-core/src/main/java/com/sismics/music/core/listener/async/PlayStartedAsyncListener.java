package com.sismics.music.core.listener.async;

import com.google.common.eventbus.Subscribe;
import com.sismics.music.core.dao.dbi.UserDao;
import com.sismics.music.core.event.async.PlayStartedEvent;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.dbi.Track;
import com.sismics.music.core.model.dbi.User;
import com.sismics.music.core.service.lastfm.LastFmService;
import com.sismics.music.core.util.TransactionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Play started listener.
 *
 * @author jtremeaux
 */
public class PlayStartedAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(PlayStartedAsyncListener.class);

    /**
     * Process the event.
     *
     * @param playStartedEvent Play started event
     */
    @Subscribe
    public void onPlayStarted(final PlayStartedEvent playStartedEvent) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("Play started event: " + playStartedEvent.toString());
        }

        final String userId = playStartedEvent.getUserId();
        final Track track = playStartedEvent.getTrack();

        TransactionUtil.handle(() -> {
            final User user = new UserDao().getActiveById(userId);
            if (user != null && user.getLastFmSessionToken() != null) {
                final LastFmService lastFmService = AppContext.getInstance().getLastFmService();
                lastFmService.nowPlayingTrack(user, track);
            }
        });
    }
}
