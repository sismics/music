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
//import org.junit.Test;
//
//import java.nio.file.Paths;
//
///**
// * Exhaustive test of the directory resource.
// * 
// * @author jtremeaux
// */
//public class TestDirectoryResource extends BaseJerseyTest {
//    /**
//     * Test the directory resource.
//     *
//     * @throws JSONException
//     */
//    @Test
//    public void testDirectoryResource() throws JSONException {
//        // Create alice user
//        clientUtil.createUser("alice");
//
//        // Login users
//        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);
//        String aliceAuthenticationToken = clientUtil.login("alice");
//
//        // Alice lists the directories: access to this resource is forbidden
//        WebResource directoryResource = target().path("/directory");
//        directoryResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, aliceAuthenticationToken));
//        ClientResponse response = directoryResource.get(ClientResponse.class);
//        Assert.assertEquals(Status.FORBIDDEN, Status.fromStatusCode(response.getStatus()));
//
//        // Admin creates a directory : bad request (location required)
//        directoryResource = target().path("/directory");
//        directoryResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
//        postParams.putSingle("name", "music");
//        response = directoryResource.put(ClientResponse.class, postParams);
//        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
//        JsonObject json = response.readEntity(JsonObject.class);
//        Assert.assertEquals("ValidationError", json.getString("type"));
//        Assert.assertTrue(json.getString("message"), json.getString("message").contains("location must be set"));
//
//        // Admin creates a directory : OK
//        directoryResource = target().path("/directory");
//        directoryResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        postParams = new MultivaluedMapImpl();
//        postParams.putSingle("name", "main");
//        postParams.putSingle("location", "/vartest/music/main");
//        response = directoryResource.put(ClientResponse.class, postParams);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        Assert.assertEquals("ok", json.getString("status"));
//
//        // Admin creates a directory without name : OK, the name is inferred from the directory location
//        directoryResource = target().path("/directory");
//        directoryResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        postParams = new MultivaluedMapImpl();
//        postParams.putSingle("location", "/vartest/music/mix");
//        response = directoryResource.put(ClientResponse.class, postParams);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        Assert.assertEquals("ok", json.getString("status"));
//
//        // Admin lists all directories
//        directoryResource = target().path("/directory");
//        directoryResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = directoryResource.get(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        JSONArray directories = json.optJSONArray("directories");
//        Assert.assertNotNull(directories);
//        Assert.assertEquals(2, directories.length());
//        JsonObject directory0 = directories.getJsonObject(0);
//        String directory0Id = directory0.optString("id");
//        Assert.assertNotNull(directory0Id);
//        Assert.assertNotNull("main", directory0.optString("name"));
//        Assert.assertNotNull("/var/music/main", directory0.optString("location"));
//        Assert.assertTrue(directory0.optBoolean("active"));
//        String directory1Id = directories.getJsonObject(1).getString("id");
//
//        // Admin updates the directory info
//        directoryResource = target().path("/directory/" + directory0Id);
//        directoryResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        postParams = new MultivaluedMapImpl();
//        postParams.add("name", "mainstream");
//        postParams.add("location", "/vartest/music/mainstream");
//        postParams.add("active", true);
//        response = directoryResource.post(ClientResponse.class, postParams);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        Assert.assertEquals("ok", json.getString("status"));
//
//        // Check the update
//        directoryResource = target().path("/directory");
//        directoryResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = directoryResource.get(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        directories = json.optJSONArray("directories");
//        Assert.assertNotNull(directories);
//        Assert.assertEquals(2, directories.length());
//        directory0 = directories.getJsonObject(0);
//        Assert.assertNotNull(directory0.opt("id"));
//        Assert.assertNotNull("mainstream", directory0.optString("name"));
//        Assert.assertNotNull("/var/music/mainstream", directory0.optString("location"));
//        Assert.assertTrue(directory0.optBoolean("active"));
//
//        // Admin deletes the directories
//        directoryResource = target().path("/directory/" + directory0Id);
//        directoryResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = directoryResource.delete(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//
//        directoryResource = target().path("/directory/" + directory1Id);
//        directoryResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = directoryResource.delete(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//
//        // Check the deletion
//        directoryResource = target().path("/directory");
//        directoryResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = directoryResource.get(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        directories = json.optJSONArray("directories");
//        Assert.assertNotNull(directories);
//        Assert.assertEquals(0, directories.length());
//    }
//
//    /**
//     * Test the collection indexing service.
//     *
//     * @throws Exception
//     */
//    @Test
//    public void testCollectionIndexing() throws Exception {
//        // Login users
//        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);
//
//        // Admin adds a directory to the collection
//        WebResource directoryResource = target().path("/directory");
//        directoryResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
//        postParams.putSingle("location", Paths.get(getClass().getResource("/music/").toURI()).toString());
//        ClientResponse response = directoryResource.put(ClientResponse.class, postParams);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        JsonObject json = response.readEntity(JsonObject.class);
//        Assert.assertEquals("ok", json.getString("status"));
//
//        // Admin lists all directories
//        directoryResource = target().path("/directory");
//        directoryResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = directoryResource.get(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        JSONArray directories = json.optJSONArray("directories");
//        Assert.assertNotNull(directories);
//        Assert.assertEquals(1, directories.length());
//        JsonObject directory0 = directories.getJsonObject(0);
//        String directory0Id = directory0.getString("id");
//
//        // Check that the albums are correctly added
//        WebResource albumResource = target().path("/album");
//        albumResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = albumResource.get(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        JSONArray albums = json.optJSONArray("albums");
//        Assert.assertNotNull(albums);
//        Assert.assertEquals(2, albums.length());
//
//        // Admin deletes the directory
//        directoryResource = target().path("/directory/" + directory0Id);
//        directoryResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = directoryResource.delete(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//
//        // Check that the albums are correctly removed
//        albumResource = target().path("/album");
//        albumResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = albumResource.get(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        albums = json.optJSONArray("albums");
//        Assert.assertNotNull(albums);
//        Assert.assertEquals(0, albums.length());
//    }
//}
