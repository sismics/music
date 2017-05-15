package com.sismics.music.rest;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Exhaustive test of the track resource.
 * 
 * @author jtremeaux
 */
public class TestTrackResource extends BaseMusicTest {
    /**
     * Test the track resource.
     *
     */
    @Test
    public void testTrackResource() throws Exception {
        // Login users
        loginAdmin();

        // This test is destructive, copy the test music to a temporary directory
        Path sourceDir = Paths.get(getClass().getResource("/music/").toURI());
        File destDir = Files.createTempDir();
        FileUtils.copyDirectory(sourceDir.toFile(), destDir);
        destDir.deleteOnExit();
        
        // Admin adds a directory to the collection
        addDirectory("/music/");

        // Check that the albums are correctly added
        GET("/album", ImmutableMap.of(
                "sort_column", "0",
                "asc", "false"));
        assertIsOk();
        JsonObject json = getJsonResult();
        JsonArray albums = json.getJsonArray("albums");
        assertEquals(2, albums.size());
        JsonObject album0 = albums.getJsonObject(0);
        String album0Id = album0.getString("id");
        assertNotNull(album0Id);

        // Admin checks the tracks info
        GET("/album/" + album0Id);
        assertIsOk();
        json = getJsonResult();
        JsonArray tracks = json.getJsonArray("tracks");
        assertEquals(1, tracks.size());
        JsonObject track0 = tracks.getJsonObject(0);
        String track0Id = track0.getString("id");
        assertFalse(track0.getBoolean("liked"));

        // Get an track by its ID (without transcoder)
        GET("/track/" + track0Id);
        assertIsOk();

        // Create a transcoder
        PUT("/transcoder", ImmutableMap.of(
                "name", "mp3",
                "source", "ogg oga aac wav wma aif flac mp3",
                "destination", "mp3",
                "step1", "ffmpeg -ss %ss -i %s -ab %bk -v 0 -f mp3 -"));
        assertIsOk();

        // Get an track by its ID (with transcoder)
        GET("/track/" + track0Id);
        // assertEquals(Status.OK.getStatusCode(), response.getStatus()); // No ffmpeg on Travis :(
        
        // Admin likes the track
        POST("/track/" + track0Id + "/like");
        assertIsOk();

        // Admin checks the tracks info
        GET("/album/" + album0Id);
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        assertEquals(1, tracks.size());
        track0 = tracks.getJsonObject(0);
        assertTrue(track0.getBoolean("liked"));

        // Admin unlikes the track
        DELETE("/track/" + track0Id + "/like");
        assertIsOk();

        // Admin checks the tracks info
        GET("/album/" + album0Id);
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        assertEquals(1, tracks.size());
        track0 = tracks.getJsonObject(0);
        assertFalse(track0.getBoolean("liked"));

        // Admin update a track info
        POST("/track/"+ track0Id, ImmutableMap.<String, String>builder()
                .put("order", "1")
                .put("title", "My fake title")
                .put("album", "My fake album")
                .put("artist", "My fake artist")
                .put("album_artist", "My fake album artist")
                .put("year", "2014")
                .put("genre", "Pop")
                .build());
        assertIsOk();

        // Admin checks the tracks info
        GET("/album/" + album0Id);
        assertIsOk();
        json = getJsonResult();
        tracks = json.getJsonArray("tracks");
        assertNotNull(tracks);
        assertEquals(1, tracks.size());
        
        // Admin checks the new album
        GET("/album");
        assertIsOk();
        json = getJsonResult();
        albums = json.getJsonArray("albums");
        assertEquals(2, albums.size());
        
        // Admin update a track info with minimal data
        POST("/track/"+ track0Id, ImmutableMap.of(
                "title", "Imagine",
                "album", "My fake album",
                "artist", "John Lennon",
                "album_artist", "My fake album artist"));
        assertIsOk();
        
        // Admin checks the albums
        GET("/album");
        assertIsOk();
        json = getJsonResult();
        albums = json.getJsonArray("albums");
        assertEquals(2, albums.size());
        
        // Admin get the lyrics
        GET("/track/" + track0Id + "/lyrics");
        assertIsOk();
        json = getJsonResult();
        assertTrue(json.getJsonArray("lyrics").getString(0).contains("Imagine no possessions"));
    }
}
