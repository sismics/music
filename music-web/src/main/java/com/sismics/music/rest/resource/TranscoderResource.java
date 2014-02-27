package com.sismics.music.rest.resource;

import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sismics.music.core.dao.dbi.TranscoderDao;
import com.sismics.music.core.model.dbi.Transcoder;
import com.sismics.music.rest.constant.BaseFunction;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.ValidationUtil;

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
        @FormParam("step2") String step2) {

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
        transcoderDao.create(transcoder);

        // Always return OK
        return Response.ok()
                .entity(Json.createObjectBuilder().add("status", "ok").build())
                .build();
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
            @FormParam("step2") String step2) {

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

        // Always return OK
        return Response.ok()
                .entity(Json.createObjectBuilder().add("status", "ok").build())
                .build();
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
    public Response delete(@PathParam("id") String id) {
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

        // Always return OK
        return Response.ok()
                .entity(Json.createObjectBuilder().add("status", "ok").build())
                .build();
    }

    /**
     * Returns all active transcoders.
     *
     * @return Response
     * @throws JSONException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        TranscoderDao transcoderDao = new TranscoderDao();
        List<Transcoder> transcoderList = transcoderDao.findAll();

        JsonObjectBuilder response = Json.createObjectBuilder();
        JsonArrayBuilder items = Json.createArrayBuilder();
        for (Transcoder transcoder : transcoderList) {
            JsonObjectBuilder transcoderJson = Json.createObjectBuilder()
                    .add("id", transcoder.getId())
                    .add("name", transcoder.getName())
                    .add("source", transcoder.getSource())
                    .add("destination", transcoder.getDestination())
                    .add("step1", transcoder.getStep1());
            if (transcoder.getStep2() == null) transcoderJson.addNull("step2");
            else transcoderJson.add("step2", transcoder.getStep2());
            items.add(transcoderJson);
        }
        response.add("transcoders", items);

        return Response.ok().entity(response.build()).build();
    }
}
