package com.sismics.music.rest;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.nio.file.Paths;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Exhaustive test of the playlist resource.
 * 
 * @author jtremeaux
 */
public class TestPlaylistResource extends BaseJerseyTest {
    /**
     * Test the default playlist resource.
     *
     */
    @Test
    public void testDefaultPlaylistResource() throws Exception {
        // Login users
        loginAdmin();

        // Admin adds a directory to the collection
        PUT("directory", ImmutableMap.of("location", Paths.get(getClass().getResource("/music/").toURI()).toString()));
        assertIsOk();

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

        // Check that the album contains some tracks
        GET("/album/" + album0Id);
        assertIsOk();
        json = getJsonResult();
        assertEquals(album0Id, json.getString("id"));
        assertEquals("Coachella 2010 Day 01 Mixtape", json.getString("name"));
        JsonObject albumArtist = json.getJsonObject("artist");
        assertEquals("[A] Proxy", albumArtist.getString("name"));
        JsonArray tracks = json.getJsonArray("tracks");
        assertNotNull(tracks);
        assertEquals(2, tracks.size());
        JsonObject track0 = tracks.getJsonObject(0);
        String track0Id = track0.getString("id");
        JsonObject track1 = tracks.getJsonObject(1);
        String track1Id = track1.getString("id");

        // Admin checks that his default playlist is empty
        GET("/playlist/default");
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        assertNotNull(tracks);
        assertEquals(0, tracks.size());

        // Admin adds a track to the playlist
        PUT("/playlist/default", ImmutableMap.of("id", track1Id));
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        assertNotNull(tracks);
        assertEquals(1, tracks.size());
        assertEquals(track1Id, tracks.getJsonObject(0).getString("id"));

        // Admin checks that his playlist contains one track
        GET("/playlist/default");
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        assertNotNull(tracks);
        assertEquals(1, tracks.size());
        assertEquals(track1Id, tracks.getJsonObject(0).getString("id"));

        // Admin adds a track to the playlist before the first one
        PUT("/playlist/default", ImmutableMap.of(
                "id", track0Id,
                "order", "0"));
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        assertNotNull(tracks);
        assertEquals(2, tracks.size());
        assertEquals(track0Id, tracks.getJsonObject(0).getString("id"));
        assertEquals(track1Id, tracks.getJsonObject(1).getString("id"));

        // Admin checks that his playlist contains 2 tracks in the right order
        GET("/playlist/default");
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        assertNotNull(tracks);
        assertEquals(2, tracks.size());
        assertEquals(track0Id, tracks.getJsonObject(0).getString("id"));
        assertEquals(track1Id, tracks.getJsonObject(1).getString("id"));

        // Admin reverses the order of the 2 tracks
        POST("/playlist/default/1/move", ImmutableMap.of("neworder", "0"));
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        assertNotNull(tracks);
        assertEquals(2, tracks.size());
        assertEquals(track1Id, tracks.getJsonObject(0).getString("id"));
        assertEquals(track0Id, tracks.getJsonObject(1).getString("id"));

        // Admin checks that his playlist contains 2 tracks in the right order
        GET("/playlist/default");
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        assertNotNull(tracks);
        assertEquals(2, tracks.size());
        assertEquals(track1Id, tracks.getJsonObject(0).getString("id"));
        assertEquals(track0Id, tracks.getJsonObject(1).getString("id"));

        // Admin removes the 1st track from the playlist
        DELETE("/playlist/default/0");
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        assertNotNull(tracks);
        assertEquals(1, tracks.size());
        assertEquals(track0Id, tracks.getJsonObject(0).getString("id"));

        // Admin checks that his playlist contains 1 track
        GET("/playlist/default");
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        assertNotNull(tracks);
        assertEquals(1, tracks.size());
        assertEquals(track0Id, tracks.getJsonObject(0).getString("id"));
        
        // Admin clears his playlist
        POST("/playlist/default/clear");
        assertIsOk();
        json = getJsonResult();
        assertEquals("ok", json.getString("status"));
        
        // Admin checks that his playlist is empty
        GET("/playlist/default");
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        assertNotNull(tracks);
        assertEquals(0, tracks.size());
        
        // Admin adds 2 tracks at the same time
        PUT("/playlist/default/multiple", ImmutableMultimap.of(
                "ids", track0Id,
                "ids", track1Id));
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        assertNotNull(tracks);
        assertEquals(2, tracks.size());
        assertEquals(track0Id, tracks.getJsonObject(0).getString("id"));
        assertEquals(track1Id, tracks.getJsonObject(1).getString("id"));
        
        // Admin checks that his playlist contains 2 tracks in the right order
        GET("/playlist/default");
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        assertNotNull(tracks);
        assertEquals(2, tracks.size());
        assertEquals(track0Id, tracks.getJsonObject(0).getString("id"));
        assertEquals(track1Id, tracks.getJsonObject(1).getString("id"));
        
        // Admin clears and adds a track to the playlist
        PUT("/playlist/default", ImmutableMap.of(
                "id", track1Id,
                "clear", "true"));
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        assertNotNull(tracks);
        assertEquals(1, tracks.size());
        
        // Admin checks that his playlist contains 1 track
        GET("/playlist/default");
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        assertNotNull(tracks);
        assertEquals(1, tracks.size());
        
        // Admin clears and adds 2 tracks at the same time
        PUT("/playlist/default/multiple", ImmutableMultimap.of(
                "ids", track0Id,
                "ids", track1Id,
                "clear", "true"));
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        assertNotNull(tracks);
        assertEquals(2, tracks.size());
        
        // Admin checks that his playlist contains 2 tracks
        GET("/playlist/default");
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        assertNotNull(tracks);
        assertEquals(2, tracks.size());
    }

