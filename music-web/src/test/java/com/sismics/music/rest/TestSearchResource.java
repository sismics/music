package com.sismics.music.rest;

import com.sismics.music.rest.filter.CookieAuthenticationFilter;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import junit.framework.Assert;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import java.nio.file.Paths;

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
        WebResource directoryResource = resource().path("/directory");
        directoryResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
        postParams.putSingle("location", Paths.get(getClass().getResource("/music/[A] Proxy - Coachella 2010 Day 01 Mixtape").toURI()).toString());
        ClientResponse response = directoryResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Search by track name : 1 result
        WebResource searchResource = resource().path("/search/revolution");
        searchResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        response = searchResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        JSONArray tracks = json.optJSONArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(1, tracks.length());
        JSONObject track0 = tracks.getJSONObject(0);
        Assert.assertEquals("The Revolution Will Not Be Televised", track0.optString("title"));
        
        // Search by album name : 1 result
        searchResource = resource().path("/search/coachella");
        searchResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        response = searchResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        JSONArray albums = json.optJSONArray("albums");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(1, albums.length());
        JSONObject album0 = albums.getJSONObject(0);
        Assert.assertEquals("Coachella 2010 Day 01 Mixtape", album0.optString("name"));
        
        // Search by artist name : 1 result
        searchResource = resource().path("/search/proxy");
        searchResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        response = searchResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        JSONArray artists = json.optJSONArray("artists");
        Assert.assertNotNull(artists);
        Assert.assertEquals(1, artists.length());
        JSONObject artist0 = artists.getJSONObject(0);
        Assert.assertEquals("[A] Proxy", artist0.optString("name"));

        // Search by track name : no result
        searchResource = resource().path("/search/NOTRACK");
        searchResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        response = searchResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        tracks = json.optJSONArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(0, tracks.length());
    }
}
