package com.sismics.music.rest.resource;

import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sismics.music.core.dao.dbi.AlbumDao;
import com.sismics.music.core.dao.dbi.ArtistDao;
import com.sismics.music.core.dao.dbi.TrackDao;
import com.sismics.music.core.dao.dbi.criteria.AlbumCriteria;
import com.sismics.music.core.dao.dbi.criteria.ArtistCriteria;
import com.sismics.music.core.dao.dbi.criteria.TrackCriteria;
import com.sismics.music.core.dao.dbi.dto.AlbumDto;
import com.sismics.music.core.dao.dbi.dto.ArtistDto;
import com.sismics.music.core.dao.dbi.dto.TrackDto;
import com.sismics.music.core.util.dbi.PaginatedList;
import com.sismics.music.core.util.dbi.PaginatedLists;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.ValidationUtil;

/**
 * Search REST resources.
 * 
 * @author jtremeaux
 */
@Path("/search")
public class SearchResource extends BaseResource {
    /**
     * Run a full text search.
     *
     * @param query Search query
     * @param limit Page limit
     * @param offset Page offset
     * @return Response
     * @throws org.codehaus.jettison.json.JSONException
     */
    @GET
    @Path("{query: .+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @PathParam("query") String query,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {

        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        ValidationUtil.validateRequired(query, "query");

        // Search tracks
        PaginatedList<TrackDto> paginatedList = PaginatedLists.create(limit, offset);
        TrackDao trackDao = new TrackDao();
        trackDao.findByCriteria(new TrackCriteria().setUserId(principal.getId()).setTitleLike(query), paginatedList);

        JsonArrayBuilder tracks = Json.createArrayBuilder();
        int i = 1;
        JsonObjectBuilder response = Json.createObjectBuilder();
        for (TrackDto trackDto : paginatedList.getResultList()) {
            JsonObjectBuilder track = Json.createObjectBuilder();
            track.add("order", i++);    // TODO use order from track
            track.add("id", trackDto.getId());
            track.add("title", trackDto.getTitle());
            track.add("year", trackDto.getYear());
            track.add("length", trackDto.getLength());
            track.add("bitrate", trackDto.getBitrate());
            track.add("vbr", trackDto.isVbr());
            track.add("format", trackDto.getFormat());
            track.add("play_count", trackDto.getUserTrackPlayCount());
            track.add("liked", trackDto.isUserTrackLike());

            JsonObjectBuilder album = Json.createObjectBuilder();
            album.add("id", trackDto.getAlbumId());
            album.add("name", trackDto.getAlbumName());
            album.add("albumart", trackDto.getAlbumArt() != null);
            track.add("album", album);

            JsonObjectBuilder artist = Json.createObjectBuilder();
            artist.add("id", trackDto.getArtistId());
            artist.add("name", trackDto.getArtistName());
            track.add("artist", artist);

            tracks.add(track);
        }
        response.add("tracks", tracks);

        // Search albums
        AlbumDao albumDao = new AlbumDao();
        List<AlbumDto> albumList = albumDao.findByCriteria(new AlbumCriteria().setNameLike(query));

        JsonArrayBuilder albums = Json.createArrayBuilder();
        for (AlbumDto album : albumList) {
            JsonObjectBuilder albumJson = Json.createObjectBuilder();
            albumJson.add("id", album.getId());
            albumJson.add("name", album.getName());
            albumJson.add("albumart", album.getAlbumArt() != null);

            JsonObjectBuilder artistJson = Json.createObjectBuilder();
            artistJson.add("id", album.getArtistId());
            artistJson.add("name", album.getArtistName());
            albumJson.add("artist", artistJson);
            albums.add(albumJson);
        }
        response.add("albums", albums);
        
        // Search artists
        ArtistDao artistDao = new ArtistDao();
        List<ArtistDto> artistList = artistDao.findByCriteria(new ArtistCriteria().setNameLike(query));

        JsonArrayBuilder artists = Json.createArrayBuilder();
        for (ArtistDto artist : artistList) {
            JsonObjectBuilder artistJson = Json.createObjectBuilder();
            artistJson.add("id", artist.getId());
            artistJson.add("name", artist.getName());
            artists.add(artistJson);
        }
        response.add("artists", artists);
        
        return Response.ok().entity(response.build()).build();
    }
}
