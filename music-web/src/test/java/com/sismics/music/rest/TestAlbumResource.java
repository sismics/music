package com.sismics.music.rest;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.sismics.util.filter.TokenBasedSecurityFilter;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.junit.Ignore;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * Exhaustive test of the album resource.
 * 
 * @author jtremeaux
 */
public class TestAlbumResource extends BaseMusicTest {
    /**
     * Test the album list.
     *
     */
    @Test
    public void shouldListAlbum() throws Exception {
        // Login users
        loginAdmin();

        // Admin adds an album to the collection
        addDirectory("/music/");

        // Check that the albums are correctly added
        GET("/album", ImmutableMap.of(
                "sort_column", "0",
                "asc", "false"));
        assertIsOk();
        JsonObject json = getJsonResult();
        assertEquals(2, json.getJsonNumber("total").intValue());
        JsonArray albums = json.getJsonArray("albums");
        assertNotNull(albums);
        assertEquals(2, albums.size());
        JsonObject album0 = albums.getJsonObject(1);
        String album0Id = album0.getString("id");
        JsonObject artist0 = album0.getJsonObject("artist");
        assertNotNull(album0Id);
        assertNotNull(album0.getString("name"));
        assertNotNull(album0.getBoolean("albumart"));
        assertNotNull(album0.getJsonNumber("update_date").longValue());
        assertNotNull(artist0.getString("id"));
    }

    /**
     * Test the album art upload.
     *
     */
    @Test
    @Ignore // TODO Fixme
    public void shouldUploadAlbumArtDirectly() throws Exception {
        // Login users
        String adminAuthenticationToken = loginAdmin();

        // Admin adds an album to the collection
        addDirectory("/music_onetrack_noart/");

        // Check that the albums are correctly added
        GET("/album", ImmutableMap.of(
                "sort_column", "0",
                "asc", "false"));
        assertIsOk();
        JsonObject json = getJsonResult();
        JsonArray albums = json.getJsonArray("albums");
        assertNotNull(albums);
        assertEquals(1, albums.size());
        JsonObject album0 = albums.getJsonObject(0);
        String album0Id = album0.getString("id");
        assertNotNull(album0Id);
        assertFalse(album0.getBoolean("albumart"));

        // Update an album art
        try (InputStream is = Resources.getResource("music/[A] Proxy - Coachella 2010 Day 01 Mixtape/albumart.png").openStream()) {
            StreamDataBodyPart streamDataBodyPart = new StreamDataBodyPart("file", is, "albumart.png");
            json = target()
                    .register(MultiPartFeature.class)
                    .path("/album/" + album0Id + "/albumart").request()
                    .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                    .put(Entity.entity(new FormDataMultiPart().bodyPart(streamDataBodyPart),
                            MediaType.MULTIPART_FORM_DATA_TYPE), JsonObject.class);
            assertEquals("ok", json.getString("status"));
        }

        // Check that the album art is correctly added
        GET("/album", ImmutableMap.of(
                "sort_column", "0",
                "asc", "false"));
        assertIsOk();
        json = getJsonResult();
        albums = json.getJsonArray("albums");
        assertNotNull(albums);
        assertEquals(1, albums.size());
        album0 = albums.getJsonObject(0);
        album0Id = album0.getString("id");
        assertNotNull(album0Id);
        assertTrue(album0.getBoolean("albumart"));

        // Check that the album art is correctly added
        GET("/album/" + album0Id + "/albumart/large");
        assertIsOk();
    }

    /**
     * Test the album art download from an URL.
     *
     */
    @Test
    public void shouldUploadAlbumArtFromUrl() throws Exception {
        // Login users
        loginAdmin();

        // Admin adds an album to the collection
        File tempDirectory = addDirectory("/music/");

        // Check that the albums are correctly added
        GET("/album", ImmutableMap.of(
                "sort_column", "0",
                "asc", "false"));
        assertIsOk();
        JsonObject json = getJsonResult();
        assertEquals(2, json.getJsonNumber("total").intValue());
        JsonArray albums = json.getJsonArray("albums");
        assertNotNull(albums);
        assertEquals(2, albums.size());
        JsonObject album0 = albums.getJsonObject(1);
        String album0Id = album0.getString("id");
        JsonObject artist0 = album0.getJsonObject("artist");
        assertNotNull(album0Id);
        assertTrue(album0.getBoolean("albumart"));

        // Get an album art
        GET("/album/" + album0Id + "/albumart/small");
        assertIsOk();

        // Get an album art
        GET("/album/" + album0Id + "/albumart/medium");
        assertIsOk();

        // Get an album art
        GET("/album/" + album0Id + "/albumart/large");
        assertIsOk();

        // Get an album art: KO, this size doesn't exist
        GET("/album/" + album0Id + "/albumart/huge");
        assertIsNotFound();

        // Update an album art
        POST("/album/" + album0Id + "/albumart/fromurl", ImmutableMap.of("url", getFakeHttpUri() + "/lorempixel/200x200.jpg"));
        assertIsOk();
        json = getJsonResult();
        assertNull(json.get("message"));

        // Get an album art
        GET("/album/" + album0Id + "/albumart/large");
        assertIsOk();

        // Check that the original file has been copied to the collection
        File albumArtFile = new File(tempDirectory.getAbsolutePath() + "/" + "[A] Proxy - Coachella 2010 Day 01 Mixtape/albumart.jpg");
        assertTrue(albumArtFile.exists());

        // Make the album art not writable
        assertTrue(albumArtFile.setWritable(false));

        // Update an album art
        POST("/album/" + album0Id + "/albumart/fromurl", ImmutableMap.of("url", getFakeHttpUri() + "/lorempixel/200x200.jpg"));
        assertIsOk();
        json = getJsonResult();
        assertEquals("AlbumArtNotCopied", json.getString("message"));
        assertTrue(albumArtFile.setWritable(true));

        // Get an album art
        GET("/album/" + album0Id + "/albumart/large");
        assertIsOk();
    }
}
