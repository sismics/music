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
 * Exhaustive test of the playlist resource.
 * 
 * @author jtremeaux
 */
public class TestPlaylistResource extends BaseJerseyTest {
    /**
     * Test the playlist resource.
     *
     */
    @Test
    public void testPlaylistResource() throws Exception {
        // Login users
        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);

        // Admin adds a directory to the collection
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
        JsonArray albums = json.getJsonArray("albums");
        Assert.assertNotNull(albums);
        Assert.assertEquals(2, albums.size());
        JsonObject album0 = albums.getJsonObject(1);
        String album0Id = album0.getString("id");
        Assert.assertNotNull(album0Id);

        // Check that the album contains some tracks
        json = target().path("/album/" + album0Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        Assert.assertEquals(album0Id, json.getString("id"));
        Assert.assertEquals("Coachella 2010 Day 01 Mixtape", json.getString("name"));
        JsonObject albumArtist = json.getJsonObject("artist");
        Assert.assertEquals("[A] Proxy", albumArtist.getString("name"));
        JsonArray tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(2, tracks.size());
        JsonObject track0 = tracks.getJsonObject(0);
        String track0Id = track0.getString("id");
        JsonObject track1 = tracks.getJsonObject(1);
        String track1Id = track1.getString("id");

        // Admin checks that his playlist is empty
        json = target().path("/playlist").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(0, tracks.size());

        // Admin adds a track to the playlist
        json = target().path("/playlist").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("id", track1Id)), JsonObject.class);
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(1, tracks.size());
        Assert.assertEquals(track1Id, tracks.getJsonObject(0).getString("id"));

        // Admin checks that his playlist contains one track
        json = target().path("/playlist").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(1, tracks.size());
        Assert.assertEquals(track1Id, tracks.getJsonObject(0).getString("id"));

        // Admin adds a track to the playlist before the first one
        json = target().path("/playlist").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("id", track0Id)
                        .param("order", "0")), JsonObject.class);
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(2, tracks.size());
        Assert.assertEquals(track0Id, tracks.getJsonObject(0).getString("id"));
        Assert.assertEquals(track1Id, tracks.getJsonObject(1).getString("id"));

        // Admin checks that his playlist contains 2 tracks in the right order
        json = target().path("/playlist").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(2, tracks.size());
        Assert.assertEquals(track0Id, tracks.getJsonObject(0).getString("id"));
        Assert.assertEquals(track1Id, tracks.getJsonObject(1).getString("id"));

        // Admin reverses the order of the 2 tracks
        json = target().path("/playlist/1/move").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .post(Entity.form(new Form()
                        .param("neworder", "0")), JsonObject.class);
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(2, tracks.size());
        Assert.assertEquals(track1Id, tracks.getJsonObject(0).getString("id"));
        Assert.assertEquals(track0Id, tracks.getJsonObject(1).getString("id"));

        // Admin checks that his playlist contains 2 tracks in the right order
        json = target().path("/playlist").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(2, tracks.size());
        Assert.assertEquals(track1Id, tracks.getJsonObject(0).getString("id"));
        Assert.assertEquals(track0Id, tracks.getJsonObject(1).getString("id"));

        // Admin removes the 1st track from the playlist
        json = target().path("/playlist/0").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .delete(JsonObject.class);
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(1, tracks.size());
        Assert.assertEquals(track0Id, tracks.getJsonObject(0).getString("id"));

        // Admin checks that his playlist contains 1 track
        json = target().path("/playlist").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(1, tracks.size());
        Assert.assertEquals(track0Id, tracks.getJsonObject(0).getString("id"));
        
        // Admin clears his playlist
        json = target().path("/playlist").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .delete(JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        
        // Admin checks that his playlist is empty
        json = target().path("/playlist").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(0, tracks.size());
        
        // Admin adds 2 tracks at the same time
        json = target().path("/playlist/multiple").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("ids", track0Id)
                        .param("ids", track1Id)), JsonObject.class);
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(2, tracks.size());
        Assert.assertEquals(track0Id, tracks.getJsonObject(0).getString("id"));
        Assert.assertEquals(track1Id, tracks.getJsonObject(1).getString("id"));
        
        // Admin checks that his playlist contains 2 tracks in the right order
        json = target().path("/playlist").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(2, tracks.size());
        Assert.assertEquals(track0Id, tracks.getJsonObject(0).getString("id"));
        Assert.assertEquals(track1Id, tracks.getJsonObject(1).getString("id"));
        
        // Admin clears and adds a track to the playlist
        json = target().path("/playlist").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("id", track1Id)
                        .param("clear", "true")), JsonObject.class);
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(1, tracks.size());
        
        // Admin checks that his playlist contains 1 track
        json = target().path("/playlist").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(1, tracks.size());
        
        // Admin clears and adds 2 tracks at the same time
        json = target().path("/playlist/multiple").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("ids", track0Id)
                        .param("ids", track1Id)
                        .param("clear", "true")), JsonObject.class);
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(2, tracks.size());
        
        // Admin checks that his playlist contains 2 tracks
        json = target().path("/playlist").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        tracks = json.getJsonArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(2, tracks.size());
    }
}
