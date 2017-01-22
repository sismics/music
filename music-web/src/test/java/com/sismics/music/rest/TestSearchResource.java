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
 * Exhaustive test of the search resource.
 * 
 * @author jtremeaux
 */
public class TestSearchResource extends BaseJerseyTest {
    /**
     * Test the search resource.
     *
     * @throws Exception
     */
    @Test
    public void testSearchResource() throws Exception {
        // Login users
        String adminAuthenticationToken = login("admin", "admin", false);

        // Admin adds an album to the collection
        JsonObject json  = target().path("/directory").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("location", Paths.get(getClass().getResource("/music/").toURI()).toString())), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Search by track name
        json = target().path("/search/revolution").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        JsonArray tracks = json.getJsonArray("tracks");
        JsonArray albums = json.getJsonArray("albums");
        JsonArray artists = json.getJsonArray("artists");
        Assert.assertEquals(1, tracks.size());
        Assert.assertEquals(0, albums.size());
        Assert.assertEquals(0, artists.size());
        Assert.assertEquals("The Revolution Will Not Be Televised", tracks.getJsonObject(0).getString("title"));
        
        // Search by album name
        json = target().path("/search/coachella").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        albums = json.getJsonArray("albums");
        tracks = json.getJsonArray("tracks");
        artists = json.getJsonArray("artists");
        Assert.assertEquals(1, albums.size());
        Assert.assertEquals(2, tracks.size());
        Assert.assertEquals(0, artists.size());
        Assert.assertEquals("Coachella 2010 Day 01 Mixtape", albums.getJsonObject(0).getString("name"));
        
        // Search by artist name
        json = target().path("/search/proxy").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        albums = json.getJsonArray("albums");
        tracks = json.getJsonArray("tracks");
        artists = json.getJsonArray("artists");
        Assert.assertEquals(1, artists.size());
        Assert.assertEquals(0, tracks.size());
        Assert.assertEquals(1, albums.size());
        Assert.assertEquals("[A] Proxy", artists.getJsonObject(0).getString("name"));
        
        // Search by artist name
        json = target().path("/search/scott").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        albums = json.getJsonArray("albums");
        tracks = json.getJsonArray("tracks");
        artists = json.getJsonArray("artists");
        Assert.assertEquals(1, artists.size());
        Assert.assertEquals(1, tracks.size());
        Assert.assertEquals(0, albums.size());
        Assert.assertEquals("Gil Scott-Heron", artists.getJsonObject(0).getString("name"));

        // Search by track name : no result
        json = target().path("/search/NOTRACK").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        Assert.assertEquals(0, json.getJsonArray("tracks").size());
        Assert.assertEquals(0, json.getJsonArray("albums").size());
        Assert.assertEquals(0, json.getJsonArray("artists").size());
    }
}
