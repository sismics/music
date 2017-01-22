package com.sismics.music.rest;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Exhaustive test of the album resource.
 * 
 * @author jtremeaux
 */
public class TestAlbumResource extends BaseJerseyTest {
    /**
     * Test the album resource.
     *
     */
    @Test
    public void testAlbumResource() throws Exception {
        // Login users
        login("admin", "admin", false);

        // Admin adds an album to the collection
        PUT("/directory", ImmutableMap.of("location", Paths.get(getClass().getResource("/music/").toURI()).toString()));
        assertIsOk();

        // Check that the albums are correctly added
        GET("/album", ImmutableMap.of(
                "sort_column", "0",
                "asc", "false"));
        assertIsOk();
        JsonObject json = getJsonResult();
        Assert.assertEquals(2, json.getJsonNumber("total").intValue());
        JsonArray albums = json.getJsonArray("albums");
        Assert.assertNotNull(albums);
        Assert.assertEquals(2, albums.size());
        JsonObject album0 = albums.getJsonObject(1);
        String album0Id = album0.getString("id");
        JsonObject artist0 = album0.getJsonObject("artist");
        Assert.assertNotNull(album0Id);
        Assert.assertNotNull(album0.getString("name"));
        Assert.assertNotNull(album0.getBoolean("albumart"));
        Assert.assertNotNull(album0.getJsonNumber("update_date").longValue());
        Assert.assertNotNull(artist0.getString("id"));

        // Get an album by its ID
        GET("/album/" + album0Id);
        assertIsOk();
        json = getJsonResult();
        Assert.assertEquals(album0Id, json.getString("id"));
        Assert.assertEquals("Coachella 2010 Day 01 Mixtape", json.getString("name"));
        Assert.assertTrue(json.getBoolean("albumart"));
        JsonObject albumArtist = json.getJsonObject("artist");
        Assert.assertEquals("[A] Proxy", albumArtist.getString("name"));
        JsonArray tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(2, tracks.size());
        JsonObject track0 = tracks.getJsonObject(0);
        Assert.assertEquals(1, track0.getInt("order"));
        JsonObject artist = track0.getJsonObject("artist");
        Assert.assertEquals("Gil Scott-Heron", artist.getString("name"));

        // Get an album art
        GET("/album/" + album0Id + "/albumart/small");
        assertIsOk();

        // Get an album art
        GET("/album/" + album0Id + "/albumart/large");
        assertIsOk();
        
        // Get an album art
        GET("/album/" + album0Id + "/albumart/medium");
        assertIsOk();

        // Get an album art: KO, this size doesn't exist
        GET("/album/" + album0Id + "/albumart/huge");
        assertIsNotFound();
        
        // Update an album art
        POST("/album/" + album0Id + "/albumart", ImmutableMap.of("url", "http://lorempixel.com/200/200/"));
        assertIsOk();
        json = getJsonResult();
        Assert.assertNull(json.get("message"));
        
        // Get an album art
        GET("/album/" + album0Id + "/albumart/large");
        assertIsOk();
        
        // Check that the original file has been copied to the collection
        Path musicPath = Paths.get(getClass().getResource("/music/").toURI());
        File albumArtFile = musicPath.resolve(Paths.get("[A] Proxy - Coachella 2010 Day 01 Mixtape", "albumart.jpg")).toFile();
        Assert.assertTrue(albumArtFile.exists());
        
        // Make the album art not writable
        albumArtFile.setWritable(false);
        
        // Update an album art
        POST("/album/" + album0Id + "/albumart", ImmutableMap.of("url", "http://lorempixel.com/200/200/"));
        assertIsOk();
        json = getJsonResult();
        Assert.assertEquals("AlbumArtNotCopied", json.getString("message"));
        albumArtFile.setWritable(true);
        
        // Get an album art
        GET("/album/" + album0Id + "/albumart/large");
        assertIsOk();
    }
}
