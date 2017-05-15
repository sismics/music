package com.sismics.music.rest;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Exhaustive test of the artist resource.
 * 
 * @author bgamard
 */
public class TestArtistResource extends BaseMusicTest {
    /**
     * Test the artist resource.
     *
     */
    @Test
    public void testArtistResource() throws Exception {
        // Login users
        loginAdmin();

        // Admin adds an album to the collection
        addDirectory("/music/");

        // Check that the artists are correctly added
        GET("/artist", ImmutableMap.of(
                "sort_column", "0",
                "asc", "true"));
        assertIsOk();
        JsonObject json = getJsonResult();
        assertEquals(4, json.getJsonNumber("total").intValue());
        JsonArray artists = json.getJsonArray("artists");
        assertNotNull(artists);
        assertEquals(4, artists.size());
        JsonObject artist0 = artists.getJsonObject(0);
        String artist0Id = artist0.getString("id");
        assertNotNull(artist0Id);
        assertNotNull(artist0.getString("name"));
        
        // Get an artist details
        GET("/artist/" + artist0Id);
        assertIsOk();
        json = getJsonResult();
        assertEquals("Gil Scott-Heron", json.getString("name"));
        JsonArray albums = json.getJsonArray("albums");
        assertEquals(0, albums.size());
        JsonArray tracks = json.getJsonArray("tracks");
        assertEquals(1, tracks.size());
    }
}
