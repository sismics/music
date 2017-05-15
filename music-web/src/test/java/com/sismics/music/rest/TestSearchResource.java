package com.sismics.music.rest;

import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;

import static org.junit.Assert.assertEquals;

/**
 * Exhaustive test of the search resource.
 * 
 * @author jtremeaux
 */
public class TestSearchResource extends BaseMusicTest {
    /**
     * Test the search resource.
     *
     */
    @Test
    public void testSearchResource() throws Exception {
        // Login users
        loginAdmin();

        // Admin adds an album to the collection
        addDirectory("/music/");

        // Search by track name
        GET("/search/revolution");
        assertIsOk();
        JsonObject json = getJsonResult();
        JsonArray tracks = json.getJsonArray("tracks");
        JsonArray albums = json.getJsonArray("albums");
        JsonArray artists = json.getJsonArray("artists");
        assertEquals(1, tracks.size());
        assertEquals(0, albums.size());
        assertEquals(0, artists.size());
        assertEquals("The Revolution Will Not Be Televised", tracks.getJsonObject(0).getString("title"));
        
        // Search by album name
        GET("/search/coachella");
        assertIsOk();
        json = getJsonResult();
        albums = json.getJsonArray("albums");
        tracks = json.getJsonArray("tracks");
        artists = json.getJsonArray("artists");
        assertEquals(1, albums.size());
        assertEquals(2, tracks.size());
        assertEquals(0, artists.size());
        assertEquals("Coachella 2010 Day 01 Mixtape", albums.getJsonObject(0).getString("name"));
        
        // Search by artist name
        GET("/search/proxy");
        assertIsOk();
        json = getJsonResult();
        albums = json.getJsonArray("albums");
        tracks = json.getJsonArray("tracks");
        artists = json.getJsonArray("artists");
        assertEquals(1, artists.size());
        assertEquals(0, tracks.size());
        assertEquals(1, albums.size());
        assertEquals("[A] Proxy", artists.getJsonObject(0).getString("name"));
        
        // Search by artist name
        GET("/search/scott");
        assertIsOk();
        json = getJsonResult();
        albums = json.getJsonArray("albums");
        tracks = json.getJsonArray("tracks");
        artists = json.getJsonArray("artists");
        assertEquals(1, artists.size());
        assertEquals(1, tracks.size());
        assertEquals(0, albums.size());
        assertEquals("Gil Scott-Heron", artists.getJsonObject(0).getString("name"));

        // Search by track name : no result
        GET("/search/NOTRACK");
        assertIsOk();
        json = getJsonResult();
        assertEquals(0, json.getJsonArray("tracks").size());
        assertEquals(0, json.getJsonArray("albums").size());
        assertEquals(0, json.getJsonArray("artists").size());
    }
}
