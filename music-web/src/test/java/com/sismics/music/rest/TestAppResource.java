//package com.sismics.music.rest;
//
//import com.sismics.music.rest.filter.CookieAuthenticationFilter;
//import com.sun.jersey.api.client.ClientResponse;
//import com.sun.jersey.api.client.ClientResponse.Status;
//import com.sun.jersey.api.client.WebResource;
//import com.sun.jersey.core.util.MultivaluedMapImpl;
//import junit.framework.Assert;
//import org.codehaus.jettison.json.JSONArray;
//import org.codehaus.jettison.json.JSONException;
//import org.codehaus.jettison.json.JsonObject;
//import org.junit.Ignore;
//import org.junit.Test;
//
//import java.nio.file.Paths;
//
///**
// * Test the app resource.
// * 
// * @author jtremeaux
// */
//public class TestAppResource extends BaseJerseyTest {
//    /**
//     * Test the API resource.
//     * 
//     * @throws JSONException
//     */
//    @Test
//    public void testAppResource() throws JSONException {
//        // Check the application info
//        WebResource appResource = target().path("/app");
//        ClientResponse response = appResource.get(ClientResponse.class);
//        response = appResource.get(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        JsonObject json = response.readEntity(JsonObject.class);
//        String currentVersion = json.getString("current_version");
//        Assert.assertNotNull(currentVersion);
//        String minVersion = json.getString("min_version");
//        Assert.assertNotNull(minVersion);
//        Long freeMemory = json.getLong("free_memory");
//        Assert.assertTrue(freeMemory > 0);
//        Long totalMemory = json.getLong("total_memory");
//        Assert.assertTrue(totalMemory > 0 && totalMemory > freeMemory);
//    }
//
//    /**
//     * Test the map port resource.
//     * 
//     * @throws JSONException
//     */
//    @Test
//    @Ignore
//    public void testMapPortResource() throws JSONException {
//        // Login admin
//        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);
//        
//        // Map port using UPnP
//        WebResource appResource = target().path("/app/map_port");
//        appResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        appResource.post(ClientResponse.class);
//    }
//    
//    /**
//     * Test the log resource.
//     * 
//     * @throws JSONException
//     */
//    @Test
//    public void testLogResource() throws JSONException {
//        // Login admin
//        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);
//        
//        // Check the logs (page 1)
//        WebResource appResource = target()
//                .path("/app/log")
//                .queryParam("level", "DEBUG");
//        ClientResponse response = appResource.get(ClientResponse.class);
//        appResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = appResource.get(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        JsonObject json = response.readEntity(JsonObject.class);
//        JSONArray logs = json.getJSONArray("logs");
//        Assert.assertTrue(logs.length() == 10);
//        Long date1 = logs.optJsonObject(0).optLong("date");
//        Long date2 = logs.optJsonObject(9).optLong("date");
//        Assert.assertTrue(date1 > date2);
//        
//        // Check the logs (page 2)
//        appResource = target()
//                .path("/app/log")
//                .queryParam("offset",  "10")
//                .queryParam("level", "DEBUG");
//        response = appResource.get(ClientResponse.class);
//        appResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = appResource.get(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        logs = json.getJSONArray("logs");
//        Assert.assertTrue(logs.length() == 10);
//        Long date3 = logs.optJsonObject(0).optLong("date");
//        Long date4 = logs.optJsonObject(9).optLong("date");
//        Assert.assertTrue(date3 > date4);
//    }
//
//    /**
//     * Test the collection reindexing batch.
//     *
//     * @throws Exception
//     */
//    @Test
//    public void testReindexBatch() throws Exception {
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
//
//        // Admin adds a directory to the collection
//        WebResource appResource = target().path("/app/batch/reindex");
//        appResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        postParams = new MultivaluedMapImpl();
//        response = appResource.post(ClientResponse.class, postParams);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        Assert.assertEquals("ok", json.getString("status"));
//
//        // Check that the albums are correctly indexed
//        albumResource = target().path("/album");
//        albumResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = albumResource.get(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        albums = json.optJSONArray("albums");
//        Assert.assertNotNull(albums);
//        Assert.assertEquals(1, albums.length());
//    }
//}
