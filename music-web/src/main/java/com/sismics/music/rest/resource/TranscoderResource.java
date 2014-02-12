package com.sismics.music.rest.resource;

import com.sismics.music.core.dao.dbi.TranscoderDao;
import com.sismics.music.core.model.dbi.Transcoder;
import com.sismics.music.rest.constant.BaseFunction;
import com.sismics.rest.exception.ClientException;
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
 * Transcoder REST resources.
 * 
 * @author jtremeaux
 */
@Path("/transcoder")
public class TranscoderResource extends BaseResource {
    /**
     * Creates a new transcoder.
     *
     * @param name Transcoder name
     * @param source Transcoder source formats, space separated
     * @param destination Transcoder destination format
     * @param step1 Transcoder command (step 1)
     * @param step2 Transcoder command (step 2)
     * @return Response
     * @throws org.codehaus.jettison.json.JSONException
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(
        @FormParam("name") String name,
        @FormParam("source") String source,
        @FormParam("destination") String destination,
        @FormParam("step1") String step1,
        @FormParam("step2") String step2) throws JSONException {

        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        // Validate the input data
        name = ValidationUtil.validateLength(name, "name", 1, 100);
        source = ValidationUtil.validateLength(source, "source", 1, 1000);
        destination = ValidationUtil.validateLength(destination, "destination", 1, 100);
        step1 = ValidationUtil.validateLength(step1, "step1", 1, 1000);
        step2 = ValidationUtil.validateLength(step2, "step2", 1, 1000, true);

        // Create the transcoder
        Transcoder transcoder = new Transcoder();
        transcoder.setName(name);
        transcoder.setSource(source);
        transcoder.setDestination(destination);
        transcoder.setStep1(step1);
        transcoder.setStep2(step2);

        TranscoderDao transcoderDao = new TranscoderDao();
        String transcoderId = transcoderDao.create(transcoder);

        // Always return OK
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    /**
     * Updates transcoder informations.
     *
     * @param id Transcoder ID
     * @param name Transcoder name
     * @param source Transcoder source formats, space separated
     * @param destination Transcoder destination format
     * @param step1 Transcoder command (step 1)
     * @param step2 Transcoder command (step 2)
     * @return Response
     * @throws org.codehaus.jettison.json.JSONException
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(
            @PathParam("id") String id,
            @FormParam("name") String name,
            @FormParam("source") String source,
            @FormParam("destination") String destination,
            @FormParam("step1") String step1,
            @FormParam("step2") String step2) throws JSONException {

        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        // Validate the input data
        name = ValidationUtil.validateLength(name, "name", 1, 100);
        source = ValidationUtil.validateLength(source, "source", 1, 1000);
        destination = ValidationUtil.validateLength(destination, "destination", 1, 100);
        step1 = ValidationUtil.validateLength(step1, "step1", 1, 1000);
        step2 = ValidationUtil.validateLength(step2, "step2", 1, 1000, true);

        // Update the transcoder
        TranscoderDao transcoderDao = new TranscoderDao();
        Transcoder transcoder = transcoderDao.getActiveById(id);
        transcoder.setName(name);
        transcoder.setSource(source);
        transcoder.setDestination(destination);
        transcoder.setStep1(step1);
        transcoder.setStep2(step2);
        transcoder = transcoderDao.update(transcoder);

        // Always return "ok"
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    /**
     * Deletes a transcoder.
     *
     * @param id Transcoder ID
     * @return Response
     * @throws org.codehaus.jettison.json.JSONException
     */
    @DELETE
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") String id) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        // Check if the transcoder exists
        TranscoderDao transcoderDao = new TranscoderDao();
        Transcoder transcoder = transcoderDao.getActiveById(id);
        if (transcoder == null) {
            throw new ClientException("TranscoderNotFound", "The transcoder doesn't exist");
        }

        // Delete the transcoder
        transcoderDao.delete(transcoder.getId());

        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    /**
     * Returns all active transcoders.
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
        checkBaseFunction(BaseFunction.ADMIN);

        TranscoderDao transcoderDao = new TranscoderDao();
        List<Transcoder> transcoderList = transcoderDao.findAll();

        JSONObject response = new JSONObject();
        List<JSONObject> items = new ArrayList<JSONObject>();
        for (Transcoder transcoder : transcoderList) {
            JSONObject transcoderJson = new JSONObject();
            transcoderJson.put("id", transcoder.getId());
            transcoderJson.put("name", transcoder.getName());
            transcoderJson.put("source", transcoder.getSource());
            transcoderJson.put("destination", transcoder.getDestination());
            transcoderJson.put("step1", transcoder.getStep1());
            transcoderJson.put("step2", transcoder.getStep2());
            items.add(transcoderJson);
        }
        response.put("transcoders", items);

        return Response.ok().entity(response).build();
    }
}
