package com.sismics.music.rest;

import java.nio.file.Paths;

import junit.framework.Assert;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.sismics.music.rest.filter.CookieAuthenticationFilter;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * Exhaustive test of the artist resource.
 * 
 * @author bgamard
 */
public class TestArtistResource extends BaseJerseyTest {
    /**
     * Test the artist resource.
     *
     * @throws Exception
     */
    @Test
    public void testArtistResource() throws Exception {
        // Login users
        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);

        // Admin adds an album to the collection
        WebResource directoryResource = resource().path("/directory");
        directoryResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
        postParams.putSingle("location", Paths.get(getClass().getResource("/music/").toURI()).toString());
        ClientResponse response = directoryResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Check that the artists are correctly added
        WebResource artistResource = resource().path("/artist");
        artistResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        response = artistResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        JSONArray artists = json.optJSONArray("artists");
        Assert.assertNotNull(artists);
        Assert.assertEquals(3, artists.length());
        JSONObject artist0 = artists.getJSONObject(0);
        String artist0Id = artist0.optString("id");
        Assert.assertNotNull(artist0Id);
        Assert.assertNotNull(artist0.optString("name"));
    }
}
