package com.sismics.music.rest.resource;

import com.sismics.music.core.dao.dbi.AlbumDao;
import com.sismics.music.core.dao.dbi.ArtistDao;
import com.sismics.music.core.dao.dbi.TrackDao;
import com.sismics.music.core.dao.dbi.criteria.AlbumCriteria;
import com.sismics.music.core.dao.dbi.criteria.ArtistCriteria;
import com.sismics.music.core.dao.dbi.criteria.TrackCriteria;
import com.sismics.music.core.dao.dbi.dto.AlbumDto;
import com.sismics.music.core.dao.dbi.dto.ArtistDto;
import com.sismics.music.core.dao.dbi.dto.TrackDto;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.dbi.Album;
import com.sismics.music.core.model.dbi.Artist;
import com.sismics.music.core.service.albumart.AlbumArtService;
import com.sismics.music.core.service.albumart.AlbumArtSize;
import com.sismics.music.core.util.ImageUtil;
import com.sismics.music.core.util.dbi.PaginatedList;
import com.sismics.music.core.util.dbi.PaginatedLists;
import com.sismics.music.core.util.dbi.SortCriteria;
import com.sismics.music.rest.util.JsonUtil;
import com.sismics.rest.exception.ForbiddenClientException;

import javax.imageio.ImageIO;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Artist REST resources.
 * 
 * @author bgamard
 */
@Path("/artist")
public class ArtistResource extends BaseResource {
    /**
     * Returns all active artists.
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

        ArtistDao artistDao = new ArtistDao();
        PaginatedList<ArtistDto> paginatedList = PaginatedLists.create(limit, offset);
        SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);
        ArtistCriteria artistCriteria = new ArtistCriteria()
                .setNameLike(search);
        artistDao.findByCriteria(paginatedList, artistCriteria, sortCriteria, null);

        JsonObjectBuilder response = Json.createObjectBuilder();
        JsonArrayBuilder items = Json.createArrayBuilder();
        for (ArtistDto artist : paginatedList.getResultList()) {
            items.add(Json.createObjectBuilder()
                    .add("id", artist.getId())
                    .add("name", artist.getName()));
        }
        
        response.add("total", paginatedList.getResultCount());
        response.add("artists", items);

        return renderJson(response);
    }
    
    /**
     * Returns an artist.
     * 
     * @param id ArtistID
     * @return Response
     */
    @GET
    @Path("{id: [a-z0-9\\-]+}")
    public Response get(
            @PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the artist
        ArtistDao artistDao = new ArtistDao();
        Artist artist = artistDao.getActiveById(id);
        if (artist == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        // Artist informations
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("id", artist.getId())
                .add("name", artist.getName());
        
        // Artist's albums
        AlbumDao albumDao = new AlbumDao();
        List<AlbumDto> albumList = albumDao.findByCriteria(new AlbumCriteria()
                .setUserId(principal.getId())
                .setArtistId(artist.getId()));

        JsonArrayBuilder albums = Json.createArrayBuilder();
        for (AlbumDto albumDto : albumList) {
            albums.add(Json.createObjectBuilder()
                    .add("id", albumDto.getId())
                    .add("name", albumDto.getName())
                    .add("update_date", albumDto.getUpdateDate().getTime())
                    .add("albumart", albumDto.getAlbumArt() != null)
                    .add("play_count", albumDto.getUserPlayCount())
                    .add("artist", Json.createObjectBuilder()
                            .add("id", albumDto.getArtistId())
                            .add("name", albumDto.getArtistName())));
        }
        response.add("albums", albums);
        
        // Artist's tracks
        TrackDao trackDao = new TrackDao();
        List<TrackDto> trackList = trackDao.findByCriteria(new TrackCriteria()
                .setUserId(principal.getId())
                .setArtistId(artist.getId()));
        
        JsonArrayBuilder tracks = Json.createArrayBuilder();
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
                    .add("album", Json.createObjectBuilder()
                            .add("id", trackDto.getAlbumId())
                            .add("name", trackDto.getAlbumName()))
                    .add("artist", Json.createObjectBuilder()
                            .add("id", trackDto.getArtistId())
                            .add("name", trackDto.getArtistName())));
        }
        response.add("tracks", tracks);

        return renderJson(response);
    }
    
    /**
     * Returns an artist cover.
     *
     * @param id Artist ID
     * @param size Cover size
     * @return Response
     */
    @GET
    @Path("{id: [a-z0-9\\-]+}/artistart/{size: [a-z]+}")
    public Response albumart(
            @PathParam("id") String id,
            @PathParam("size") String size) throws Exception {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the artist
        ArtistDao artistDao = new ArtistDao();
        Artist artist = artistDao.getActiveById(id);
        if (artist == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Get the artist art size
        AlbumArtSize artistArtSize = null;
        try {
            artistArtSize = AlbumArtSize.valueOf(size.toUpperCase());
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Get artist's album arts
        AlbumDao albumDao = new AlbumDao();
        final AlbumArtService albumArtService = AppContext.getInstance().getAlbumArtService();
        List<Album> albumList = albumDao.getActiveByArtistId(id);
        List<BufferedImage> imageList = new ArrayList<>();
        for (Album album : albumList) {
            if (album.getAlbumArt() != null) {
                File file = albumArtService.getAlbumArtFile(album.getAlbumArt(), artistArtSize);
                if (file.exists() && file.canRead()) {
                    imageList.add(ImageIO.read(file));
                }
            }
        }
        
        // Write to PNG
        String format = imageList.size() == 0 ? "png" : "jpeg";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(ImageUtil.makeMosaic(imageList, artistArtSize.getSize()), format, outputStream);
        byte[] imageData = outputStream.toByteArray();
        
        return Response.ok(imageData, "image/" + format)
                .header("Expires", new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").format(new Date().getTime() + 3600000 * 24 * 7))
                .build();
    }
}
