package com.sismics.music.rest;

import com.sismics.util.filter.TokenBasedSecurityFilter;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import java.nio.file.Paths;
import java.util.Date;

/**
 * Exhaustive test of the player resource.
 * 
 * @author jtremeaux
 */
public class TestPlayerResource extends BaseJerseyTest {
    /**
     * Test the track resource.
     *
     * @throws Exception
     */
    @Test
    public void testPlayerResource() throws Exception {
        // Login users
        String adminAuthenticationToken = login("admin", "admin", false);

        // Admin adds a track to the collection
        JsonObject json = target().path("/directory").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("location", Paths.get(getClass().getResource("/music/").toURI()).toString())), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Check that the albums are correctly added
        json = target().path("/album")
                .queryParam("sort_column", "0")
                .queryParam("asc", "false")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        JsonArray albums = json.getJsonArray("albums");
        Assert.assertNotNull(albums);
        Assert.assertEquals(2, albums.size());
        JsonObject album0 = albums.getJsonObject(1);
        String album0Id = album0.getString("id");
        Assert.assertNotNull(album0Id);

        // Check the tracks info
        json = target().path("/album/" + album0Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        JsonArray tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(2, tracks.size());
        JsonObject track0 = tracks.getJsonObject(0);
        String track0Id = track0.getString("id");
        Integer track0Length = track0.getInt("length");
        Assert.assertEquals(0, track0.getInt("play_count"));

        // Marks a track as played
        json = target().path("/player/listening").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .post(Entity.form(new Form()
                        .param("id", track0Id)
                        .param("date", Long.toString(new Date().getTime()))
                        .param("duration", Integer.toString(track0Length / 2 + 1))), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Check the tracks info
        json = target().path("/album/" + album0Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(2, tracks.size());
        track0 = tracks.getJsonObject(0);
        Assert.assertEquals(1, json.getInt("play_count"));
        Assert.assertEquals(1, track0.getInt("play_count"));
        
        // Marks tracks as played
        json = target().path("/player/listened").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .post(Entity.form(new Form()
                        .param("id", track0Id)
                        .param("date", Long.toString(new Date().getTime()))), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        
        // Check the tracks info
        json = target().path("/album/" + album0Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(2, tracks.size());
        track0 = tracks.getJsonObject(0);
        Assert.assertEquals(2, json.getInt("play_count"));
        Assert.assertEquals(2, track0.getInt("play_count"));
    }
    
    /**
     * Test remote control resources.
     * 
     * @throws Exception
     */
    @Test
    public void testRemoteControl() throws Exception {
        // Login users
        String adminAuthenticationToken = login("admin", "admin", false);
        
        // Register a player
        JsonObject json = target().path("/player/register").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .post(null, JsonObject.class);
        String playerToken = json.getString("token");
        Assert.assertNotNull(playerToken);
        
        // Unregister a player
        json = target().path("/player/register").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .post(Entity.form(new Form()
                        .param("token", playerToken)), JsonObject.class);
    }
}
