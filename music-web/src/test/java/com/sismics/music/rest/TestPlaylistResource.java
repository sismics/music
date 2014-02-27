//package com.sismics.music.rest;
//
//import com.google.common.collect.Lists;
//import com.sismics.music.rest.filter.CookieAuthenticationFilter;
//import com.sun.jersey.api.client.ClientResponse;
//import com.sun.jersey.api.client.ClientResponse.Status;
//import com.sun.jersey.api.client.WebResource;
//import com.sun.jersey.core.util.MultivaluedMapImpl;
//import junit.framework.Assert;
//import org.codehaus.jettison.json.JSONArray;
//import org.codehaus.jettison.json.JsonObject;
//import org.junit.Test;
//
//import java.nio.file.Paths;
//
///**
// * Exhaustive test of the playlist resource.
// * 
// * @author jtremeaux
// */
//public class TestPlaylistResource extends BaseJerseyTest {
//    /**
//     * Test the playlist resource.
//     *
//     * @throws Exception
//     */
//    @Test
//    public void testPlaylistResource() throws Exception {
//        // Login users
//        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);
//
//        // Admin adds a directory to the collection
//        WebResource directoryResource = target().path("/directory");
//        directoryResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
//        postParams.putSingle("location", Paths.get(getClass().getResource("/music/[A] Proxy - Coachella 2010 Day 01 Mixtape").toURI()).toString());
//        ClientResponse response = directoryResource.put(ClientResponse.class, postParams);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        JsonObject json = response.readEntity(JsonObject.class);
//        Assert.assertEquals("ok", json.getString("status"));
//
//        // Check that the albums are correctly added
//        WebResource albumResource = target().path("/album");
//        albumResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = albumResource.get(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        JSONArray albums = json.optJSONArray("albums");
//        Assert.assertNotNull(albums);
//        Assert.assertEquals(1, albums.length());
//        JsonObject album0 = albums.getJsonObject(0);
//        String album0Id = album0.optString("id");
//        Assert.assertNotNull(album0Id);
//
//        // Check that the album contains some tracks
//        albumResource = target().path("/album/" + album0Id);
//        albumResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = albumResource.get(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        Assert.assertEquals(album0Id, json.optString("id"));
//        Assert.assertEquals("Coachella 2010 Day 01 Mixtape", json.optString("name"));
//        JsonObject albumArtist = json.getJsonObject("artist");
//        Assert.assertEquals("[A] Proxy", albumArtist.optString("name"));
//        JSONArray tracks = json.optJSONArray("tracks");
//        Assert.assertNotNull(tracks);
//        Assert.assertEquals(2, tracks.length());
//        JsonObject track0 = tracks.getJsonObject(0);
//        String track0Id = track0.getString("id");
//        JsonObject track1 = tracks.getJsonObject(1);
//        String track1Id = track1.getString("id");
//
//        // Admin checks that his playlist is empty
//        WebResource playlistResource = target().path("/playlist");
//        playlistResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = playlistResource.get(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        tracks = json.optJSONArray("tracks");
//        Assert.assertNotNull(tracks);
//        Assert.assertEquals(0, tracks.length());
//
//        // Admin adds a track to the playlist
//        playlistResource = target().path("/playlist");
//        playlistResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        postParams = new MultivaluedMapImpl();
//        postParams.putSingle("id", track1Id);
//        response = playlistResource.put(ClientResponse.class, postParams);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        Assert.assertEquals("ok", json.getString("status"));
//
//        // Admin checks that his playlist contains one track.
//        playlistResource = target().path("/playlist");
//        playlistResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = playlistResource.get(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        tracks = json.optJSONArray("tracks");
//        Assert.assertNotNull(tracks);
//        Assert.assertEquals(1, tracks.length());
//        Assert.assertEquals(track1Id, tracks.getJsonObject(0).getString("id"));
//
//        // Admin adds a track to the playlist before the first one
//        playlistResource = target().path("/playlist");
//        playlistResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        postParams = new MultivaluedMapImpl();
//        postParams.putSingle("id", track0Id);
//        postParams.putSingle("order", 0);
//        response = playlistResource.put(ClientResponse.class, postParams);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        Assert.assertEquals("ok", json.getString("status"));
//
//        // Admin checks that his playlist contains 2 tracks in the right order.
//        playlistResource = target().path("/playlist");
//        playlistResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = playlistResource.get(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        tracks = json.optJSONArray("tracks");
//        Assert.assertNotNull(tracks);
//        Assert.assertEquals(2, tracks.length());
//        Assert.assertEquals(track0Id, tracks.getJsonObject(0).getString("id"));
//        Assert.assertEquals(track1Id, tracks.getJsonObject(1).getString("id"));
//
//        // Admin reverses the order of the 2 tracks
//        playlistResource = target().path("/playlist/1/move");
//        playlistResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        postParams = new MultivaluedMapImpl();
//        postParams.putSingle("neworder", 0);
//        response = playlistResource.post(ClientResponse.class, postParams);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        Assert.assertEquals("ok", json.getString("status"));
//
//        // Admin checks that his playlist contains 2 tracks in the right order
//        playlistResource = target().path("/playlist");
//        playlistResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = playlistResource.get(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        tracks = json.optJSONArray("tracks");
//        Assert.assertNotNull(tracks);
//        Assert.assertEquals(2, tracks.length());
//        Assert.assertEquals(track1Id, tracks.getJsonObject(0).getString("id"));
//        Assert.assertEquals(track0Id, tracks.getJsonObject(1).getString("id"));
//
//        // Admin removes the 1st track from the playlist
//        playlistResource = target().path("/playlist/0");
//        playlistResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = playlistResource.delete(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        Assert.assertEquals("ok", json.getString("status"));
//
//        // Admin checks that his playlist contains 1 track.
//        playlistResource = target().path("/playlist");
//        playlistResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = playlistResource.get(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        tracks = json.optJSONArray("tracks");
//        Assert.assertNotNull(tracks);
//        Assert.assertEquals(1, tracks.length());
//        Assert.assertEquals(track0Id, tracks.getJsonObject(0).getString("id"));
//        
//        // Admin clears his playlist
//        playlistResource = target().path("/playlist");
//        playlistResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = playlistResource.delete(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        Assert.assertEquals("ok", json.getString("status"));
//        
//        // Admin checks that his playlist is empty
//        playlistResource = target().path("/playlist");
//        playlistResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = playlistResource.get(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        tracks = json.optJSONArray("tracks");
//        Assert.assertNotNull(tracks);
//        Assert.assertEquals(0, tracks.length());
//        
//        // Admin adds 2 tracks at the same time
//        playlistResource = target().path("/playlist/multiple");
//        playlistResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        postParams = new MultivaluedMapImpl();
//        postParams.put("ids", Lists.newArrayList(track0Id, track1Id));
//        response = playlistResource.put(ClientResponse.class, postParams);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        Assert.assertEquals("ok", json.getString("status"));
//        
//        // Admin checks that his playlist contains 2 tracks in the right order
//        playlistResource = target().path("/playlist");
//        playlistResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = playlistResource.get(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        tracks = json.optJSONArray("tracks");
//        Assert.assertNotNull(tracks);
//        Assert.assertEquals(2, tracks.length());
//        Assert.assertEquals(track0Id, tracks.getJsonObject(0).getString("id"));
//        Assert.assertEquals(track1Id, tracks.getJsonObject(1).getString("id"));
//    }
//}
