package com.sismics.music.rest;

import com.sismics.util.filter.TokenBasedSecurityFilter;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;

/**
 * Exhaustive test of the transcoder resource.
 * 
 * @author jtremeaux
 */
public class TestTranscoderResource extends BaseJerseyTest {
    /**
     * Test the transcoder resource.
     * 
     * @throws Exception
     */
    @Test
    public void testTranscoderResource() throws Exception {
        // Login admin
        String adminAuthenticationToken = login("admin", "admin", false);

        // List all transcoders
        JsonObject json = target().path("/transcoder").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        JsonArray transcoders = json.getJsonArray("transcoders");
        Assert.assertEquals(0, transcoders.size());

        // Create a transcoder
        json = target().path("/transcoder").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("name", "mp3")
                        .param("source", "ogg")
                        .param("destination", "mp4")
                        .param("step1", "ffmpeg")), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // List all transcoders
        json = target().path("/transcoder").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        transcoders = json.getJsonArray("transcoders");
        Assert.assertEquals(1, transcoders.size());
        JsonObject transcoder0 = transcoders.getJsonObject(0);
        String transcoder0Id = transcoder0.getString("id");
        Assert.assertEquals("mp3", transcoder0.getString("name"));

        // Update a transcoder
        json = target().path("/transcoder/" + transcoder0Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .post(Entity.form(new Form()
                        .param("name", "mp3 audio")
                        .param("source", "ogg oga aac m4a flac wav wma aif aiff ape mpc shn")
                        .param("destination", "mp3")
                        .param("step1", "ffmpeg -i %s -ab %bk -v 0 -f mp3 -")), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Check the update
        json = target().path("/transcoder").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        transcoders = json.getJsonArray("transcoders");
        Assert.assertEquals(1, transcoders.size());
        transcoder0 = transcoders.getJsonObject(0);
        Assert.assertEquals("mp3 audio", transcoder0.getString("name"));

        // Delete the transcoder
        json = target().path("/transcoder/" + transcoder0Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .delete(JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Check the deletion
        json = target().path("/transcoder").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        transcoders = json.getJsonArray("transcoders");
        Assert.assertEquals(0, transcoders.size());
    }
}
