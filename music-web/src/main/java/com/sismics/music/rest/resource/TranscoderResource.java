package com.sismics.music.rest.resource;

import com.sismics.music.core.dao.dbi.TranscoderDao;
import com.sismics.music.core.model.dbi.Transcoder;
import com.sismics.music.rest.constant.Privilege;
import com.sismics.music.rest.util.JsonUtil;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.Validation;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
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
     */
    @PUT
    public Response create(
        @FormParam("name") String name,
        @FormParam("source") String source,
        @FormParam("destination") String destination,
        @FormParam("step1") String step1,
        @FormParam("step2") String step2) {

        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkPrivilege(Privilege.ADMIN);

        // Validate the input data
        name = Validation.length(name, "name", 1, 100);
        source = Validation.length(source, "source", 1, 1000);
        destination = Validation.length(destination, "destination", 1, 100);
        step1 = Validation.length(step1, "step1", 1, 1000);
        step2 = Validation.length(step2, "step2", 1, 1000, true);

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
        return okJson();
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
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}")
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
        checkPrivilege(Privilege.ADMIN);

        // Validate the input data
        name = Validation.length(name, "name", 1, 100);
        source = Validation.length(source, "source", 1, 1000);
        destination = Validation.length(destination, "destination", 1, 100);
        step1 = Validation.length(step1, "step1", 1, 1000);
        step2 = Validation.length(step2, "step2", 1, 1000, true);

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
        return okJson();
    }

    /**
     * Deletes a transcoder.
     *
     * @param id Transcoder ID
     * @return Response
     */
    @DELETE
    @Path("{id: [a-z0-9\\-]+}")
    public Response delete(@PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkPrivilege(Privilege.ADMIN);

        // Check if the transcoder exists
        TranscoderDao transcoderDao = new TranscoderDao();
        Transcoder transcoder = transcoderDao.getActiveById(id);
        if (transcoder == null) {
            throw new ClientException("TranscoderNotFound", "The transcoder doesn't exist");
        }

        // Delete the transcoder
        transcoderDao.delete(transcoder.getId());

        // Always return OK
        return okJson();
    }

    /**
     * Returns all active transcoders.
     *
     * @return Response
     */
    @GET
    public Response list() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkPrivilege(Privilege.ADMIN);

        TranscoderDao transcoderDao = new TranscoderDao();
        List<Transcoder> transcoderList = transcoderDao.findAll();

        JsonObjectBuilder response = Json.createObjectBuilder();
        JsonArrayBuilder items = Json.createArrayBuilder();
        for (Transcoder transcoder : transcoderList) {
            items.add(Json.createObjectBuilder()
                    .add("id", transcoder.getId())
                    .add("name", transcoder.getName())
                    .add("source", transcoder.getSource())
                    .add("destination", transcoder.getDestination())
                    .add("step1", transcoder.getStep1())
                    .add("step2", JsonUtil.nullable(transcoder.getStep2())));
        }
        response.add("transcoders", items);

        return renderJson(response);
    }
}
