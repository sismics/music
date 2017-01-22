package com.sismics.music.rest.resource;

import com.sismics.music.core.dao.dbi.PlaylistDao;
import com.sismics.music.core.dao.dbi.PlaylistTrackDao;
import com.sismics.music.core.dao.dbi.TrackDao;
import com.sismics.music.core.dao.dbi.criteria.TrackCriteria;
import com.sismics.music.core.dao.dbi.dto.PlaylistDto;
import com.sismics.music.core.dao.dbi.dto.TrackDto;
import com.sismics.music.core.model.dbi.Track;
import com.sismics.music.core.util.dbi.PaginatedList;
import com.sismics.music.core.util.dbi.PaginatedLists;
import com.sismics.music.rest.util.JsonUtil;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.text.MessageFormat;
import java.util.List;

/**
 * Playlist REST resources.
 * 
 * @author jtremeaux
 */
@Path("/playlist")
public class PlaylistResource extends BaseResource {
    /**
     * Inserts a track in the playlist.
     *
     * @param id Track ID
     * @param order Insert at this order in the playlist
     * @param clear If true, clear the playlist
     * @return Response
     */
    @PUT
    public Response insertTrack(
            @FormParam("id") String id,
            @FormParam("order") Integer order,
            @FormParam("clear") Boolean clear) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Load the track
        TrackDao trackDao = new TrackDao();
        Track track = trackDao.getActiveById(id);
        if (track == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Get the playlist
        PlaylistDao playlistDao = new PlaylistDao();
        PlaylistDto playlist = playlistDao.getDefaultPlaylistByUserId(principal.getId());
        if (playlist == null) {
            throw new ServerException("UnknownError", MessageFormat.format("Playlist not found for user {0}", principal.getId()));
        }
        PlaylistTrackDao playlistTrackDao = new PlaylistTrackDao();

        if (clear != null && clear) {
            // Delete all tracks in the playlist
            playlistTrackDao.deleteByPlaylistId(playlist.getId());
        }
        
        // Get the track order
        if (order == null) {
            order = playlistTrackDao.getPlaylistTrackNextOrder(playlist.getId());
        }

        // Insert the track into the playlist
        playlistTrackDao.insertPlaylistTrack(playlist.getId(), track.getId(), order);

        // Output the playlist
        return Response.ok()
                .entity(buildPlaylistJson(playlist))
                .build();
    }
    
    /**
     * Inserts tracks in the playlist.
     *
     * @param idList List of track ID
     * @param clear If true, clear the playlist
     * @return Response
     */
    @PUT
    @Path("multiple")
    public Response insertTracks(
            @FormParam("ids") List<String> idList,
            @FormParam("clear") Boolean clear) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        ValidationUtil.validateRequired(idList, "ids");
        
        // Get the playlist
        PlaylistDao playlistDao = new PlaylistDao();
        PlaylistDto playlist = playlistDao.getDefaultPlaylistByUserId(principal.getId());
        if (playlist == null) {
            throw new ServerException("UnknownError", MessageFormat.format("Playlist not found for user {0}", principal.getId()));
        }
        PlaylistTrackDao playlistTrackDao = new PlaylistTrackDao();
        
        if (clear != null && clear) {
            // Delete all tracks in the playlist
            playlistTrackDao.deleteByPlaylistId(playlist.getId());
        }
        
        // Get the track order
        int order = playlistTrackDao.getPlaylistTrackNextOrder(playlist.getId());
        
        for (String id : idList) {
            // Load the track
            TrackDao trackDao = new TrackDao();
            Track track = trackDao.getActiveById(id);
            if (track == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            
            // Insert the track into the playlist
            playlistTrackDao.insertPlaylistTrack(playlist.getId(), track.getId(), order++);
        }

        // Output the playlist
        return Response.ok()
                .entity(buildPlaylistJson(playlist))
                .build();
    }
    
    /**
     * Start or continue party mode.
     * Adds some good tracks.
     * 
     * @param clear If true, clear the playlist
     * @return Response
     */
    @POST
    @Path("party")
    public Response party(@FormParam("clear") Boolean clear) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get the playlist
        PlaylistDao playlistDao = new PlaylistDao();
        PlaylistDto playlist = playlistDao.getDefaultPlaylistByUserId(principal.getId());
        if (playlist == null) {
            throw new ServerException("UnknownError", MessageFormat.format("Playlist not found for user {0}", principal.getId()));
        }
        PlaylistTrackDao playlistTrackDao = new PlaylistTrackDao();
        
        if (clear != null && clear) {
            // Delete all tracks in the playlist
            playlistTrackDao.deleteByPlaylistId(playlist.getId());
        }
        
        // Get the track order
        int order = playlistTrackDao.getPlaylistTrackNextOrder(playlist.getId());
        
        // TODO Add prefered tracks
        // Add random tracks
        TrackDao trackDao = new TrackDao();
        PaginatedList<TrackDto> paginatedList = PaginatedLists.create();
        trackDao.findByCriteria(new TrackCriteria().setRandom(true), paginatedList);
        
        for (TrackDto trackDto : paginatedList.getResultList()) {
            // Insert the track into the playlist
            playlistTrackDao.insertPlaylistTrack(playlist.getId(), trackDto.getId(), order++);
        }
        
