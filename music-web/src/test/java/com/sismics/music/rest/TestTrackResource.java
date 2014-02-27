//package com.sismics.music.rest;
//
//import com.sismics.music.rest.filter.CookieAuthenticationFilter;
//import com.sun.jersey.api.client.ClientResponse;
//import com.sun.jersey.api.client.ClientResponse.Status;
//import com.sun.jersey.api.client.WebResource;
//import com.sun.jersey.core.util.MultivaluedMapImpl;
//import junit.framework.Assert;
//import org.codehaus.jettison.json.JsonArray;
//import org.codehaus.jettison.json.JsonObject;
//import org.junit.Test;
//
//import java.nio.file.Paths;
//
///**
// * Exhaustive test of the track resource.
// * 
// * @author jtremeaux
// */
//public class TestTrackResource extends BaseJerseyTest {
//    /**
//     * Test the track resource.
//     *
//     * @throws Exception
//     */
//    @Test
//    public void testTrackResource() throws Exception {
//        // Login users
//        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);
//
//        // Admin adds a track to the collection
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
//        JsonArray albums = json.getJsonArray("albums");
//        Assert.assertNotNull(albums);
//        Assert.assertEquals(1, albums.length());
//        JsonObject album0 = albums.getJsonObject(0);
//        String album0Id = album0.getString("id");
//        Assert.assertNotNull(album0Id);
//
//        // Admin checks the tracks info
//        albumResource = target().path("/album/" + album0Id);
//        albumResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = albumResource.get(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        JsonArray tracks = json.getJsonArray("tracks");
//        Assert.assertNotNull(tracks);
//        Assert.assertEquals(2, tracks.length());
//        JsonObject track0 = tracks.getJsonObject(0);
//        String track0Id = track0.getString("id");
//        Assert.assertFalse(track0.getBoolean("liked"));
//
//        // Get an track by its ID.
//        WebResource trackResource = target().path("/track/" + track0Id);
//        trackResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = trackResource.get(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//
//        // Admin likes the track
//        trackResource = target().path("/track/" + track0Id + "/like");
//        trackResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        postParams = new MultivaluedMapImpl();
//        response = trackResource.post(ClientResponse.class, postParams);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        Assert.assertEquals("ok", json.getString("status"));
//
//        // Admin checks the tracks info
//        albumResource = target().path("/album/" + album0Id);
//        albumResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = albumResource.get(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        tracks = json.getJsonArray("tracks");
//        Assert.assertNotNull(tracks);
//        Assert.assertEquals(2, tracks.length());
//        track0 = tracks.getJsonObject(0);
//        Assert.assertTrue(track0.getBoolean("liked"));
//
//        // Admin unlikes the track
//        trackResource = target().path("/track/" + track0Id + "/like");
//        trackResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = trackResource.delete(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        Assert.assertEquals("ok", json.getString("status"));
//
//        // Admin checks the tracks info
//        albumResource = target().path("/album/" + album0Id);
//        albumResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = albumResource.get(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        tracks = json.getJsonArray("tracks");
//        Assert.assertNotNull(tracks);
//        Assert.assertEquals(2, tracks.length());
//        track0 = tracks.getJsonObject(0);
//        Assert.assertFalse(track0.getBoolean("liked"));
//
//    }
//}
