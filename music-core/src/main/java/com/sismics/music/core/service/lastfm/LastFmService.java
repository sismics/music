package com.sismics.music.core.service.lastfm;

import com.sismics.music.core.constant.ConfigType;
import com.sismics.music.core.dao.dbi.ArtistDao;
import com.sismics.music.core.model.dbi.Artist;
import com.sismics.music.core.model.dbi.Track;
import com.sismics.music.core.model.dbi.User;
import com.sismics.music.core.util.ConfigUtil;
import de.umass.lastfm.Authenticator;
import de.umass.lastfm.Result;
import de.umass.lastfm.Session;
import de.umass.lastfm.scrobble.ScrobbleData;
import de.umass.lastfm.scrobble.ScrobbleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
     * Returns the Last.fm information.
     *
     * @param user User
     * @return Last.fm information
     */
    public de.umass.lastfm.User getInfo(User user) {
        Session session = restoreSession(user);

        return de.umass.lastfm.User.getInfo(session);
    }

    /**
     * Update track now playing.
     *
     * @param track Track now playing
     */
    public void nowPlayingTrack(User user, Track track) {
        Session session = restoreSession(user);

        int now = (int) (System.currentTimeMillis() / 1000);
        final Artist artist = new ArtistDao().getActiveById(track.getArtistId());
        ScrobbleData scrobbleData = new ScrobbleData(artist.getName(), track.getTitle(), now);
        scrobbleData.setDuration(track.getLength());

        ScrobbleResult result = de.umass.lastfm.Track.updateNowPlaying(scrobbleData, session);
        log.info(MessageFormat.format("Update now playing for user {0}: {1}", user.getId(), result.toString()));
    }

    /**
     * Scrobble a track.
     *
     * @param track Track to scrobble
     */
    public void scrobbleTrack(User user, Track track) {
        Session session = restoreSession(user);

        int now = (int) (System.currentTimeMillis() / 1000);
        final Artist artist = new ArtistDao().getActiveById(track.getArtistId());
        ScrobbleData scrobbleData = new ScrobbleData(artist.getName(), track.getTitle(), now);
        scrobbleData.setDuration(track.getLength());

        ScrobbleResult result = de.umass.lastfm.Track.scrobble(scrobbleData, session);
        log.info(MessageFormat.format("Play completed for user {0}: {1}", user.getId(), result.toString()));
    }

    /**
     * Scrobble a list of track.
     *
     * @param trackList Tracks to scrobble
     * @param dateList Dates the tracks were played
     */
    public void scrobbleTrackList(User user, List<Track> trackList, List<Date> dateList) {
        Session session = restoreSession(user);

        final ArtistDao artistDao = new ArtistDao();
        List<ScrobbleData> scrobbleDataList = new ArrayList<ScrobbleData>();
        for (int i = 0; i < trackList.size(); i++) {
            final Track track = trackList.get(i);
            final Artist artist = artistDao.getActiveById(track.getArtistId());
            ScrobbleData scrobbleData = new ScrobbleData(artist.getName(), track.getTitle(), (int) dateList.get(i).getTime());
            scrobbleDataList.add(scrobbleData);
        }
        List<ScrobbleResult> result = de.umass.lastfm.Track.scrobble(scrobbleDataList, session);
        log.info(MessageFormat.format("Scrobbled a list of tracks for user {0}", user.getId()));
    }

    /**
     * Love a track.
     *
     * @param track Track to love
     */
    public void loveTrack(User user, Track track) {
        Session session = restoreSession(user);

        final Artist artist = new ArtistDao().getActiveById(track.getArtistId());
        Result result = de.umass.lastfm.Track.love(artist.getName(), track.getTitle(), session);
        log.info(MessageFormat.format("Loved a track for user {0}: {1}", user.getId(), result.toString()));
    }

    /**
     * Unlove a track.
     *
     * @param track Track to unlove
     */
    public void unloveTrack(User user, Track track) {
        Session session = restoreSession(user);

        final Artist artist = new ArtistDao().getActiveById(track.getArtistId());
        Result result = de.umass.lastfm.Track.unlove(artist.getName(), track.getTitle(), session);
        log.info(MessageFormat.format("Unloved a track for user {0}: {1}", user.getId(), result.toString()));
    }
}