        // Output the playlist
        return Response.ok()
                .entity(buildPlaylistJson(playlist))
                .build();
    }

    /**
     * Move the track to another position in the playlist.
     *
     * @param order Current track order in the playlist
     * @param newOrder New track order in the playlist
     * @return Response
     */
    @POST
    @Path("{order: [0-9]+}/move")
    public Response moveTrack(
            @PathParam("order") Integer order,
            @FormParam("neworder") Integer newOrder) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        ValidationUtil.validateRequired(order, "order");
        ValidationUtil.validateRequired(newOrder, "neworder");

        // Get the playlist
        PlaylistDao playlistDao = new PlaylistDao();
        PlaylistDto playlist = playlistDao.getDefaultPlaylistByUserId(principal.getId());
        if (playlist == null) {
            throw new ServerException("UnknownError", MessageFormat.format("Playlist not found for user {0}", principal.getId()));
        }
        PlaylistTrackDao playlistTrackDao = new PlaylistTrackDao();

        // Remove the track at the current order from playlist
        String trackId = playlistTrackDao.removePlaylistTrack(playlist.getId(), order);
        if (trackId == null) {
            throw new ClientException("TrackNotFound", MessageFormat.format("Track not found at position {0}", order));
        }

        // Insert the track at the new order into the playlist
        playlistTrackDao.insertPlaylistTrack(playlist.getId(), trackId, newOrder);

        // Output the playlist
        return Response.ok()
                .entity(buildPlaylistJson(playlist))
                .build();
    }

    /**
     * Remove a track from the playlist.
     *
     * @param order Current track order in the playlist
     * @return Response
     */
    @DELETE
    @Path("{order: [0-9]+}")
    public Response delete(
            @PathParam("order") Integer order) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        ValidationUtil.validateRequired(order, "order");

        // Get the playlist
        PlaylistDao playlistDao = new PlaylistDao();
        PlaylistDto playlist = playlistDao.getDefaultPlaylistByUserId(principal.getId());
        if (playlist == null) {
            throw new ServerException("UnknownError", MessageFormat.format("Playlist not found for user {0}", principal.getId()));
        }
        PlaylistTrackDao playlistTrackDao = new PlaylistTrackDao();

        // Remove the track at the current order from playlist
        String trackId = playlistTrackDao.removePlaylistTrack(playlist.getId(), order);
        if (trackId == null) {
            throw new ClientException("TrackNotFound", MessageFormat.format("Track not found at position {0}", order));
        }

        // Output the playlist
        return Response.ok()
                .entity(buildPlaylistJson(playlist))
                .build();
    }

    /**
     * Returns all tracks in the playlist.
     *
     * @return Response
     */
    @GET
    public Response list() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Output the playlist
        PlaylistDto playlist = new PlaylistDao().getDefaultPlaylistByUserId(principal.getId());

        return Response.ok()
                .entity(buildPlaylistJson(playlist))
                .build();
    }

    /**
     * Removes all tracks from the playlist.
     *
     * @return Response
     */
    @DELETE
    public Response clear() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the playlist
        PlaylistDao playlistDao = new PlaylistDao();
        PlaylistDto playlist = playlistDao.getDefaultPlaylistByUserId(principal.getId());
        if (playlist == null) {
            throw new ServerException("UnknownError", MessageFormat.format("Playlist not found for user {0}", principal.getId()));
        }
        PlaylistTrackDao playlistTrackDao = new PlaylistTrackDao();

        // Delete all tracks in the playlist
        playlistTrackDao.deleteByPlaylistId(playlist.getId());

        // Always return OK
        return Response.ok()
                .entity(Json.createObjectBuilder().add("status", "ok").build())
                .build();
    }
    
    /**
     * Build the JSON output of a playlist.
     * 
     * @param playlist Playlist
     * @return JSON
     */
    private JsonObject buildPlaylistJson(PlaylistDto playlist) {
        JsonObjectBuilder response = Json.createObjectBuilder();
        JsonArrayBuilder tracks = Json.createArrayBuilder();
        TrackDao trackDao = new TrackDao();
        List<TrackDto> trackList = trackDao.findByCriteria(new TrackCriteria()
                .setUserId(principal.getId())
                .setPlaylistId(playlist.getId()));
        int i = 0;
        for (TrackDto trackDto : trackList) {
            tracks.add(Json.createObjectBuilder()
                    .add("order", i++)
                    .add("id", trackDto.getId())
                    .add("title", trackDto.getTitle())
                    .add("year", JsonUtil.nullable(trackDto.getYear()))
                    .add("genre", JsonUtil.nullable(trackDto.getGenre()))
                    .add("length", trackDto.getLength())
                    .add("bitrate", trackDto.getBitrate())
                    .add("vbr", trackDto.isVbr())
                    .add("format", trackDto.getFormat())
                    .add("play_count", trackDto.getUserTrackPlayCount())
                    .add("liked", trackDto.isUserTrackLike())

                    .add("artist", Json.createObjectBuilder()
                            .add("id", trackDto.getArtistId())
                            .add("name", trackDto.getArtistName()))
                    
                    .add("album", Json.createObjectBuilder()
                            .add("id", trackDto.getAlbumId())
                            .add("name", trackDto.getAlbumName())
                            .add("albumart", trackDto.getAlbumArt() != null)));
        }
        response.add("tracks", tracks);
        return response.build();
    }
}
