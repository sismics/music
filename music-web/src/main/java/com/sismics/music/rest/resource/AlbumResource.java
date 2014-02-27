package com.sismics.music.rest.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;

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
            @PathParam("id") String id) {
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

        JsonObjectBuilder response = Json.createObjectBuilder();
        response.add("id", album.getId());
        response.add("name", album.getName());
        response.add("albumart", album.getAlbumArt() != null);

        JsonObjectBuilder artistJson = Json.createObjectBuilder();
        artistJson.add("id", album.getArtistId());
        artistJson.add("name", album.getArtistName());
        response.add("artist", artistJson);

        // Get track info
        JsonArrayBuilder tracks = Json.createArrayBuilder();
        TrackDao trackDao = new TrackDao();
        List<TrackDto> trackList = trackDao.findByCriteria(new TrackCriteria()
                .setAlbumId(album.getId())
                .setUserId(principal.getId()));
        int i = 1;
        for (TrackDto trackDto : trackList) {
            JsonObjectBuilder track = Json.createObjectBuilder();
            track.add("order", i++);    // TODO use order from track
            track.add("id", trackDto.getId());
            track.add("title", trackDto.getTitle());
            track.add("year", trackDto.getYear());
            track.add("length", trackDto.getLength());
            track.add("bitrate", trackDto.getBitrate());
            track.add("vbr", trackDto.isVbr());
            track.add("format", trackDto.getFormat());
            track.add("filename", trackDto.getFileName());
            track.add("play_count", trackDto.getUserTrackPlayCount());
            track.add("liked", trackDto.isUserTrackLike());

            JsonObjectBuilder artist = Json.createObjectBuilder();
            artist.add("id", trackDto.getArtistId());
            artist.add("name", trackDto.getArtistName());
            track.add("artist", artist);

            tracks.add(track);
        }
        response.add("tracks", tracks);

        return Response.ok().entity(response.build()).build();
    }

    /**
     * Returns an album cover.
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
            @PathParam("size") String size) {
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
     * Update an album cover.
     *
     * @param id Album ID
     * @param url Image URL
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}/albumart")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateAlbumart(
            @PathParam("id") String id,
            @FormParam("url") String url) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the album
        AlbumDao albumDao = new AlbumDao();
        Album album = albumDao.getActiveById(id);
        if (album == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Copy the remote URL to a temporary file
        File imageFile = null;
        try {
            imageFile = File.createTempFile("music_albumart", null);
            try (InputStream urlStream = new URL(url).openStream();
                    OutputStream imageStream = new FileOutputStream(imageFile)) {
                IOUtils.copy(urlStream, imageStream);
            }
        } catch (IOException e) {
            throw new ClientException("IOError", "Error while reading the remote URL", e);
        }
            
        // Update the album art
        try {
            final AlbumArtService albumArtService = AppContext.getInstance().getAlbumArtService();
            String albumArtId = albumArtService.importAlbumArt(imageFile);
            album.setAlbumArt(albumArtId);
            albumDao.update(album);
        } catch (Exception e) {
            throw new ClientException("ImageError", "The provided URL is not an image", e);
        }
        
        // TODO Delete the previous album art

        // Always return OK
        return Response.ok()
                .entity(Json.createObjectBuilder().add("status", "ok").build())
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
    public Response list(
            @QueryParam("artist") String artistId) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        AlbumDao albumDao = new AlbumDao();
        List<AlbumDto> albumList = albumDao.findByCriteria(new AlbumCriteria().setArtistId(artistId));

        JsonObjectBuilder response = Json.createObjectBuilder();
        JsonArrayBuilder items = Json.createArrayBuilder();
        for (AlbumDto album : albumList) {
            JsonObjectBuilder albumJson = Json.createObjectBuilder();
            albumJson.add("id", album.getId());
            albumJson.add("name", album.getName());
            albumJson.add("update_date", album.getUpdateDate().getTime());
            albumJson.add("albumart", album.getAlbumArt() != null);

            JsonObjectBuilder artistJson = Json.createObjectBuilder();
            artistJson.add("id", album.getArtistId());
            artistJson.add("name", album.getArtistName());
            albumJson.add("artist", artistJson);
            items.add(albumJson);
        }
        response.add("albums", items);

        // TODO add stats

        return Response.ok().entity(response.build()).build();
    }
}
