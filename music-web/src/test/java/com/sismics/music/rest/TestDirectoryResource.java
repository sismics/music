package com.sismics.music.rest;

import junit.framework.Assert;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.sismics.music.rest.filter.CookieAuthenticationFilter;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * Exhaustive test of the directory resource.
 * 
 * @author jtremeaux
 */
public class TestDirectoryResource extends BaseJerseyTest {
    /**
     * Test the directory resource.
     *
     * @throws JSONException
     */
    @Test
    public void testDirectoryResource() throws JSONException {
        // Create alice user
        clientUtil.createUser("alice");

        // Login users
        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);
        String aliceAuthenticationToken = clientUtil.login("alice");

        // Alice lists the directories: access to this resource is forbidden
        WebResource directoryResource = resource().path("/directory");
        directoryResource.addFilter(new CookieAuthenticationFilter(aliceAuthenticationToken));
        ClientResponse response = directoryResource.get(ClientResponse.class);
        Assert.assertEquals(Status.FORBIDDEN, Status.fromStatusCode(response.getStatus()));

        // Admin creates a directory : bad request (location required)
        directoryResource = resource().path("/directory");
        directoryResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
        postParams.putSingle("name", "music");
        response = directoryResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ValidationError", json.getString("type"));
        Assert.assertTrue(json.getString("message"), json.getString("message").contains("location must be set"));

        // Admin creates a directory : OK
        directoryResource = resource().path("/directory");
        directoryResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        postParams = new MultivaluedMapImpl();
        postParams.putSingle("name", "main");
        postParams.putSingle("location", "/vartest/music/main");
        response = directoryResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Admin creates a directory without name : OK, the name is inferred from the directory location
        directoryResource = resource().path("/directory");
        directoryResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        postParams = new MultivaluedMapImpl();
        postParams.putSingle("location", "/vartest/music/mix");
        response = directoryResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Admin lists all directories
        directoryResource = resource().path("/directory");
        directoryResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        response = directoryResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        JSONArray directories = json.optJSONArray("directories");
        Assert.assertNotNull(directories);
        Assert.assertEquals(2, directories.length());
        JSONObject directory0 = directories.getJSONObject(0);
        Assert.assertNotNull(directory0.opt("id"));
        String directory0Id = directory0.optString("id");
        Assert.assertNotNull(directory0Id);
        Assert.assertNotNull("main", directory0.optString("name"));
        Assert.assertNotNull("/var/music/main", directory0.optString("location"));
        Assert.assertTrue(directory0.optBoolean("active"));

        // Admin updates the directory info
        directoryResource = resource().path("/directory/" + directory0Id);
        directoryResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        postParams = new MultivaluedMapImpl();
        postParams.add("name", "mainstream");
        postParams.add("location", "/vartest/music/mainstream");
        postParams.add("active", true);
        response = directoryResource.post(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Check the update
        directoryResource = resource().path("/directory");
        directoryResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        response = directoryResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        directories = json.optJSONArray("directories");
        Assert.assertNotNull(directories);
        Assert.assertEquals(2, directories.length());
        directory0 = directories.getJSONObject(0);
        Assert.assertNotNull(directory0.opt("id"));
        Assert.assertNotNull("mainstream", directory0.optString("name"));
        Assert.assertNotNull("/var/music/mainstream", directory0.optString("location"));
        Assert.assertTrue(directory0.optBoolean("active"));

        // Admin deletes the directory
        directoryResource = resource().path("/directory/" + directory0Id);
        directoryResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        response = directoryResource.delete(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));

        // Check the deletion
        directoryResource = resource().path("/directory");
        directoryResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        response = directoryResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        directories = json.optJSONArray("directories");
        Assert.assertNotNull(directories);
        Assert.assertEquals(1, directories.length());
    }

    /**
     * Test the collection indexing service.
     *
     * @throws Exception
     */
    @Test
    public void testCollectionIndexing() throws Exception {
        // Login users
        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);

        // Admin adds a directory to the collection
        WebResource directoryResource = resource().path("/directory");
        directoryResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
        postParams.putSingle("location", getClass().getResource("/music/").toURI().getPath());
        ClientResponse response = directoryResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Check that the albums are correctly added
        WebResource albumResource = resource().path("/album");
        albumResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        response = albumResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        JSONArray albums = json.optJSONArray("albums");
        Assert.assertNotNull(albums);
        Assert.assertEquals(1, albums.length());
    }
}
