package com.sismics.music.rest;

import java.nio.file.Paths;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;

import org.junit.Assert;
import org.junit.Test;

import com.sismics.util.filter.TokenBasedSecurityFilter;

/**
 * Exhaustive test of the track resource.
 * 
 * @author jtremeaux
 */
public class TestTrackResource extends BaseJerseyTest {
    /**
     * Test the track resource.
     *
     * @throws Exception
     */
    @Test
    public void testTrackResource() throws Exception {
        // Login users
        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);

        // Admin adds a track to the collection
        JsonObject json = target().path("/directory").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("location", Paths.get(getClass().getResource("/music/[A] Proxy - Coachella 2010 Day 01 Mixtape").toURI()).toString())), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Check that the albums are correctly added
        json = target().path("/album").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        JsonArray albums = json.getJsonArray("albums");
        Assert.assertNotNull(albums);
        Assert.assertEquals(1, albums.size());
        JsonObject album0 = albums.getJsonObject(0);
        String album0Id = album0.getString("id");
        Assert.assertNotNull(album0Id);

        // Admin checks the tracks info
        json = target().path("/album/" + album0Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        JsonArray tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(2, tracks.size());
        JsonObject track0 = tracks.getJsonObject(0);
        String track0Id = track0.getString("id");
        Assert.assertFalse(track0.getBoolean("liked"));

        // Get an track by its ID.
        target().path("/track/" + track0Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get();

        // Admin likes the track
        json = target().path("/track/" + track0Id + "/like").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .post(Entity.form(new Form()), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Admin checks the tracks info
        json = target().path("/album/" + album0Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(2, tracks.size());
        track0 = tracks.getJsonObject(0);
        Assert.assertTrue(track0.getBoolean("liked"));

        // Admin unlikes the track
        json = target().path("/track/" + track0Id + "/like").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .delete(JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Admin checks the tracks info
        json = target().path("/album/" + album0Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(2, tracks.size());
        track0 = tracks.getJsonObject(0);
        Assert.assertFalse(track0.getBoolean("liked"));

    }
}
