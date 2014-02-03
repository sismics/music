package com.sismics.music.rest.resource;

import com.sismics.music.core.dao.jpa.PlaylistDao;
import com.sismics.music.core.dao.jpa.PlaylistTrackDao;
import com.sismics.music.core.dao.jpa.TrackDao;
import com.sismics.music.core.dao.jpa.criteria.TrackCriteria;
import com.sismics.music.core.dao.jpa.dto.TrackDto;
import com.sismics.music.core.model.jpa.Playlist;
import com.sismics.music.core.model.jpa.Track;
import com.sismics.music.core.util.TransactionUtil;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.text.MessageFormat;
import java.util.ArrayList;
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
     * @return Response
     * @throws JSONException
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response insertTrack(
            @FormParam("id") String id,
            @FormParam("order") Integer order) throws JSONException {

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
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
    
    /**
     * Inserts tracks in the playlist.
     *
     * @param idList List of track ID
     * @return Response
     * @throws JSONException
     */
    @PUT
    @Path("multiple")
    @Produces(MediaType.APPLICATION_JSON)
    public Response insertTracks(
            @FormParam("ids") List<String> idList) throws JSONException {

        if (idList != null) {
            for (String id : idList) {
                insertTrack(id, null);
                TransactionUtil.commit();
            }
        }

        // Always return OK
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    /**
     * Move the track to another position in the playlist.
     *
     * @param order Current track order in the playlist
     * @param newOrder New track order in the playlist
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("{order: [0-9]+}/move")
    @Produces(MediaType.APPLICATION_JSON)
    public Response moveTrack(
            @PathParam("order") Integer order,
            @FormParam("neworder") Integer newOrder) throws JSONException {

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
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    /**
     * Remove a track from the playlist.
     *
     * @param order Current track order in the playlist
     * @return Response
     * @throws JSONException
     */
    @DELETE
    @Path("{order: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(
            @PathParam("order") Integer order) throws JSONException {

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
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    /**
     * Returns all tracks in the playlist.
     *
     * @return Response
     * @throws JSONException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the list of tracks in the playlist
        JSONObject response = new JSONObject();
        List<JSONObject> tracks = new ArrayList<JSONObject>();
        TrackDao trackDao = new TrackDao();
        List<TrackDto> trackList = trackDao.findByCriteria(new TrackCriteria().setUserId(principal.getId()));
        int i = 0;
        for (TrackDto trackDto : trackList) {
            JSONObject track = new JSONObject();
            track.put("order", i++);
            track.put("id", trackDto.getId());
            track.put("title", trackDto.getTitle());
            track.put("year", trackDto.getYear());
            track.put("length", trackDto.getLength());
            track.put("bitrate", trackDto.getBitrate());
            track.put("vbr", trackDto.isVbr());
            track.put("format", trackDto.getFormat());

            JSONObject artist = new JSONObject();
            artist.put("id", trackDto.getArtistId());
            artist.put("name", trackDto.getArtistName());
            track.put("artist", artist);
            
            JSONObject album = new JSONObject();
            album.put("id", trackDto.getAlbumId());
            album.put("name", trackDto.getAlbumName());
            track.put("album", album);

            tracks.add(track);
        }
        response.put("tracks", tracks);

        return Response.ok().entity(response).build();
    }

    /**
     * Removes all tracks from the playlist.
     *
     * @return Response
     * @throws JSONException
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete() throws JSONException {
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

        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
}
