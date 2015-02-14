package com.sismics.music.rest.atmosphere;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.atmosphere.client.TrackMessageSizeInterceptor;
import org.atmosphere.config.service.AtmosphereService;
import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;

import com.sismics.music.rest.resource.BaseResource;

@Path("/test")
@AtmosphereService(
        dispatch = false,
        interceptors = {AtmosphereResourceLifecycleInterceptor.class, TrackMessageSizeInterceptor.class},
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
    public void broadcast(String message) {
        AtmosphereResource r = (AtmosphereResource) request.getAttribute(ApplicationConfig.ATMOSPHERE_RESOURCE);

        if (r != null) {
            r.getBroadcaster().broadcast(message);
        } else {
            throw new IllegalStateException();
        }
    }
}
