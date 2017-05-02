package com.sismics.music.rest;

import com.google.common.collect.ImmutableMap;
import org.junit.Ignore;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;

import static org.junit.Assert.*;

/**
 * Exhaustive test of the album art resource.
 * 
 * @author jtremeaux
 */
public class TestAlbumArtResource extends BaseMusicTest {
    /**
     * Test the album list.
     *
     */
    @Test
    @Ignore
    public void shouldSearchAlbumArt() throws Exception {
        // Login users
        loginAdmin();

        // Search some album art: OK
        GET("/albumart/search", ImmutableMap.of(
                "query", "Drake More Life"));
        assertIsOk();
        JsonObject json = getJsonResult();
        JsonArray albumArts = json.getJsonArray("albumArts");
        assertFalse(albumArts.isEmpty());
        JsonObject albumArt = albumArts.getJsonObject(0);
        assertNotNull(albumArt.get("url"));
        assertEquals(300, albumArt.getInt("width"));
        assertEquals(300, albumArt.getInt("height"));
    }
}
