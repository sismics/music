package com.sismics.music.rest.resource;

import java.text.MessageFormat;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sismics.music.core.dao.dbi.PlaylistDao;
import com.sismics.music.core.dao.dbi.PlaylistTrackDao;
import com.sismics.music.core.dao.dbi.TrackDao;
import com.sismics.music.core.dao.dbi.criteria.TrackCriteria;
import com.sismics.music.core.dao.dbi.dto.TrackDto;
import com.sismics.music.core.model.dbi.Playlist;
import com.sismics.music.core.model.dbi.Track;
import com.sismics.music.core.util.TransactionUtil;
import com.sismics.music.rest.util.JsonUtil;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;

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
     * @return Response
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response insertTrack(
            @FormParam("id") String id,
            @FormParam("order") Integer order) {

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
        Playlist playlist = playlistDao.getActiveByUserId(principal.getId());
        if (playlist == null) {
            throw new ServerException("UnknownError", MessageFormat.format("Playlist not found for user {0}", principal.getId()));
        }
        PlaylistTrackDao playlistTrackDao = new PlaylistTrackDao();

        // Get the track order
        if (order == null) {
            order = playlistTrackDao.getPlaylistTrackNextOrder(playlist.getId());
        }

        // Insert the track into the playlist
        playlistTrackDao.insertPlaylistTrack(playlist.getId(), track.getId(), order);

        // Always return OK
        return Response.ok()
                .entity(Json.createObjectBuilder().add("status", "ok").build())
                .build();
    }
    
    /**
     * Inserts tracks in the playlist.
     *
     * @param idList List of track ID
     * @return Response
     */
    @PUT
    @Path("multiple")
    @Produces(MediaType.APPLICATION_JSON)
    public Response insertTracks(
            @FormParam("ids") List<String> idList) {

        if (idList != null) {
            for (String id : idList) {
                insertTrack(id, null);
                TransactionUtil.commit();
            }
        }

        // Always return OK
        return Response.ok()
                .entity(Json.createObjectBuilder().add("status", "ok").build())
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
    @Produces(MediaType.APPLICATION_JSON)
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
        Playlist playlist = playlistDao.getActiveByUserId(principal.getId());
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

        // Always return OK
        return Response.ok()
                .entity(Json.createObjectBuilder().add("status", "ok").build())
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
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(
            @PathParam("order") Integer order) {

        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        ValidationUtil.validateRequired(order, "order");

        // Get the playlist
        PlaylistDao playlistDao = new PlaylistDao();
        Playlist playlist = playlistDao.getActiveByUserId(principal.getId());
        if (playlist == null) {
            throw new ServerException("UnknownError", MessageFormat.format("Playlist not found for user {0}", principal.getId()));
        }
        PlaylistTrackDao playlistTrackDao = new PlaylistTrackDao();

        // Remove the track at the current order from playlist
        String trackId = playlistTrackDao.removePlaylistTrack(playlist.getId(), order);
        if (trackId == null) {
            throw new ClientException("TrackNotFound", MessageFormat.format("Track not found at position {0}", order));
        }

        // Always return OK
        return Response.ok()
                .entity(Json.createObjectBuilder().add("status", "ok").build())
                .build();
    }

    /**
     * Returns all tracks in the playlist.
     *
     * @return Response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the list of tracks in the playlist
        Playlist playlist = new PlaylistDao().getActiveByUserId(principal.getId());
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

        return Response.ok().entity(response.build()).build();
    }

    /**
     * Removes all tracks from the playlist.
     *
     * @return Response
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the playlist
        PlaylistDao playlistDao = new PlaylistDao();
        Playlist playlist = playlistDao.getActiveByUserId(principal.getId());
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
}
