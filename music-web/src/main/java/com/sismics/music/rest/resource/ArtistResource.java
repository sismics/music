package com.sismics.music.rest.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.music.core.dao.dbi.ArtistDao;
import com.sismics.music.core.dao.dbi.criteria.ArtistCriteria;
import com.sismics.music.core.dao.dbi.dto.ArtistDto;
import com.sismics.rest.exception.ForbiddenClientException;

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
     * @throws JSONException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        ArtistDao artistDao = new ArtistDao();
        List<ArtistDto> artistList = artistDao.findByCriteria(new ArtistCriteria());

        JSONObject response = new JSONObject();
        List<JSONObject> items = new ArrayList<JSONObject>();
        for (ArtistDto artist : artistList) {
            JSONObject artistJson = new JSONObject();
            artistJson.put("id", artist.getId());
            artistJson.put("name", artist.getName());
            items.add(artistJson);
        }
        response.put("artists", items);

        return Response.ok().entity(response).build();
    }
}
