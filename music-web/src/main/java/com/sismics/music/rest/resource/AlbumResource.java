package com.sismics.music.rest.resource;

import com.sismics.music.core.dao.dbi.AlbumDao;
import com.sismics.music.core.dao.dbi.TrackDao;
import com.sismics.music.core.dao.dbi.criteria.AlbumCriteria;
import com.sismics.music.core.dao.dbi.criteria.TrackCriteria;
import com.sismics.music.core.dao.dbi.dto.AlbumDto;
import com.sismics.music.core.dao.dbi.dto.TrackDto;
import com.sismics.music.core.exception.NonWritableException;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.dbi.Album;
import com.sismics.music.core.service.albumart.AlbumArtService;
import com.sismics.music.core.service.albumart.AlbumArtSize;
import com.sismics.music.core.util.dbi.PaginatedList;
import com.sismics.music.core.util.dbi.PaginatedLists;
import com.sismics.music.core.util.dbi.SortCriteria;
import com.sismics.music.rest.util.JsonUtil;
import com.sismics.rest.FormDataUtil;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.Validation;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
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
     */
    @GET
    @Path("{id: [a-z0-9\\-]+}")
    public Response detail(
            @PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get album info
        AlbumDao albumDao = new AlbumDao();
        List<AlbumDto> albumList = albumDao.findByCriteria(new AlbumCriteria().setUserId(principal.getId()).setId(id));
        if (albumList.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        AlbumDto album = albumList.iterator().next();

        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("id", album.getId())
                .add("name", album.getName())
                .add("albumart", album.getAlbumArt() != null)
                .add("play_count", album.getUserPlayCount());
        
        response.add("artist", Json.createObjectBuilder()
                .add("id", album.getArtistId())
                .add("name", album.getArtistName()));

        // Get track info
        JsonArrayBuilder tracks = Json.createArrayBuilder();
        TrackDao trackDao = new TrackDao();
        List<TrackDto> trackList = trackDao.findByCriteria(new TrackCriteria()
                .setAlbumId(album.getId())
                .setUserId(principal.getId()));
        
        for (TrackDto trackDto : trackList) {
            tracks.add(Json.createObjectBuilder()
                    .add("order", JsonUtil.nullable(trackDto.getOrder()))
                    .add("id", trackDto.getId())
                    .add("title", trackDto.getTitle())
                    .add("year", JsonUtil.nullable(trackDto.getYear()))
                    .add("genre", JsonUtil.nullable(trackDto.getGenre()))
                    .add("length", trackDto.getLength())
                    .add("bitrate", trackDto.getBitrate())
                    .add("vbr", trackDto.isVbr())
                    .add("format", trackDto.getFormat())
                    .add("filename", trackDto.getFileName())
                    .add("play_count", trackDto.getUserTrackPlayCount())
                    .add("liked", trackDto.isUserTrackLike())
                    .add("artist", Json.createObjectBuilder()
                            .add("id", trackDto.getArtistId())
                            .add("name", trackDto.getArtistName())));
        }
        response.add("tracks", tracks);

        return renderJson(response);
    }

    /**
     * Returns an album cover.
     *
     * @param id Album ID
     * @param size Cover size
     * @return Response
     */
    @GET
    @Path("{id: [a-z0-9\\-]+}/albumart/{size: [a-z]+}")
    public Response albumart(
            @PathParam("id") String id,
            @PathParam("size") String size) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the album
        Album album = Album.getActiveById(id);
        if (album == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Get the album art size
        AlbumArtSize albumArtSize;
        try {
            albumArtSize = AlbumArtSize.valueOf(size.toUpperCase());
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Get the file
        final AlbumArtService albumArtService = AppContext.getInstance().getAlbumArtService();
        File file = albumArtService.getAlbumArtFile(album.getAlbumArt(), albumArtSize);
        if (!file.exists() || !file.canRead()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(file, "image/jpeg")
                .header("Expires", new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").format(new Date().getTime() + 3600000 * 24 * 7))
                .build();
    }
    
    /**
     * Download an album cover from an URL and update the cover.
     *
     * @param id Album ID
     * @param url Image URL
     * @return Response
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}/albumart/fromurl")
    public Response updateAlbumart(
            @PathParam("id") String id,
            @FormParam("url") String url) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the album
        Album album = Album.getActiveById(id);
        notFoundIfNull(album, "id");

        // Copy the remote URL to a temporary file
        File imageFile = downloadAlbumArt(url);

        JsonObjectBuilder response = Json.createObjectBuilder().add("status", "ok");
        
        // Update the album art
        final AlbumArtService albumArtService = AppContext.getInstance().getAlbumArtService();
        String oldAlbumArtId = album.getAlbumArt();
        try {
            albumArtService.importAlbumArt(album, imageFile, true);
        } catch (NonWritableException e) {
            // The album art could't be copied to the album folder
            response.add("message", "AlbumArtNotCopied");
        } catch (Exception e) {
            log.error("The provided URL is not an image", e);
            throw new ClientException("ImageError", "The provided URL is not an image");
        }
        AlbumDao.update(album);
        
        // Delete the previous album art
        if (oldAlbumArtId != null) {
            albumArtService.deleteAlbumArt(oldAlbumArtId);
        }

        return renderJson(response);
    }

    /**
     * Download an album cover from an URL and update the cover.
     *
     * @param id Album ID
     * @param fileBodyPart The image
     * @return Response
     */
    @PUT
    @Path("{id: [a-z0-9\\-]+}/albumart")
    @Consumes("multipart/form-data")
    public Response updateAlbumart(
            @PathParam("id") String id,
            @FormDataParam("file") FormDataBodyPart fileBodyPart) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the album
        Album album = Album.getActiveById(id);
        notFoundIfNull(album, "id");

        // Validate input data
        Validation.required(fileBodyPart, "file");
        Validation.required(fileBodyPart.getFormDataContentDisposition().getFileName(), "filename");

        JsonObjectBuilder response = Json.createObjectBuilder().add("status", "ok");
        File importFile = null;
        try {
            importFile = FormDataUtil.getAsTempFile(fileBodyPart);

            // Update the album art
            final AlbumArtService albumArtService = AppContext.getInstance().getAlbumArtService();
            String oldAlbumArtId = album.getAlbumArt();
            try {
                albumArtService.importAlbumArt(album, importFile, true);
            } catch (NonWritableException e) {
                // The album art could't be copied to the album folder
                response.add("message", "AlbumArtNotCopied");
            } catch (Exception e) {
                log.error("The provided URL is not an image", e);
                throw new ClientException("ImageError", "The provided URL is not an image");
            }
            AlbumDao.update(album);

            // Delete the previous album art
            if (oldAlbumArtId != null) {
                albumArtService.deleteAlbumArt(oldAlbumArtId);
            }
        } catch (Exception e) {
            throw new ServerException("ImportError", e.getMessage(), e);
        } finally {
            if (importFile != null) {
                importFile.delete();
            }
        }

        return renderJson(response);
    }

    /**
     * Returns active albums.
     *
     * @return Response
     */
    @GET
    public Response list(
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            @QueryParam("sort_column") Integer sortColumn,
            @QueryParam("asc") Boolean asc,
            @QueryParam("search") String search) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        AlbumDao albumDao = new AlbumDao();
        PaginatedList<AlbumDto> paginatedList = PaginatedLists.create(limit, offset);
        SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);
        AlbumCriteria albumCriteria = new AlbumCriteria()
                .setUserId(principal.getId())
                .setNameLike(search);
        albumDao.findByCriteria(paginatedList, albumCriteria, sortCriteria, null);

        JsonObjectBuilder response = Json.createObjectBuilder();
        JsonArrayBuilder items = Json.createArrayBuilder();
        for (AlbumDto album : paginatedList.getResultList()) {
            items.add(Json.createObjectBuilder()
                    .add("id", album.getId())
                    .add("name", album.getName())
                    .add("update_date", album.getUpdateDate().getTime())
                    .add("albumart", album.getAlbumArt() != null)
                    .add("play_count", album.getUserPlayCount())
                    .add("artist", Json.createObjectBuilder()
                            .add("id", album.getArtistId())
                            .add("name", album.getArtistName())));
        }
        
        response.add("total", paginatedList.getResultCount());
        response.add("albums", items);

        return renderJson(response);
    }

    // FIXME Handle HTTP Client https://github.com/sismics/music/issues/78
    public static File downloadAlbumArt(String url) {
        File imageFile;
        try {
            imageFile = File.createTempFile("music_albumart", null);
            try (InputStream urlStream = new URL(url).openStream();
                 OutputStream imageStream = new FileOutputStream(imageFile)) {
                IOUtils.copy(urlStream, imageStream);
            }
        } catch (IOException e) {
            log.error("Error reading remote URL", e);
            throw new ClientException("IOError", "Error while reading the remote URL");
        }
        return imageFile;
    }
}
