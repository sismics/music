//package com.sismics.music.rest;
//
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
// * Exhaustive test of the album resource.
// * 
// * @author jtremeaux
// */
//public class TestAlbumResource extends BaseJerseyTest {
//    /**
//     * Test the album resource.
//     *
//     * @throws Exception
//     */
//    @Test
//    public void testAlbumResource() throws Exception {
//        // Login users
//        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);
//
//        // Admin adds an album to the collection
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
//        JsonObject artist0 = album0.optJsonObject("artist");
//        Assert.assertNotNull(album0Id);
//        Assert.assertNotNull(album0.optString("name"));
//        Assert.assertNotNull(album0.optBoolean("albumart"));
//        Assert.assertNotNull(album0.optLong("update_date"));
//        Assert.assertNotNull(artist0.optString("id"));
//        
//        // Get all albums from an artist
//        albumResource = target().path("/album").queryParam("artist", artist0.optString("id"));
//        albumResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = albumResource.get(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        albums = json.optJSONArray("albums");
//        Assert.assertNotNull(albums);
//        Assert.assertEquals(1, albums.length());
//
//        // Get an album by its ID.
//        albumResource = target().path("/album/" + album0Id);
//        albumResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = albumResource.get(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        Assert.assertEquals(album0Id, json.optString("id"));
//        Assert.assertEquals("Coachella 2010 Day 01 Mixtape", json.optString("name"));
//        Assert.assertTrue(json.optBoolean("albumart"));
//        JsonObject albumArtist = json.getJsonObject("artist");
//        Assert.assertEquals("[A] Proxy", albumArtist.optString("name"));
//        JSONArray tracks = json.optJSONArray("tracks");
//        Assert.assertNotNull(tracks);
//        Assert.assertEquals(2, tracks.length());
//        JsonObject track0 = tracks.getJsonObject(0);
//        Assert.assertEquals(1, track0.optInt("order"));
//        JsonObject artist = track0.getJsonObject("artist");
//        Assert.assertEquals("Jay-Z", artist.optString("name"));
//
//        // Get an album art.
//        albumResource = target().path("/album/" + album0Id + "/albumart/small");
//        albumResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = albumResource.get(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//
//        // Get an album art.
//        albumResource = target().path("/album/" + album0Id + "/albumart/large");
//        albumResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = albumResource.get(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//
//        // Get an album art: KO, this size doesn't exist.
//        albumResource = target().path("/album/" + album0Id + "/albumart/huge");
//        albumResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = albumResource.get(ClientResponse.class);
//        Assert.assertEquals(Status.NOT_FOUND, Status.fromStatusCode(response.getStatus()));
//        
//        // Update an album art
//        albumResource = target().path("/album/" + album0Id + "/albumart");
//        albumResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        postParams = new MultivaluedMapImpl();
//        postParams.putSingle("url", "http://placehold.it/300x300");
//        response = albumResource.post(ClientResponse.class, postParams);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//    }
//}
