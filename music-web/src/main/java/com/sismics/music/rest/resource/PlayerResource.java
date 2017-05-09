package com.sismics.music.rest.resource;

import com.sismics.music.core.dao.dbi.PlayerDao;
import com.sismics.music.core.dao.dbi.TrackDao;
import com.sismics.music.core.dao.dbi.UserDao;
import com.sismics.music.core.dao.dbi.UserTrackDao;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.dbi.Player;
import com.sismics.music.core.model.dbi.Track;
import com.sismics.music.core.model.dbi.User;
import com.sismics.music.core.service.lastfm.LastFmService;
import com.sismics.music.core.service.player.PlayerService;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.Validation;
import com.sismics.util.MathUtil;

import javax.json.Json;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Player REST resources.
 * 
 * @author jtremeaux
 */
@Path("/player")
public class PlayerResource extends BaseResource {
    /**
     * Signals Music that a track is being played. A track is considered playing if it is played more than halfway.
     *
     * @param id Track ID
     * @param dateStr Date the track was started playing.
     * @param duration Duration into the track in seconds
     * @return Response
     */
    @POST
    @Path("listening")
    public Response listening(
            @FormParam("id") String id,
            @FormParam("date") String dateStr,
            @FormParam("duration") Integer duration) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        Date date = Validation.date(dateStr, "date", false);
        Validation.required(duration, "duration");

        // Load the track
        TrackDao trackDao = new TrackDao();
        Track track = trackDao.getActiveById(id);
        if (track == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        duration = MathUtil.clip(duration, 0, track.getLength());

        // Update currently playing track
        final PlayerService playerService = AppContext.getInstance().getPlayerService();
        playerService.notifyPlaying(principal.getId(), track, date, duration);

        // Always return OK
        return okJson();
    }

    /**
     * Post a set of tracks played before (useful for offline mode).
     *
     * @param idList An array of track ID
     * @param dateStrList An array of dates at which the track was started playing
     * @return Response
     */
    @POST
    @Path("listened")
    public Response listened(
            @FormParam("id") List<String> idList,
            @FormParam("date") List<String> dateStrList) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        if (idList == null || dateStrList == null || idList.size() != dateStrList.size()) {
            throw new ClientException("ValidationError", "Invalid id or dates");
        }

        // Scrobble tracks on Last.fm
        final TrackDao trackDao = new TrackDao();
        final UserTrackDao userTrackDao = new UserTrackDao();
        List<Track> trackList = new ArrayList<>();
        List<Date> dateList = new ArrayList<>();
        for (int i = 0; i < idList.size(); i++) {
            Track track = trackDao.getActiveById(idList.get(i));
            if (track != null) {
                Date date = Validation.date(dateStrList.get(i), "date", false);
                trackList.add(track);
                dateList.add(date);
                
                // Increment local play count
                userTrackDao.incrementPlayCount(principal.getId(), track.getId());
            }
        }
        final User user = new UserDao().getActiveById(principal.getId());
        if (user != null && user.getLastFmSessionToken() != null) {
            final LastFmService lastFmService = AppContext.getInstance().getLastFmService();
            lastFmService.scrobbleTrackList(user, trackList, dateList);
        }

        // Always return OK
        return okJson();
    }
    
    /**
     * Register a new player.
     * 
     * @return Response
     */
    @POST
    @Path("register")
    public Response register() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Create a new player
        PlayerDao playerDao = new PlayerDao();
        Player player = new Player();
        String id = playerDao.create(player);
        
        // Return the token
        return renderJson(Json.createObjectBuilder().add("token", id));
    }
    
    /**
     * Unregister a player.
     * 
     * @return Response
     */
    @POST
    @Path("unregister")
    public Response unregister(
            @FormParam("token") String token) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        Validation.required(token, "token");
        
        // Delete the player
        PlayerDao playerDao = new PlayerDao();
        Player player = playerDao.getById(token);
        if (player == null) {
            throw new ClientException("PlayerNotFound", "Player not found: " + token);
        }
        playerDao.delete(token);
        
        // Always return OK
        return okJson();
    }
}
