package com.sismics.music.application;

import javax.json.stream.JsonGenerator;
import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.jsonp.JsonProcessingFeature;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/music")
public class Application extends ResourceConfig {
    /**
     * Configure JAX-WS RS application.
     */
    public Application() {
        packages("com.sismics.music.rest.resource");
        register(JsonProcessingFeature.class);
        property(JsonGenerator.PRETTY_PRINTING, true);
    }
}