    /**
     * Test the default playlist resource.
     *
     */
    @Test
    public void testNamedPlaylistResource() throws Exception {
        // Login users
        loginAdmin();

        // Admin adds a directory to the collection
        PUT("directory", ImmutableMap.of("location", Paths.get(getClass().getResource("/music/").toURI()).toString()));
        assertIsOk();

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

        // Check that the album contains some tracks
        GET("/album/" + album0Id);
        assertIsOk();
        json = getJsonResult();
        assertEquals(album0Id, json.getString("id"));
        assertEquals("Coachella 2010 Day 01 Mixtape", json.getString("name"));
        JsonObject albumArtist = json.getJsonObject("artist");
        assertEquals("[A] Proxy", albumArtist.getString("name"));
        JsonArray tracks = json.getJsonArray("tracks");
        assertNotNull(tracks);
        assertEquals(2, tracks.size());
        JsonObject track0 = tracks.getJsonObject(0);
        String track0Id = track0.getString("id");

        // List all playlists
        GET("/playlist");
        assertIsOk();
        json = getJsonResult();
        JsonArray items = json.getJsonArray("items");
        assertEquals(0, items.size());

        // Create a playlist
        PUT("/playlist", ImmutableMap.of("name", "Test playlist 0"));
        assertIsOk();
        String playlist0Id = getItemId();

        // Add a track to the playlist
        PUT("/playlist/" + playlist0Id, ImmutableMap.of("id", track0Id));
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        assertNotNull(tracks);
        assertEquals(1, tracks.size());
        assertEquals(track0Id, tracks.getJsonObject(0).getString("id"));

        // List all playlists
        GET("/playlist");
        assertIsOk();
        json = getJsonResult();
        items = json.getJsonArray("items");
        assertEquals(1, items.size());
        JsonObject item = items.getJsonObject(0);
        assertEquals(playlist0Id, item.getString("id"));
        assertEquals("Test playlist 0", item.getString("name"));

        // List all playlists
        GET("/playlist");
        assertIsOk();
        json = getJsonResult();
        items = json.getJsonArray("items");
        assertEquals(1, items.size());
        item = items.getJsonObject(0);
        assertEquals(1, item.getInt("trackCount"));
        assertEquals(0, item.getInt("userTrackPlayCount"));

        // Update a playlist
        POST("/playlist/" + playlist0Id, ImmutableMap.of("name", "Test playlist updated 0"));
        assertIsOk();

        // List all playlists
        GET("/playlist");
        assertIsOk();
        json = getJsonResult();
        items = json.getJsonArray("items");
        assertEquals(1, items.size());
        item = items.getJsonObject(0);
        assertEquals("Test playlist updated 0", item.getString("name"));

        // Load a playlist into the default playlist
        POST("/playlist/" + playlist0Id + "/load");
        assertIsOk();

        // Load a playlist into the default playlist, twice
        POST("/playlist/" + playlist0Id + "/load");
        assertIsOk();

        // Check that the playlist is loaded: OK
        GET("/playlist/default");
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        assertNotNull(tracks);
        assertEquals(2, tracks.size());
        assertEquals(track0Id, tracks.getJsonObject(0).getString("id"));
        assertEquals(track0Id, tracks.getJsonObject(1).getString("id"));

        // Load a playlist into the default playlist, clearing the old tracks
        POST("/playlist/" + playlist0Id + "/load", ImmutableMap.of("clear", "true"));
        assertIsOk();

        // Check that the playlist is loaded: OK
        GET("/playlist/default");
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        assertNotNull(tracks);
        assertEquals(1, tracks.size());
        assertEquals(track0Id, tracks.getJsonObject(0).getString("id"));

        // Marks a track as played
        POST("/player/listening", ImmutableMap.of(
                "id", track0Id,
                "date", Long.toString(new Date().getTime()),
                "duration", "60"));
        assertIsOk();

        // List all playlists
        GET("/playlist");
        assertIsOk();
        json = getJsonResult();
        items = json.getJsonArray("items");
        assertEquals(1, items.size());
        item = items.getJsonObject(0);
        assertEquals(1, item.getInt("trackCount"));
        assertEquals(1, item.getInt("userTrackPlayCount"));

        // Delete a playlist
        DELETE("/playlist/" + playlist0Id);
        assertIsOk();

        // List all playlists
        GET("/playlist");
        assertIsOk();
        json = getJsonResult();
        items = json.getJsonArray("items");
        assertEquals(0, items.size());
    }
}
