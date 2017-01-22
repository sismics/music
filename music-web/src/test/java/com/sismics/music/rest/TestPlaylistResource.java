package com.sismics.music.rest;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.nio.file.Paths;

/**
 * Exhaustive test of the playlist resource.
 * 
 * @author jtremeaux
 */
public class TestPlaylistResource extends BaseJerseyTest {
    /**
     * Test the playlist resource.
     *
     */
    @Test
    public void testPlaylistResource() throws Exception {
        // Login users
        login("admin", "admin", false);

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
        Assert.assertNotNull(albums);
        Assert.assertEquals(2, albums.size());
        JsonObject album0 = albums.getJsonObject(1);
        String album0Id = album0.getString("id");
        Assert.assertNotNull(album0Id);

        // Check that the album contains some tracks
        GET("/album/" + album0Id);
        assertIsOk();
        json = getJsonResult();
        Assert.assertEquals(album0Id, json.getString("id"));
        Assert.assertEquals("Coachella 2010 Day 01 Mixtape", json.getString("name"));
        JsonObject albumArtist = json.getJsonObject("artist");
        Assert.assertEquals("[A] Proxy", albumArtist.getString("name"));
        JsonArray tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(2, tracks.size());
        JsonObject track0 = tracks.getJsonObject(0);
        String track0Id = track0.getString("id");
        JsonObject track1 = tracks.getJsonObject(1);
        String track1Id = track1.getString("id");

        // Admin checks that his playlist is empty
        GET("/playlist");
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(0, tracks.size());

        // Admin adds a track to the playlist
        PUT("/playlist", ImmutableMap.of("id", track1Id));
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(1, tracks.size());
        Assert.assertEquals(track1Id, tracks.getJsonObject(0).getString("id"));

        // Admin checks that his playlist contains one track
        GET("/playlist");
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(1, tracks.size());
        Assert.assertEquals(track1Id, tracks.getJsonObject(0).getString("id"));

        // Admin adds a track to the playlist before the first one
        PUT("/playlist", ImmutableMap.of(
                "id", track0Id,
                "order", "0"));
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(2, tracks.size());
        Assert.assertEquals(track0Id, tracks.getJsonObject(0).getString("id"));
        Assert.assertEquals(track1Id, tracks.getJsonObject(1).getString("id"));

        // Admin checks that his playlist contains 2 tracks in the right order
        GET("/playlist");
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(2, tracks.size());
        Assert.assertEquals(track0Id, tracks.getJsonObject(0).getString("id"));
        Assert.assertEquals(track1Id, tracks.getJsonObject(1).getString("id"));

        // Admin reverses the order of the 2 tracks
        POST("/playlist/1/move", ImmutableMap.of("neworder", "0"));
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(2, tracks.size());
        Assert.assertEquals(track1Id, tracks.getJsonObject(0).getString("id"));
        Assert.assertEquals(track0Id, tracks.getJsonObject(1).getString("id"));

        // Admin checks that his playlist contains 2 tracks in the right order
        GET("/playlist");
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(2, tracks.size());
        Assert.assertEquals(track1Id, tracks.getJsonObject(0).getString("id"));
        Assert.assertEquals(track0Id, tracks.getJsonObject(1).getString("id"));

        // Admin removes the 1st track from the playlist
        DELETE("/playlist/0");
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(1, tracks.size());
        Assert.assertEquals(track0Id, tracks.getJsonObject(0).getString("id"));

        // Admin checks that his playlist contains 1 track
        GET("/playlist");
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(1, tracks.size());
        Assert.assertEquals(track0Id, tracks.getJsonObject(0).getString("id"));
        
        // Admin clears his playlist
        DELETE("/playlist");
        assertIsOk();
        json = getJsonResult();
        Assert.assertEquals("ok", json.getString("status"));
        
        // Admin checks that his playlist is empty
        GET("/playlist");
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(0, tracks.size());
        
        // Admin adds 2 tracks at the same time
        PUT("/playlist/multiple", ImmutableMultimap.of(
                "ids", track0Id,
                "ids", track1Id));
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(2, tracks.size());
        Assert.assertEquals(track0Id, tracks.getJsonObject(0).getString("id"));
        Assert.assertEquals(track1Id, tracks.getJsonObject(1).getString("id"));
        
        // Admin checks that his playlist contains 2 tracks in the right order
        GET("/playlist");
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(2, tracks.size());
        Assert.assertEquals(track0Id, tracks.getJsonObject(0).getString("id"));
        Assert.assertEquals(track1Id, tracks.getJsonObject(1).getString("id"));
        
        // Admin clears and adds a track to the playlist
        PUT("/playlist", ImmutableMap.of(
                "id", track1Id,
                "clear", "true"));
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(1, tracks.size());
        
        // Admin checks that his playlist contains 1 track
        GET("/playlist");
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(1, tracks.size());
        
        // Admin clears and adds 2 tracks at the same time
        PUT("/playlist/multiple", ImmutableMultimap.of(
                "ids", track0Id,
                "ids", track1Id,
                "clear", "true"));
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(2, tracks.size());
        
        // Admin checks that his playlist contains 2 tracks
        GET("/playlist");
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(2, tracks.size());
    }
}
