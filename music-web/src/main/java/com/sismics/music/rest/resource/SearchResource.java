package com.sismics.music.rest.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.music.core.dao.dbi.AlbumDao;
import com.sismics.music.core.dao.dbi.TrackDao;
import com.sismics.music.core.dao.dbi.criteria.AlbumCriteria;
import com.sismics.music.core.dao.dbi.criteria.TrackCriteria;
import com.sismics.music.core.dao.dbi.dto.AlbumDto;
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
            @QueryParam("offset") Integer offset) throws JSONException {

        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        ValidationUtil.validateRequired(query, "query");

        // Search tracks
        PaginatedList<TrackDto> paginatedList = PaginatedLists.create(limit, offset);
        TrackDao trackDao = new TrackDao();
        trackDao.findByCriteria(new TrackCriteria().setUserId(principal.getId()).setTitleLike(query), paginatedList);

        List<JSONObject> tracks = new ArrayList<JSONObject>();
        int i = 1;
        JSONObject response = new JSONObject();
        for (TrackDto trackDto : paginatedList.getResultList()) {
            JSONObject track = new JSONObject();
            track.put("order", i++);    // TODO use order from track
            track.put("id", trackDto.getId());
            track.put("title", trackDto.getTitle());
            track.put("year", trackDto.getYear());
            track.put("length", trackDto.getLength());
            track.put("bitrate", trackDto.getBitrate());
            track.put("vbr", trackDto.isVbr());
            track.put("format", trackDto.getFormat());
            track.put("play_count", trackDto.getUserTrackPlayCount());
            track.put("liked", trackDto.isUserTrackLike());

            JSONObject album = new JSONObject();
            album.put("id", trackDto.getAlbumId());
            album.put("name", trackDto.getAlbumName());
            track.put("album", album);

            JSONObject artist = new JSONObject();
            artist.put("id", trackDto.getArtistId());
            artist.put("name", trackDto.getArtistName());
            track.put("artist", artist);

            tracks.add(track);
        }
        response.put("tracks", tracks);

        // Search albums
        AlbumDao albumDao = new AlbumDao();
        List<AlbumDto> albumList = albumDao.findByCriteria(new AlbumCriteria().setNameLike(query));

        List<JSONObject> albums = new ArrayList<JSONObject>();
        for (AlbumDto album : albumList) {
            JSONObject albumJson = new JSONObject();
            albumJson.put("id", album.getId());
            albumJson.put("name", album.getName());
            albumJson.put("albumart", album.getAlbumArt() != null);

            JSONObject artistJson = new JSONObject();
            artistJson.put("id", album.getArtistId());
            artistJson.put("name", album.getArtistName());
            albumJson.put("artist", artistJson);
            albums.add(albumJson);
        }
        response.put("albums", albums);
        
        return Response.ok().entity(response).build();
    }

}
