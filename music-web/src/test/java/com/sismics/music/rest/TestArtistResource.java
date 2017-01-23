package com.sismics.music.rest;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.nio.file.Paths;

/**
 * Exhaustive test of the artist resource.
 * 
 * @author bgamard
 */
public class TestArtistResource extends BaseJerseyTest {
    /**
     * Test the artist resource.
     *
     */
    @Test
    public void testArtistResource() throws Exception {
        // Login users
        loginAdmin();

        // Admin adds an album to the collection
        PUT("/directory", ImmutableMap.of("location", Paths.get(getClass().getResource("/music/").toURI()).toString()));
        assertIsOk();
        JsonObject json = getJsonResult();
        Assert.assertEquals("ok", json.getString("status"));

        // Check that the artists are correctly added
        GET("/artist", ImmutableMap.of(
                "sort_column", "0",
                "asc", "true"));
        assertIsOk();
        json = getJsonResult();
        Assert.assertEquals(4, json.getJsonNumber("total").intValue());
        JsonArray artists = json.getJsonArray("artists");
        Assert.assertNotNull(artists);
        Assert.assertEquals(4, artists.size());
        JsonObject artist0 = artists.getJsonObject(0);
        String artist0Id = artist0.getString("id");
        Assert.assertNotNull(artist0Id);
        Assert.assertNotNull(artist0.getString("name"));
        
        // Get an artist details
        GET("/artist/" + artist0Id);
        assertIsOk();
        json = getJsonResult();
        Assert.assertEquals("Gil Scott-Heron", json.getString("name"));
        JsonArray albums = json.getJsonArray("albums");
        Assert.assertEquals(0, albums.size());
        JsonArray tracks = json.getJsonArray("tracks");
        Assert.assertEquals(1, tracks.size());
    }
}
