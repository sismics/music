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
// * Exhaustive test of the artist resource.
// * 
// * @author bgamard
// */
//public class TestArtistResource extends BaseJerseyTest {
//    /**
//     * Test the artist resource.
//     *
//     * @throws Exception
//     */
//    @Test
//    public void testArtistResource() throws Exception {
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
//        // Check that the artists are correctly added
//        WebResource artistResource = target().path("/artist");
//        artistResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = artistResource.get(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        JSONArray artists = json.optJSONArray("artists");
//        Assert.assertNotNull(artists);
//        Assert.assertEquals(3, artists.length());
//        JsonObject artist0 = artists.getJsonObject(0);
//        String artist0Id = artist0.optString("id");
//        Assert.assertNotNull(artist0Id);
//        Assert.assertNotNull(artist0.optString("name"));
//        
//        // Get an artist details
//        artistResource = target().path("/artist/" + artist0Id);
//        artistResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = artistResource.get(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        Assert.assertEquals("Gil Scott-Heron", json.optString("name"));
//    }
//}
