package com.sismics.music.core.listener.async;

import com.google.common.eventbus.Subscribe;
import com.sismics.music.core.dao.jpa.UserDao;
import com.sismics.music.core.event.async.PlayStartedEvent;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.jpa.Track;
import com.sismics.music.core.model.jpa.User;
import com.sismics.music.core.service.lastfm.LastFmService;
import com.sismics.music.core.util.TransactionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Play completed listener.
 *
 * @author jtremeaux
 */
public class PlayCompletedAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(PlayCompletedAsyncListener.class);

    /**
     * Process the event.
     *
     * @param playStartedEvent Play completed event
     * @throws Exception
     */
    @Subscribe
    public void onDirectoryCreated(final PlayStartedEvent playStartedEvent) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("Play completed event: " + playStartedEvent.toString());
        }

        final String userId = playStartedEvent.getUserId();
        final Track track = playStartedEvent.getTrack();

        TransactionUtil.handle(new Runnable() {
            @Override
            public void run() {
                final User user = new UserDao().getActiveById(userId);
                if (user != null && user.getLastFmSessionToken() != null) {
                    final LastFmService lastFmService = AppContext.getInstance().getLastFmService();
                    lastFmService.scrobbleTrack(user, track);
                }
            }
        });
    }
}
