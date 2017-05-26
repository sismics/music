package com.sismics.music.rest.resource;

import com.sismics.music.core.dao.dbi.DirectoryDao;
import com.sismics.music.core.event.async.DirectoryCreatedAsyncEvent;
import com.sismics.music.core.event.async.DirectoryDeletedAsyncEvent;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.dbi.Directory;
import com.sismics.music.rest.constant.Privilege;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.Validation;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.nio.file.Paths;
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
     * @param location Directory location
     * @return Response
     */
    @PUT
    public Response create(
        @FormParam("location") String location) {

        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkPrivilege(Privilege.ADMIN);

        // Validate the input data
        location = Validation.length(location, "location", 1, 1000);

        // Create the directory
        Directory directory = new Directory();
        directory.setLocation(location);

        // Create the directory
        DirectoryDao directoryDao = new DirectoryDao();
        directoryDao.create(directory);
        
        // Raise a directory creation event
        DirectoryCreatedAsyncEvent directoryCreatedAsyncEvent = new DirectoryCreatedAsyncEvent();
        directoryCreatedAsyncEvent.setDirectory(directory);
        AppContext.getInstance().getCollectionEventBus().post(directoryCreatedAsyncEvent);

        // Always return OK
        return okJson();
    }

    /**
     * Updates directory informations.
     *
     * @param id The directory ID
     * @param location Location
     * @param active True if the directory is active.
     * @return Response
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}")
    public Response update(
        @PathParam("id") String id,
        @FormParam("location") String location,
        @FormParam("active") boolean active) {

        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkPrivilege(Privilege.ADMIN);

        // Validate the input data
        location = Validation.length(location, "location", 1, 1000);

        // Check if the directory exists
        DirectoryDao directoryDao = new DirectoryDao();
        Directory directory = directoryDao.getActiveById(id);
        if (directory == null) {
            throw new ClientException("DirectoryNotFound", "The directory doesn't exist");
        }

        // Update the directory
        directory.setLocation(location);
        if (active) {
            directory.setDisableDate(null);
        } else {
            directory.setDisableDate(new Date());
        }
        directoryDao.update(directory);

        // Raise a directory creation event
        DirectoryCreatedAsyncEvent directoryCreatedAsyncEvent = new DirectoryCreatedAsyncEvent();
        directoryCreatedAsyncEvent.setDirectory(directory);
        AppContext.getInstance().getCollectionEventBus().post(directoryCreatedAsyncEvent);

        // Always return "ok"
        return okJson();
    }

    /**
     * Deletes a directory.
     *
     * @param id Directory ID
     * @return Response
     */
    @DELETE
    @Path("{id: [a-z0-9\\-]+}")
    public Response delete(@PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkPrivilege(Privilege.ADMIN);

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
        return okJson();
    }

    /**
     * Returns all active directories.
     *
     * @return Response
     */
    @GET
    public Response list() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkPrivilege(Privilege.ADMIN);

        DirectoryDao directoryDao = new DirectoryDao();
        List<Directory> directoryList = directoryDao.findAll();

        JsonObjectBuilder response = Json.createObjectBuilder();
        JsonArrayBuilder items = Json.createArrayBuilder();
        for (Directory directory : directoryList) {
            items.add(Json.createObjectBuilder()
                    .add("id", directory.getId())
                    .add("location", directory.getLocation())
                    .add("active", directory.getDisableDate() == null)
                    .add("valid", java.nio.file.Files.exists(Paths.get(directory.getLocation())))
                    .add("writable", java.nio.file.Files.isWritable(Paths.get(directory.getLocation()))));
        }
        response.add("directories", items);

        return renderJson(response);
    }
}
