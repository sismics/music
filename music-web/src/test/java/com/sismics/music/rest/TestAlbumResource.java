package com.sismics.music.rest;

import com.sismics.util.filter.TokenBasedSecurityFilter;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
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
     * @throws Exception
     */
    @Test
    public void testAlbumResource() throws Exception {
        // Login users
        String adminAuthenticationToken = login("admin", "admin", false);

        // Admin adds an album to the collection
        JsonObject json = target().path("/directory").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("location", Paths.get(getClass().getResource("/music/").toURI()).toString())), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Check that the albums are correctly added
        json = target().path("/album")
                .queryParam("sort_column", "0")
                .queryParam("asc", "false")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
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
        json = target().path("/album/" + album0Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
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
        Response response = target().path("/album/" + album0Id + "/albumart/small").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken).get();
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));

        // Get an album art
        response = target().path("/album/" + album0Id + "/albumart/large").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken).get();
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        
        // Get an album art
        response = target().path("/album/" + album0Id + "/albumart/medium").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken).get();
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));

        // Get an album art: KO, this size doesn't exist
        response = target().path("/album/" + album0Id + "/albumart/huge").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken).get();
        Assert.assertEquals(Status.NOT_FOUND, Status.fromStatusCode(response.getStatus()));
        
        // Update an album art
        json = target().path("/album/" + album0Id + "/albumart").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .post(Entity.form(new Form()
                            .param("url", "http://lorempixel.com/200/200/")), JsonObject.class);
        Assert.assertNull(json.get("message"));
        
        // Get an album art
        response = target().path("/album/" + album0Id + "/albumart/large").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken).get();
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        
        // Check that the original file has been copied to the collection
        Path musicPath = Paths.get(getClass().getResource("/music/").toURI());
        File albumArtFile = musicPath.resolve(Paths.get("[A] Proxy - Coachella 2010 Day 01 Mixtape", "albumart.jpg")).toFile();
        Assert.assertTrue(albumArtFile.exists());
        
        // Make the album art not writable
        albumArtFile.setWritable(false);
        
        // Update an album art
        json = target().path("/album/" + album0Id + "/albumart").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .post(Entity.form(new Form()
                        .param("url", "http://lorempixel.com/200/200/")), JsonObject.class);
        Assert.assertEquals("AlbumArtNotCopied", json.getString("message"));
        albumArtFile.setWritable(true);
        
        // Get an album art
        response = target().path("/album/" + album0Id + "/albumart/large").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken).get();
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
    }
}
