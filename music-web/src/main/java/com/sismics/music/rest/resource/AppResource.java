package com.sismics.music.rest.resource;

import com.sismics.music.core.event.async.CollectionReindexAsyncEvent;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.util.ConfigUtil;
import com.sismics.music.core.util.dbi.PaginatedList;
import com.sismics.music.core.util.dbi.PaginatedLists;
import com.sismics.music.rest.constant.Privilege;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.util.NetworkUtil;
import com.sismics.util.db.DbUtil;
import com.sismics.util.log4j.LogCriteria;
import com.sismics.util.log4j.LogEntry;
import com.sismics.util.log4j.MemoryAppender;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ResourceBundle;

/**
 * General app REST resource.
 * 
 * @author jtremeaux
 */
@Path("/app")
public class AppResource extends BaseResource {
    /**
     * Return the information about the application.
     * 
     * @return Response
     */
    @GET
    public Response version() {
        ResourceBundle configBundle = ConfigUtil.getConfigBundle();
        String currentVersion = configBundle.getString("api.current_version");
        String minVersion = configBundle.getString("api.min_version");

        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("current_version", currentVersion.replace("-SNAPSHOT", ""))
                .add("min_version", minVersion)
                .add("total_memory", Runtime.getRuntime().totalMemory())
                .add("free_memory", Runtime.getRuntime().freeMemory());
        return renderJson(response);
    }
    
    /**
     * Retrieve the application logs.
     * 
     * @param level Filter on logging level
     * @param tag Filter on logger name / tag
     * @param message Filter on message
     * @param limit Page limit
     * @param offset Page offset
     * @return Response
     */
    @GET
    @Path("log")
    public Response log(
            @QueryParam("level") String level,
            @QueryParam("tag") String tag,
            @QueryParam("message") String message,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkPrivilege(Privilege.ADMIN);

        // Get the memory appender
        Logger logger = Logger.getRootLogger();
        Appender appender = logger.getAppender("MEMORY");
        if (appender == null || !(appender instanceof MemoryAppender)) {
            throw new ServerException("ServerError", "MEMORY appender not configured");
        }
        MemoryAppender memoryAppender = (MemoryAppender) appender;
        
        // Find the logs
        LogCriteria logCriteria = new LogCriteria();
        logCriteria.setLevel(StringUtils.stripToNull(level));
        logCriteria.setTag(StringUtils.stripToNull(tag));
        logCriteria.setMessage(StringUtils.stripToNull(message));
        
        PaginatedList<LogEntry> paginatedList = PaginatedLists.create(limit, offset);
        memoryAppender.find(logCriteria, paginatedList);
        JsonObjectBuilder response = Json.createObjectBuilder();
        JsonArrayBuilder logs = Json.createArrayBuilder();
        for (LogEntry logEntry : paginatedList.getResultList()) {
            logs.add(Json.createObjectBuilder()
                    .add("date", logEntry.getTimestamp())
                    .add("level", logEntry.getLevel())
                    .add("tag", logEntry.getTag())
                    .add("message", logEntry.getMessage()));
        }
        response.add("total", paginatedList.getResultCount());
        response.add("logs", logs);
        
        return renderJson(response);
    }
    
    /**
     * Attempt to map a port to the gateway.
     * 
     * @return Response
     */
    @POST
    @Path("map_port")
    public Response mapPort() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkPrivilege(Privilege.ADMIN);
        
        if (!NetworkUtil.mapTcpPort(request.getServerPort())) {
            throw new ServerException("NetworkError", "Error mapping port using UPnP");
        }

        // Always return OK
        return okJson();
    }

    /**
     * Rebuilds the music collection index.
     *
     * @return Response
     */
    @POST
    @Path("batch/reindex")
    public Response reindex() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkPrivilege(Privilege.ADMIN);

        // Raise a directory creation event
        CollectionReindexAsyncEvent collectionReindexAsyncEvent = new CollectionReindexAsyncEvent();
        AppContext.getInstance().getCollectionEventBus().post(collectionReindexAsyncEvent);

        // Always return OK
        return okJson();
    }

    /**
     * Start the DB console.
     *
     * @return Response
     */
    @GET
    @Path("db")
    public Response db() throws Exception {
        if (!DbUtil.isStarted()) {
            DbUtil.start();
        }

        if (DbUtil.isStarted()) {
            return Response.seeOther(new URI(DbUtil.getUrl())).build();
        } else {
            return Response.serverError().build();
        }
    }
}
