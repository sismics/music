package com.sismics.music.rest;

import java.nio.file.Paths;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;

import org.junit.Assert;
import org.junit.Test;

import com.sismics.util.filter.TokenBasedSecurityFilter;

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
        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);

        // Admin adds an album to the collection
        JsonObject json  = target().path("/directory").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("location", Paths.get(getClass().getResource("/music/").toURI()).toString())), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Search by track name : 1 result
        json = target().path("/search/revolution").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        JsonArray tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(1, tracks.size());
        JsonObject track0 = tracks.getJsonObject(0);
        Assert.assertEquals("The Revolution Will Not Be Televised", track0.getString("title"));
        
        // Search by album name : 1 result
        json = target().path("/search/coachella").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        JsonArray albums = json.getJsonArray("albums");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(1, albums.size());
        JsonObject album0 = albums.getJsonObject(0);
        Assert.assertEquals("Coachella 2010 Day 01 Mixtape", album0.getString("name"));
        
        // Search by artist name : 1 result
        json = target().path("/search/proxy").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        JsonArray artists = json.getJsonArray("artists");
        Assert.assertNotNull(artists);
        Assert.assertEquals(1, artists.size());
        JsonObject artist0 = artists.getJsonObject(0);
        Assert.assertEquals("[A] Proxy", artist0.getString("name"));

        // Search by track name : no result
        json = target().path("/search/NOTRACK").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(0, tracks.size());
    }
}
