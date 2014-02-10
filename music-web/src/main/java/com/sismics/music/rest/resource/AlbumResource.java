package com.sismics.music.rest.resource;

import com.sismics.music.core.dao.dbi.AlbumDao;
import com.sismics.music.core.dao.dbi.TrackDao;
import com.sismics.music.core.dao.dbi.criteria.AlbumCriteria;
import com.sismics.music.core.dao.dbi.criteria.TrackCriteria;
import com.sismics.music.core.dao.dbi.dto.AlbumDto;
import com.sismics.music.core.dao.dbi.dto.TrackDto;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.dbi.Album;
import com.sismics.music.core.service.albumart.AlbumArtService;
import com.sismics.music.core.service.albumart.AlbumArtSize;
import com.sismics.rest.exception.ForbiddenClientException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Album REST resources.
 * 
 * @author jtremeaux
 */
@Path("/album")
public class AlbumResource extends BaseResource {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(AlbumResource.class);

    /**
     * Returns an album detail.
     *
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response detail(
            @PathParam("id") String id) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get album info
        AlbumDao albumDao = new AlbumDao();
        List<AlbumDto> albumList = albumDao.findByCriteria(new AlbumCriteria().setId(id));
        if (albumList.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        AlbumDto album = albumList.iterator().next();

        JSONObject response = new JSONObject();
        response.put("id", album.getId());
        response.put("name", album.getName());
        response.put("albumart", album.getAlbumArt() != null);

        JSONObject artistJson = new JSONObject();
        artistJson.put("id", album.getArtistId());
        artistJson.put("name", album.getArtistName());
        response.put("artist", artistJson);

        // Get track info
        List<JSONObject> tracks = new ArrayList<JSONObject>();
        TrackDao trackDao = new TrackDao();
        List<TrackDto> trackList = trackDao.findByCriteria(new TrackCriteria().setAlbumId(album.getId()));
        int i = 1;
        for (TrackDto trackDto : trackList) {
            JSONObject track = new JSONObject();
            track.put("order", i++);    // TODO use order from track
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

            tracks.add(track);
        }
        response.put("tracks", tracks);

        return Response.ok().entity(response).build();
    }

    /**
     * Returns a album cover.
     *
     * @param id Album ID
     * @param size Cover size
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("{id: [a-z0-9\\-]+}/albumart/{size: [a-z]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response albumart(
            @PathParam("id") String id,
            @PathParam("size") String size) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the album
        AlbumDao albumDao = new AlbumDao();
        Album album = albumDao.getActiveById(id);
        if (album == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Get the album art size
        AlbumArtSize albumArtSize = null;
        try {
            albumArtSize = AlbumArtSize.valueOf(size.toUpperCase());
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Get the file
        final AlbumArtService albumArtService = AppContext.getInstance().getAlbumArtService();
        File file = albumArtService.getAlbumArtFile(album.getAlbumArt(), albumArtSize);
        if (!file.exists() || !file.canRead()) {
            if (log.isErrorEnabled()) {
                log.error("Album art file cannot be read: " + file.getAbsolutePath());
            }
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(file, "image/jpeg")
                .header("Expires", new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").format(new Date().getTime() + 3600000 * 24 * 7))
                .build();
    }

    /**
     * Returns all active albums.
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

        AlbumDao albumDao = new AlbumDao();
        List<AlbumDto> albumList = albumDao.findByCriteria(new AlbumCriteria());

        JSONObject response = new JSONObject();
        List<JSONObject> items = new ArrayList<JSONObject>();
        for (AlbumDto album : albumList) {
            JSONObject albumJson = new JSONObject();
            albumJson.put("id", album.getId());
            albumJson.put("name", album.getName());
            albumJson.put("albumart", album.getAlbumArt() != null);

            JSONObject artistJson = new JSONObject();
            artistJson.put("id", album.getArtistId());
            artistJson.put("name", album.getArtistName());
            albumJson.put("artist", artistJson);
            items.add(albumJson);
        }
        response.put("albums", items);

        // TODO add stats

        return Response.ok().entity(response).build();
    }
}
