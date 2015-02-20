package com.sismics.music.rest.atmosphere;

import com.sismics.atmosphere.interceptor.DbiTransactionInterceptor;
import com.sismics.music.core.dao.dbi.UserDao;
import com.sismics.music.core.model.dbi.User;
import com.sismics.music.rest.resource.BaseResource;
import org.atmosphere.client.TrackMessageSizeInterceptor;
import org.atmosphere.config.service.AtmosphereService;
import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

@Path("/test")
@AtmosphereService(
        dispatch = false,
        interceptors = {AtmosphereResourceLifecycleInterceptor.class, TrackMessageSizeInterceptor.class, DbiTransactionInterceptor.class},
        path = "/ws",
        servlet = "org.glassfish.jersey.servlet.ServletContainer")
public class TestResource extends BaseResource {
    @Context
    private HttpServletRequest request;

    /**
     * Echo the chat message.
     *
     * @param message
     */
    @POST
    public void broadcast(JsonObject message) {
        AtmosphereResource r = (AtmosphereResource) request.getAttribute(ApplicationConfig.ATMOSPHERE_RESOURCE);

        String userName = "anonymous";
        if (authenticate()) {
            User user = new UserDao().getActiveByUsername(principal.getId());
            userName = user.getUsername();
        }

        if (r != null) {
            r.getBroadcaster().broadcast(
                    Json.createObjectBuilder()
                    .add("author", userName)
                    .add("message", message.get("message")).build());
        } else {
            throw new IllegalStateException();
        }
    }
}
