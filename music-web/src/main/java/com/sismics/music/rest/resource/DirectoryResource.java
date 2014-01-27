package com.sismics.music.rest.resource;

import com.sismics.music.core.dao.jpa.DirectoryDao;
import com.sismics.music.core.event.async.DirectoryCreatedAsyncEvent;
import com.sismics.music.core.event.async.DirectoryDeletedAsyncEvent;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.jpa.Directory;
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
import java.util.Date;
import java.util.List;

/**
 * Directory REST resources.
 * 
 * @author jtremeaux
 */
@Path("/directory")
public class DirectoryResource extends BaseResource {
    /**
     * Creates a new directory.
     *
     * @param name Directory name
     * @param location Directory location
     * @return Response
     * @throws JSONException
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(
        @FormParam("name") String name,
        @FormParam("location") String location) throws JSONException {

        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        // Validate the input data
        name = ValidationUtil.validateLength(name, "name", 0, 100, true);
        location = ValidationUtil.validateLength(location, "location", 1, 1000);

        // Create the directory
        Directory directory = new Directory();
        directory.setName(name);
        directory.setLocation(location);

        // Create the directory
        DirectoryDao directoryDao = new DirectoryDao();
        String directoryId = directoryDao.create(directory);
        
        // Raise a directory creation event
        DirectoryCreatedAsyncEvent directoryCreatedAsyncEvent = new DirectoryCreatedAsyncEvent();
        directoryCreatedAsyncEvent.setDirectory(directory);
        AppContext.getInstance().getCollectionEventBus().post(directoryCreatedAsyncEvent);

        // Always return OK
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    /**
     * Updates directory informations.
     *
     * @param name Directory name
     * @param location Location
     * @param active True if the directory is active.
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(
        @PathParam("id") String id,
        @FormParam("name") String name,
        @FormParam("location") String location,
        @FormParam("active") boolean active) throws JSONException {

        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        // Validate the input data
        name = ValidationUtil.validateLength(name, "name", 0, 100);
        location = ValidationUtil.validateLength(location, "location", 1, 1000);

        // Check if the directory exists
        DirectoryDao directoryDao = new DirectoryDao();
        Directory directory = directoryDao.getActiveById(id);
        if (directory == null) {
            throw new ClientException("DirectoryNotFound", "The directory doesn't exist");
        }

        // Update the directory
        if (name != null) {
            directory.setName(name);
        }
        directory.setLocation(location);
        if (active) {
            directory.setDisableDate(null);
        } else {
            directory.setDisableDate(new Date());
        }
        directoryDao.update(directory);

        // TODO delete and recreate index if the location is different
        // Raise a directory creation event
//        DirectoryCreatedAsyncEvent directoryCreatedAsyncEvent = new DirectoryCreatedAsyncEvent();
//        directoryCreatedAsyncEvent.setDirectory(directory);
//        AppContext.getInstance().getCollectionEventBus().post(directoryCreatedAsyncEvent);

        // Always return "ok"
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    /**
     * Deletes a directory.
     *
     * @param id Directory ID
     * @return Response
     * @throws JSONException
     */
    @DELETE
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") String id) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        // Check if the directory exists
        DirectoryDao directoryDao = new DirectoryDao();
        Directory directory = directoryDao.getActiveById(id);
        if (directory == null) {
            throw new ClientException("DirectoryNotFound", "The directory doesn't exist");
        }

        // Delete the directory
        directoryDao.delete(directory.getId());

        // Raise a directory deleted event
        DirectoryDeletedAsyncEvent directoryDeletedAsyncEvent = new DirectoryDeletedAsyncEvent();
        directoryDeletedAsyncEvent.setDirectory(directory);
        AppContext.getInstance().getCollectionEventBus().post(directoryDeletedAsyncEvent);

        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    /**
     * Returns all active directories.
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

        DirectoryDao directoryDao = new DirectoryDao();
        List<Directory> directoryList = directoryDao.findAll();

        JSONObject response = new JSONObject();
        List<JSONObject> items = new ArrayList<JSONObject>();
        for (Directory directory : directoryList) {
            JSONObject directoryJson = new JSONObject();
            directoryJson.put("id", directory.getId());
            directoryJson.put("name", directory.getId());
            directoryJson.put("location", directory.getLocation());
            directoryJson.put("active", directory.getDisableDate() == null);
            directoryJson.put("valid", true); // TODO test if directory valid
            items.add(directoryJson);
        }
        response.put("directories", items);

        return Response.ok().entity(response).build();
    }
}
