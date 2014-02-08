package com.sismics.music.core.listener.async;

import com.google.common.eventbus.Subscribe;
import com.sismics.music.core.constant.ConfigType;
import com.sismics.music.core.dao.jpa.ArtistDao;
import com.sismics.music.core.dao.jpa.UserDao;
import com.sismics.music.core.event.async.PlayStartedEvent;
import com.sismics.music.core.model.jpa.Artist;
import com.sismics.music.core.model.jpa.Track;
import com.sismics.music.core.model.jpa.User;
import com.sismics.music.core.util.ConfigUtil;
import com.sismics.music.core.util.TransactionUtil;
import de.umass.lastfm.Session;
import de.umass.lastfm.scrobble.ScrobbleResult;
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
        final Artist artist = new ArtistDao().getActiveById(track.getArtistId());

        TransactionUtil.handle(new Runnable() {
            @Override
            public void run() {
                final User user = new UserDao().getActiveById(userId);
                if (user != null && user.getLastFmSessionToken() != null) {
                    String key = ConfigUtil.getConfigStringValue(ConfigType.LAST_FM_API_KEY);
                    String secret = ConfigUtil.getConfigStringValue(ConfigType.LAST_FM_API_SECRET);
                    Session session = Session.createSession(key, secret, user.getLastFmSessionToken());

                    int now = (int) (System.currentTimeMillis() / 1000);
                    ScrobbleResult result = de.umass.lastfm.Track.scrobble(artist.getName(), track.getTitle(), now, session);
                    log.info("Play completed for user {0}: {1}", userId, result.toString());
                }
            }
        });
    }
}
