package com.sismics.music.rest.atmosphere;

import com.sismics.atmosphere.interceptor.DbiTransactionInterceptor;
import com.sismics.music.core.dao.dbi.PlayerDao;
import com.sismics.music.core.model.dbi.Player;
import com.sismics.music.rest.resource.BaseResource;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.Validation;
import org.atmosphere.client.TrackMessageSizeInterceptor;
import org.atmosphere.config.service.AtmosphereService;
import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonReader;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.StringReader;

/**
 * Player resource, managed by Atmosphere.
 * 
 * @author bgamard
 */
@Path("/player")
@AtmosphereService(
        dispatch = true,
        interceptors = {AtmosphereResourceLifecycleInterceptor.class, TrackMessageSizeInterceptor.class, DbiTransactionInterceptor.class},
        path = "/ws",
        servlet = "org.glassfish.jersey.servlet.ServletContainer")
public class PlayerResource extends BaseResource {
    @Context
    private HttpServletRequest request;

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(PlayerResource.class);

    /**
     * Connect a player by websocket.
     * 
     * @param token Player token
     */
    @GET
    public void suspend(@QueryParam("token") String token) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        Validation.required(token, "token");
        
        // Get the player
        PlayerDao playerDao = new PlayerDao();
        Player player = playerDao.getById(token);
        if (player == null) {
            throw new ClientException("PlayerNotFound", "Player not found: " + token);
        }
        
        // Set the resource to the right broadcaster
        AtmosphereResource atmosphereResource = (AtmosphereResource) request.getAttribute(ApplicationConfig.ATMOSPHERE_RESOURCE);
        @SuppressWarnings("deprecation")
        Broadcaster lookup = BroadcasterFactory.getDefault().lookup(token, true);
        atmosphereResource.setBroadcaster(lookup);
    }
    
    /**
     * Send a command to a player.
     *
     */
    @POST
    @Path("command")
    public Response command(
            @FormParam("token") String token,
            @FormParam("json") String json) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        Validation.required(token, "token");
        Validation.required(json, "json");
        

        // Get the player
        PlayerDao playerDao = new PlayerDao();
        Player player = playerDao.getById(token);
        if (player == null) {
            throw new ClientException("PlayerNotFound", "Player not found: " + token);
        }
        
        // Check if a player is connected
        @SuppressWarnings("deprecation")
        Broadcaster broadcaster = BroadcasterFactory.getDefault().lookup(token, true);
        if (broadcaster.getAtmosphereResources().size() == 0) {
            throw new ClientException("PlayerNotConnected", "Player not connected: " + token);
        }
        
        // Parse and broadcast the JSON command
        try (JsonReader jsonReader = Json.createReader(new StringReader(json))) {
            broadcaster.broadcast(jsonReader.readObject());
        } catch (JsonException e) {
            log.error("Command not parsable: " + json, e);
            throw new ClientException("CommandError", "Command not parsable: " + json);
        }
        
        // Always return OK
        return okJson();
    }
}
