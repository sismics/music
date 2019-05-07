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
import com.sismics.music.core.event.async.LastFmUpdateTrackPlayCountAsyncEvent;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.dbi.Artist;
import com.sismics.music.core.model.dbi.Track;
import com.sismics.music.core.model.dbi.User;
import com.sismics.music.core.util.ConfigUtil;
import com.sismics.music.core.util.TransactionUtil;
import com.sismics.util.lastfm.LastFmUtil;
import de.umass.lastfm.*;
import de.umass.lastfm.scrobble.ScrobbleData;
import de.umass.lastfm.scrobble.ScrobbleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.*;
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

    @Override
    protected void runOneIteration() throws Exception {
        TransactionUtil.handle(() -> {
            UserDao userDao = new UserDao();
            List<UserDto> userList = userDao.findByCriteria(new UserCriteria().setLastFmSessionTokenNotNull(true));
            for (UserDto userDto : userList) {
                User user = userDao.getActiveById(userDto.getId());
                AppContext.getInstance().getLastFmEventBus().post(new LastFmUpdateLovedTrackAsyncEvent(user));
                AppContext.getInstance().getLastFmEventBus().post(new LastFmUpdateTrackPlayCountAsyncEvent(user));
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

        return Authenticator.getMobileSession(lastFmUsername, lastFmPassword, key, secret);
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
     * @param user User
     * @param track Track now playing
     */
    public void nowPlayingTrack(User user, Track track) {
        Session session = restoreSession(user);

        int now = (int) (System.currentTimeMillis() / 1000);
        final Artist artist = new ArtistDao().getActiveById(track.getArtistId());
        ScrobbleData scrobbleData = new ScrobbleData(artist.getName(), track.getTitle(), now);
        scrobbleData.setDuration(track.getLength());

        ScrobbleResult result = de.umass.lastfm.Track.updateNowPlaying(scrobbleData, session);
        updateLocalData(result, track, artist);
        log.info(MessageFormat.format("Update now playing for user {0}: {1}", user.getId(), result.toString()));
    }

    /**
     * Scrobble a track.
     *
     * @param user User
     * @param track Track to scrobble
     */
    public void scrobbleTrack(User user, Track track) {
        Session session = restoreSession(user);

        int now = (int) (System.currentTimeMillis() / 1000);
        final Artist artist = new ArtistDao().getActiveById(track.getArtistId());
        ScrobbleData scrobbleData = new ScrobbleData(artist.getName(), track.getTitle(), now);
        scrobbleData.setDuration(track.getLength());

        ScrobbleResult result = de.umass.lastfm.Track.scrobble(scrobbleData, session);
        updateLocalData(result, track, artist);
        log.info(MessageFormat.format("Play completed for user {0}: {1}", user.getId(), result.toString()));
    }

    /**
     * Scrobble a list of track.
     *
     * @param user User
     * @param trackList Tracks to scrobble
     * @param dateList Dates the tracks were played
     */
    public void scrobbleTrackList(User user, List<Track> trackList, List<Date> dateList) {
        Session session = restoreSession(user);

        final ArtistDao artistDao = new ArtistDao();
        List<ScrobbleData> scrobbleDataList = new ArrayList<>();
        for (int i = 0; i < trackList.size(); i++) {
            final Track track = trackList.get(i);
            final Artist artist = artistDao.getActiveById(track.getArtistId());
            ScrobbleData scrobbleData = new ScrobbleData(artist.getName(), track.getTitle(), (int) (dateList.get(i).getTime() / 1000));
            scrobbleDataList.add(scrobbleData);
        }
        de.umass.lastfm.Track.scrobble(scrobbleDataList, session);
        // TODO Update local data
        log.info(MessageFormat.format("Scrobbled a list of tracks for user {0}", user.getId()));
    }

    /**
     * Love a track.
     *
     * @param user User
     * @param track Track to love
     */
    public void loveTrack(User user, Track track) {
        Session session = restoreSession(user);

        final Artist artist = new ArtistDao().getActiveById(track.getArtistId());
        Result result = de.umass.lastfm.Track.love(
                artist.getNameCorrected() == null ? artist.getName() : artist.getNameCorrected(),
                track.getTitleCorrected() == null ? track.getTitle() : track.getTitleCorrected(),
                session);
        log.info(MessageFormat.format("Loved a track for user {0}: {1}", user.getId(), result.toString()));
    }

    /**
     * Unlove a track.
     *
     * @param user User
     * @param track Track to unlove
     */
    public void unloveTrack(User user, Track track) {
        Session session = restoreSession(user);

        final Artist artist = new ArtistDao().getActiveById(track.getArtistId());
        Result result = de.umass.lastfm.Track.unlove(
                artist.getNameCorrected() == null ? artist.getName() : artist.getNameCorrected(),
                track.getTitleCorrected() == null ? track.getTitle() : track.getTitleCorrected(),
                session);
        log.info(MessageFormat.format("Unloved a track for user {0}: {1}", user.getId(), result.toString()));
    }
    
    /**
     * Update local data from Last.fm corrected labels.
     * 
     * @param result Scrobble result
     * @param track Track
     * @param artist Artist
     */
    private void updateLocalData(ScrobbleResult result, Track track, Artist artist) {
        TrackDao trackDao = new TrackDao();
        ArtistDao artistDao = new ArtistDao();
        
        if (result.isTrackCorrected()) {
            track.setTitleCorrected(result.getTrack());
            trackDao.update(track);
        }
        
        if (result.isArtistCorrected()) {
            artist.setNameCorrected(result.getArtist());
            artistDao.update(artist);
        }
    }

    /**
     * Import all track play counts from Last.fm.
     *
     * @param user User
     */
    public void importTrackPlayCount(User user) {
        Session session = restoreSession(user);
        de.umass.lastfm.User lastFmUser = de.umass.lastfm.User.getInfo(session);

        UserTrackDao userTrackDao = new UserTrackDao();
        int page = 1;
        int lastFmCount = 0;
        int localCount = 0;
        PaginatedResult<de.umass.lastfm.Track> result;
        do {
            log.info(MessageFormat.format("Getting page {0} of Last.fm user library", page - 1));
            result = LastFmUtil.getAllTracks(lastFmUser.getName(), page, 1000, session.getApiKey());
            TrackDao trackDao = new TrackDao();
            for (Iterator<de.umass.lastfm.Track> it = result.iterator(); it.hasNext();) {
                lastFmCount++;
                de.umass.lastfm.Track lastFmTrack = it.next();
                log.debug(MessageFormat.format("  Last.fm track name={0} artist={1}", lastFmTrack.getName(), lastFmTrack.getArtist()));
                for (TrackDto track : trackDao.findByCriteria(new TrackCriteria()
                        .setTitle(lastFmTrack.getName())
                        .setArtistName(lastFmTrack.getArtist())
                       )) {
                    log.debug(MessageFormat.format("    Found match in local collection title={0} artistName={1}", track.getTitle(), track.getArtistName()));
                    userTrackDao.initPlayCount(user.getId(), track.getId(), lastFmTrack.getPlaycount()); // Playcount is actually the user play count
                    localCount++;
                }
            }
            page++;
        } while (result != null && page <= result.getTotalPages());

        log.info(MessageFormat.format("Retrieved {0} tracks from Last.fm, updated {1} play counts in the local collection", lastFmCount, localCount));
    }

    /**
     * Import all loved tracks from Last.fm.
     *
     * @param user User
     */
    public void importLovedTrack(User user) {
        Session session = restoreSession(user);
        de.umass.lastfm.User lastFmUser = de.umass.lastfm.User.getInfo(session);

        UserTrackDao userTrackDao = new UserTrackDao();
        int page = 1;
        int lastFmCount = 0;
        int localCount = 0;
        PaginatedResult<de.umass.lastfm.Track> result = null;
        do {
            // XXX implement rate limitation, should be good around 1000*10 = 10k faves for now
            // TODO implement a more permissive track search on local database, if Last.fm corrected the title/artist
            log.info(MessageFormat.format("Getting page {0} of Last.fm loved tracked", page - 1));
            result = LastFmUtil.getLovedTracks(lastFmUser.getName(), page, 1000, session.getApiKey());
            TrackDao trackDao = new TrackDao();
            for (Iterator<de.umass.lastfm.Track> it = result.iterator(); it.hasNext();) {
                lastFmCount++;
                de.umass.lastfm.Track lastFmTrack = it.next();
                log.debug(MessageFormat.format("  Last.fm loved track name={0} artist={1}", lastFmTrack.getName(), lastFmTrack.getArtist()));
                for (TrackDto track : trackDao.findByCriteria(new TrackCriteria()
                        .setTitle(lastFmTrack.getName())
                        .setArtistName(lastFmTrack.getArtist())
                       )) {
                    log.debug(MessageFormat.format("    Found match in local collection title={0} artistName={1}", track.getTitle(), track.getArtistName()));
                    userTrackDao.like(user.getId(), track.getId());
                    localCount++;
                }
            }
            page++;
        } while (result != null && page <= result.getTotalPages());

        log.info(MessageFormat.format("Retrieved {0} loved tracks from Last.fm, imported {1} into the local collection", lastFmCount, localCount));
    }

    /**
     * Search album arts.
     *
     * @param query The query
     * @return The albums
     */
    public Collection<Album> searchAlbumArt(String query) {
        return Album.search(query, ConfigUtil.getConfigStringValue(ConfigType.LAST_FM_API_KEY));
    }
}
