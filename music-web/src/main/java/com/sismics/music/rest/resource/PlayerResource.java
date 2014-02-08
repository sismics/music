package com.sismics.music.rest.resource;

import com.sismics.music.core.dao.jpa.TrackDao;
import com.sismics.music.core.model.jpa.Track;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.ValidationUtil;
import com.sismics.util.MathUtil;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Player REST resources.
 * 
 * @author jtremeaux
 */
@Path("/player")
public class PlayerResource extends BaseResource {
    /**
     * Signals Music that a track is being played. A track is considered playing if it is played more than halfway.
     *
     * @param id Track ID
     * @param time Duration into the track in seconds
     * @return Response
     * @throws org.codehaus.jettison.json.JSONException
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("listening")
    public Response listening(
            @FormParam("id") String id,
            @FormParam("time") Integer time) throws JSONException {

        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        ValidationUtil.validateRequired(time, "time");

        // Load the track
        TrackDao trackDao = new TrackDao();
        Track track = trackDao.getActiveById(id);
        if (track == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        time = MathUtil.clip(time, 0, track.getLength());

        // TODO notify playing

        // Always return OK
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    /**
     * Post a set of tracks played before (useful for offline mode).
     *
     * @param id An array of track ID
     * @param date An array of dates at which the track was started playing
     * @return Response
     * @throws org.codehaus.jettison.json.JSONException
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("listened")
    public Response listened(
            @FormParam("id") List<String> id,
            @FormParam("date") List<Long> date) throws JSONException {

        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        if (id == null || date == null || id.size() != date.size()) {
            throw new ClientException("ValidationError", "Invalid id or dates");
        }

        // TODO notify played

        // Always return OK
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
}
