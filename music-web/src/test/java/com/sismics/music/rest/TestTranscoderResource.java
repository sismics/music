package com.sismics.music.rest;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;

import static org.junit.Assert.assertEquals;

/**
 * Exhaustive test of the transcoder resource.
 * 
 * @author jtremeaux
 */
public class TestTranscoderResource extends BaseJerseyTest {
    /**
     * Test the transcoder resource.
     * 
     */
    @Test
    public void testTranscoderResource() throws Exception {
        // Login admin
        loginAdmin();

        // List all transcoders
        GET("/transcoder");
        assertIsOk();
        JsonObject json = getJsonResult();
        JsonArray transcoders = json.getJsonArray("transcoders");
        assertEquals(0, transcoders.size());

        // Create a transcoder
        PUT("/transcoder", ImmutableMap.of(
                "name", "mp3",
                "source", "ogg",
                "destination", "mp4",
                "step1", "ffmpeg"));
        assertIsOk();

        // List all transcoders
        GET("/transcoder");
        assertIsOk();
        json = getJsonResult();
        transcoders = json.getJsonArray("transcoders");
        assertEquals(1, transcoders.size());
        JsonObject transcoder0 = transcoders.getJsonObject(0);
        String transcoder0Id = transcoder0.getString("id");
        assertEquals("mp3", transcoder0.getString("name"));

        // Update a transcoder
        POST("/transcoder/" + transcoder0Id, ImmutableMap.of(
                "name", "mp3 audio",
                "source", "ogg oga aac m4a flac wav wma aif aiff ape mpc shn",
                "destination", "mp3",
                "step1", "ffmpeg -i %s -ab %bk -v 0 -f mp3 -"));
        assertIsOk();

        // Check the update
        GET("/transcoder");
        assertIsOk();
        json = getJsonResult();
        transcoders = json.getJsonArray("transcoders");
        assertEquals(1, transcoders.size());
        transcoder0 = transcoders.getJsonObject(0);
        assertEquals("mp3 audio", transcoder0.getString("name"));

        // Delete the transcoder
        DELETE("/transcoder/" + transcoder0Id);
        assertIsOk();

        // Check the deletion
        GET("/transcoder");
        assertIsOk();
        json = getJsonResult();
        transcoders = json.getJsonArray("transcoders");
        assertEquals(0, transcoders.size());
    }
}
