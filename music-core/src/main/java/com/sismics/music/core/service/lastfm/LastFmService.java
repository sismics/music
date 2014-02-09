package com.sismics.music.core.service.lastfm;

import com.sismics.music.core.constant.ConfigType;
import com.sismics.music.core.dao.jpa.ArtistDao;
import com.sismics.music.core.model.jpa.Artist;
import com.sismics.music.core.model.jpa.Track;
import com.sismics.music.core.model.jpa.User;
import com.sismics.music.core.util.ConfigUtil;
import de.umass.lastfm.Authenticator;
import de.umass.lastfm.Result;
import de.umass.lastfm.Session;
import de.umass.lastfm.scrobble.ScrobbleData;
import de.umass.lastfm.scrobble.ScrobbleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Last.fm service.
 *
 * @author jtremeaux
 */
public class LastFmService {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(LastFmService.class);

    public LastFmService() {
    }

    /**
     * Create a new user session.
     *
     * @param lastFmUsername User name
     * @param lastFmPassword Password
     * @return Last.fm session
     */
    public Session createSession(String lastFmUsername, String lastFmPassword) {
        String key = ConfigUtil.getConfigStringValue(ConfigType.LAST_FM_API_KEY);
        String secret = ConfigUtil.getConfigStringValue(ConfigType.LAST_FM_API_SECRET);
        Session session = Authenticator.getMobileSession(lastFmUsername, lastFmPassword, key, secret);
        return session;
    }

    /**
     * Restore the session from the stored session token.
     *
     * @param user User
     * @return Last.fm session
     */
    public Session restoreSession(User user) {
        String key = ConfigUtil.getConfigStringValue(ConfigType.LAST_FM_API_KEY);
        String secret = ConfigUtil.getConfigStringValue(ConfigType.LAST_FM_API_SECRET);
        return Session.createSession(key, secret, user.getLastFmSessionToken());
    }

    /**
     * Update track now playing.
     *
     * @param track Track to nowPlayingTrack
     */
    public void nowPlayingTrack(User user, Track track) {
        Session session = restoreSession(user);

        int now = (int) (System.currentTimeMillis() / 1000);
        final Artist artist = new ArtistDao().getActiveById(track.getArtistId());
        ScrobbleData scrobbleData = new ScrobbleData(artist.getName(), track.getTitle(), now);
        scrobbleData.setDuration(track.getLength());

        ScrobbleResult result = de.umass.lastfm.Track.updateNowPlaying(scrobbleData, session);
        log.info("Update now playing for user {0}: {1}", user.getId(), result.toString());
    }

    /**
     * Scrobble a track.
     *
     * @param track Track to nowPlayingTrack
     */
    public void scrobbleTrack(User user, Track track) {
        Session session = restoreSession(user);

        int now = (int) (System.currentTimeMillis() / 1000);
        final Artist artist = new ArtistDao().getActiveById(track.getArtistId());
        ScrobbleData scrobbleData = new ScrobbleData(artist.getName(), track.getTitle(), now);
        scrobbleData.setDuration(track.getLength());

        ScrobbleResult result = de.umass.lastfm.Track.scrobble(scrobbleData, session);
        log.info("Play completed for user {0}: {1}", user.getId(), result.toString());
    }

    /**
     * Love a track.
     *
     * @param track Track to nowPlayingTrack
     */
    public void loveTrack(User user, Track track) {
        Session session = restoreSession(user);

        final Artist artist = new ArtistDao().getActiveById(track.getArtistId());
        Result result = de.umass.lastfm.Track.love(artist.getName(), track.getTitle(), session);
        log.info("Loved a track for user {0}: {1}", user.getId(), result.toString());
    }
    /**
     * Unlove a track.
     *
     * @param track Track to nowPlayingTrack
     */
    public void unloveTrack(User user, Track track) {
        Session session = restoreSession(user);

        final Artist artist = new ArtistDao().getActiveById(track.getArtistId());
        Result result = de.umass.lastfm.Track.unlove(artist.getName(), track.getTitle(), session);
        log.info("Unloved a track for user {0}: {1}", user.getId(), result.toString());
    }
}
