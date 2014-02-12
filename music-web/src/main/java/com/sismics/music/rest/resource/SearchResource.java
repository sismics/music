package com.sismics.music.rest.resource;

import com.sismics.music.core.dao.dbi.TrackDao;
import com.sismics.music.core.dao.dbi.criteria.TrackCriteria;
import com.sismics.music.core.dao.dbi.dto.TrackDto;
import com.sismics.music.core.util.dbi.PaginatedList;
import com.sismics.music.core.util.dbi.PaginatedLists;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.ValidationUtil;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

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

        return Response.ok().entity(response).build();
    }

}
