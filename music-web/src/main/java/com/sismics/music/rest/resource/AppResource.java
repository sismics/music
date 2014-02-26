package com.sismics.music.rest.resource;

import javax.ws.rs.Path;

/**
 * General app REST resource.
 * 
 * @author jtremeaux
 */
@Path("/app")
public class AppResource extends BaseResource {
//    /**
//     * Return the information about the application.
//     * 
//     * @return Response
//     * @throws JSONException
//     */
//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response version() throws JSONException {
//        ResourceBundle configBundle = ConfigUtil.getConfigBundle();
//        String currentVersion = configBundle.getString("api.current_version");
//        String minVersion = configBundle.getString("api.min_version");
//
//        JSONObject response = new JSONObject();
//        response.put("current_version", currentVersion.replace("-SNAPSHOT", ""));
//        response.put("min_version", minVersion);
//        response.put("total_memory", Runtime.getRuntime().totalMemory());
//        response.put("free_memory", Runtime.getRuntime().freeMemory());
//        return Response.ok().entity(response).build();
//    }
//    
//    /**
//     * Retrieve the application logs.
//     * 
//     * @param level Filter on logging level
//     * @param tag Filter on logger name / tag
//     * @param message Filter on message
//     * @param limit Page limit
//     * @param offset Page offset
//     * @return Response
//     * @throws JSONException
//     */
//    @GET
//    @Path("log")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response log(
//            @QueryParam("level") String level,
//            @QueryParam("tag") String tag,
//            @QueryParam("message") String message,
//            @QueryParam("limit") Integer limit,
//            @QueryParam("offset") Integer offset) throws JSONException {
//        if (!authenticate()) {
//            throw new ForbiddenClientException();
//        }
//        checkBaseFunction(BaseFunction.ADMIN);
//
//        // Get the memory appender
//        Logger logger = Logger.getRootLogger();
//        Appender appender = logger.getAppender("MEMORY");
//        if (appender == null || !(appender instanceof MemoryAppender)) {
//            throw new ServerException("ServerError", "MEMORY appender not configured");
//        }
//        MemoryAppender memoryAppender = (MemoryAppender) appender;
//        
//        // Find the logs
//        LogCriteria logCriteria = new LogCriteria();
//        logCriteria.setLevel(StringUtils.stripToNull(level));
//        logCriteria.setTag(StringUtils.stripToNull(tag));
//        logCriteria.setMessage(StringUtils.stripToNull(message));
//        
//        PaginatedList<LogEntry> paginatedList = PaginatedLists.create(limit, offset);
//        memoryAppender.find(logCriteria, paginatedList);
//        JSONObject response = new JSONObject();
//        List<JSONObject> logs = new ArrayList<JSONObject>();
//        for (LogEntry logEntry : paginatedList.getResultList()) {
//            JSONObject log = new JSONObject();
//            log.put("date", logEntry.getTimestamp());
//            log.put("level", logEntry.getLevel());
//            log.put("tag", logEntry.getTag());
//            log.put("message", logEntry.getMessage());
//            logs.add(log);
//        }
//        response.put("total", paginatedList.getResultCount());
//        response.put("logs", logs);
//        
//        return Response.ok().entity(response).build();
//    }
//    
//    /**
//     * Attempt to map a port to the gateway.
//     * 
//     * @return Response
//     * @throws JSONException
//     */
//    @POST
//    @Path("map_port")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response mapPort() throws JSONException {
//        if (!authenticate()) {
//            throw new ForbiddenClientException();
//        }
//        checkBaseFunction(BaseFunction.ADMIN);
//        
//        if (!NetworkUtil.mapTcpPort(request.getServerPort())) {
//            throw new ServerException("NetworkError", "Error mapping port using UPnP");
//        }
//
//        // Always return OK
//        JSONObject response = new JSONObject();
//        response.put("status", "ok");
//        return Response.ok().entity(response).build();
//    }
//
//    /**
//     * Rebuilds the music collection index.
//     *
//     * @return Response
//     * @throws JSONException
//     */
//    @POST
//    @Path("batch/reindex")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response reindex() throws JSONException {
//        if (!authenticate()) {
//            throw new ForbiddenClientException();
//        }
//        checkBaseFunction(BaseFunction.ADMIN);
//
//        // Raise a directory creation event
//        CollectionReindexAsyncEvent collectionReindexAsyncEvent = new CollectionReindexAsyncEvent();
//        AppContext.getInstance().getCollectionEventBus().post(collectionReindexAsyncEvent);
//
//        // Always return OK
//        JSONObject response = new JSONObject();
//        response.put("status", "ok");
//        return Response.ok().entity(response).build();
//    }
}
