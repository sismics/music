package com.sismics.music.rest;

import com.sismics.util.filter.TokenBasedSecurityFilter;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
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
     * @throws Exception
     */
    @Test
    public void testArtistResource() throws Exception {
        // Login users
        String adminAuthenticationToken = login("admin", "admin", false);

        // Admin adds an album to the collection
        JsonObject json = target().path("/directory").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("location", Paths.get(getClass().getResource("/music/").toURI()).toString())), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Check that the artists are correctly added
        json = target().path("/artist")
                .queryParam("sort_column", "0")
                .queryParam("asc", "true")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        Assert.assertEquals(4, json.getJsonNumber("total").intValue());
        JsonArray artists = json.getJsonArray("artists");
        Assert.assertNotNull(artists);
        Assert.assertEquals(4, artists.size());
        JsonObject artist0 = artists.getJsonObject(0);
        String artist0Id = artist0.getString("id");
        Assert.assertNotNull(artist0Id);
        Assert.assertNotNull(artist0.getString("name"));
        
        // Get an artist details
        json = target().path("/artist/" + artist0Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        Assert.assertEquals("Gil Scott-Heron", json.getString("name"));
        JsonArray albums = json.getJsonArray("albums");
        Assert.assertEquals(0, albums.size());
        JsonArray tracks = json.getJsonArray("tracks");
        Assert.assertEquals(1, tracks.size());
    }
}
