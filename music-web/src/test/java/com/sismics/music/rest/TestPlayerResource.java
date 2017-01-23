package com.sismics.music.rest;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;
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
     */
    @Test
    public void testPlayerResource() throws Exception {
        // Login users
        loginAdmin();

        // Admin adds a track to the collection
        PUT("/directory", ImmutableMap.of("location", Paths.get(getClass().getResource("/music/").toURI()).toString()));
        assertIsOk();

        // Check that the albums are correctly added
        GET("/album", ImmutableMap.of(
                "sort_column", "0",
                "asc", "false"));
        assertIsOk();
        JsonObject json = getJsonResult();
        JsonArray albums = json.getJsonArray("albums");
        Assert.assertNotNull(albums);
        Assert.assertEquals(2, albums.size());
        JsonObject album0 = albums.getJsonObject(1);
        String album0Id = album0.getString("id");
        Assert.assertNotNull(album0Id);

        // Check the tracks info
        GET("/album/" + album0Id);
        assertIsOk();
        json = getJsonResult();
        JsonArray tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(2, tracks.size());
        JsonObject track0 = tracks.getJsonObject(0);
        String track0Id = track0.getString("id");
        Integer track0Length = track0.getInt("length");
        Assert.assertEquals(0, track0.getInt("play_count"));

        // Marks a track as played
        POST("/player/listening", ImmutableMap.of(
                "id", track0Id,
                "date", Long.toString(new Date().getTime()),
                "duration", Integer.toString(track0Length / 2 + 1)));
        assertIsOk();

        // Check the tracks info
        GET("/album/" + album0Id);
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(2, tracks.size());
        track0 = tracks.getJsonObject(0);
        Assert.assertEquals(1, json.getInt("play_count"));
        Assert.assertEquals(1, track0.getInt("play_count"));
        
        // Marks tracks as played
        POST("/player/listened", ImmutableMap.of(
                "id", track0Id,
                "date", Long.toString(new Date().getTime())));
        assertIsOk();

        // Check the tracks info
        GET("/album/" + album0Id);
        assertIsOk();
        json = getJsonResult();
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
     */
    @Test
    public void testRemoteControl() throws Exception {
        // Login users
        loginAdmin();

        // Register a player
        POST("/player/register");
        assertIsOk();
        JsonObject json = getJsonResult();
        String playerToken = json.getString("token");
        Assert.assertNotNull(playerToken);
        
        // Unregister a player
        POST("/player/register", ImmutableMap.of("token", playerToken));
        assertIsOk();
    }
}
