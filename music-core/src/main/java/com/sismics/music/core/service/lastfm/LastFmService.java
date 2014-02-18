package com.sismics.music.core.service.lastfm;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.sismics.music.core.constant.ConfigType;
import com.sismics.music.core.dao.dbi.ArtistDao;
import com.sismics.music.core.dao.dbi.TrackDao;
import com.sismics.music.core.dao.dbi.UserDao;
import com.sismics.music.core.dao.dbi.UserTrackDao;
import com.sismics.music.core.dao.dbi.criteria.TrackCriteria;
import com.sismics.music.core.dao.dbi.criteria.UserCriteria;
import com.sismics.music.core.dao.dbi.dto.TrackDto;
import com.sismics.music.core.dao.dbi.dto.UserDto;
import com.sismics.music.core.event.async.LastFmUpdateLovedTrackAsyncEvent;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.dbi.Artist;
import com.sismics.music.core.model.dbi.Track;
import com.sismics.music.core.model.dbi.User;
import com.sismics.music.core.util.ConfigUtil;
import com.sismics.music.core.util.TransactionUtil;
import com.sismics.util.LastFmUtil;
import de.umass.lastfm.Authenticator;
import de.umass.lastfm.PaginatedResult;
import de.umass.lastfm.Result;
import de.umass.lastfm.Session;
import de.umass.lastfm.scrobble.ScrobbleData;
import de.umass.lastfm.scrobble.ScrobbleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Last.fm service.
 *
 * @author jtremeaux
 */
public class LastFmService extends AbstractScheduledService {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(LastFmService.class);

    public LastFmService() {
    }

    @Override
    protected void startUp() {
    }

    @Override
    protected void shutDown() {
    }

    @Override
    protected void runOneIteration() throws Exception {
        TransactionUtil.handle(new Runnable() {
            @Override
            public void run() {
                UserDao userDao = new UserDao();
                List<UserDto> userList = userDao.findByCriteria(new UserCriteria().setLastFmSessionTokenNotNull(true));
                for (UserDto userDto : userList) {
                    User user = userDao.getActiveById(userDto.getId());
                    AppContext.getInstance().getLastFmEventBus().post(new LastFmUpdateLovedTrackAsyncEvent(user));
                }
            }
        });
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(23, 24, TimeUnit.HOURS);
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

    /**
     * Import all loved tracks from Last.fm.
     *
     * @param user Track to love
     */
    public void updateLovedTrack(User user) {
        Session session = restoreSession(user);
        de.umass.lastfm.User lastFmUser = de.umass.lastfm.User.getInfo(session);

        UserTrackDao userTrackDao = new UserTrackDao();
        TrackDao trackDao = new TrackDao();
        userTrackDao.unlikeAll(user.getId());
        int page = 1;
        int count = 0;
        PaginatedResult<de.umass.lastfm.Track> result = null;
        do {
            // TODO implement rate limitation, should be good around 1000*10 = 10k faves for now
            // TODO check result, don't commit if Last.fm reports an error (current faves will be lost!)
            result = LastFmUtil.getLovedTracks(lastFmUser.getName(), page, 1000, session.getApiKey());

            for (Iterator<de.umass.lastfm.Track> it = result.iterator(); it.hasNext();) {
                count++;
                de.umass.lastfm.Track lastFmTrack = it.next();
                for (TrackDto track : trackDao.findByCriteria(new TrackCriteria()
                        .setTitle(lastFmTrack.getName())
                        .setArtistName(lastFmTrack.getArtist())
                       )) {
                    userTrackDao.like(user.getId(), track.getId());
                }
            }
            page++;
        } while (result != null && page <= result.getTotalPages());

        log.info(MessageFormat.format("Imported {0} loved tracks from Last.fm", count));
    }
}
