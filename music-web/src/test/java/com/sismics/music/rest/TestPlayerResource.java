package com.sismics.music.rest;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Exhaustive test of the player resource.
 * 
 * @author jtremeaux
 */
public class TestPlayerResource extends BaseMusicTest {
    /**
     * Test the track resource.
     *
     */
    @Test
    public void testPlayerResource() throws Exception {
        // Login users
        loginAdmin();

        // Admin adds a track to the collection
        addDirectory("/music/");

        // Check that the albums are correctly added
        GET("/album", ImmutableMap.of(
                "sort_column", "0",
                "asc", "false"));
        assertIsOk();
        JsonObject json = getJsonResult();
        JsonArray albums = json.getJsonArray("albums");
        assertNotNull(albums);
        assertEquals(2, albums.size());
        JsonObject album0 = albums.getJsonObject(1);
        String album0Id = album0.getString("id");
        assertNotNull(album0Id);

        // Check the tracks info
        GET("/album/" + album0Id);
        assertIsOk();
        json = getJsonResult();
        JsonArray tracks = json.getJsonArray("tracks");
        assertNotNull(tracks);
        assertEquals(2, tracks.size());
        JsonObject track0 = tracks.getJsonObject(0);
        String track0Id = track0.getString("id");
        Integer track0Length = track0.getInt("length");
        assertEquals(0, track0.getInt("play_count"));

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
        assertNotNull(tracks);
        assertEquals(2, tracks.size());
        track0 = tracks.getJsonObject(0);
        assertEquals(1, json.getInt("play_count"));
        assertEquals(1, track0.getInt("play_count"));
        
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
        assertNotNull(tracks);
        assertEquals(2, tracks.size());
        track0 = tracks.getJsonObject(0);
        assertEquals(2, json.getInt("play_count"));
        assertEquals(2, track0.getInt("play_count"));
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
        assertNotNull(playerToken);
        
        // Unregister a player
        POST("/player/register", ImmutableMap.of("token", playerToken));
        assertIsOk();
    }
}
