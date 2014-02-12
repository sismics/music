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

import java.util.Date;

/**
 * Exhaustive test of the player resource.
 * 
 * @author jtremeaux
 */
public class TestPlayerResource extends BaseJerseyTest {
    /**
     * Test the track resource.
     *
     * @throws Exception
     */
    @Test
    public void testTrackResource() throws Exception {
        // Login users
        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);

        // Admin adds a track to the collection
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
        JSONObject album0 = albums.getJSONObject(0);
        String album0Id = album0.optString("id");
        Assert.assertNotNull(album0Id);

        // Check the tracks info
        albumResource = resource().path("/album/" + album0Id);
        albumResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        response = albumResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        JSONArray tracks = json.optJSONArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(2, tracks.length());
        JSONObject track0 = tracks.getJSONObject(0);
        String track0Id = track0.getString("id");
        Integer track0Length = track0.getInt("length");
        Assert.assertEquals(0, track0.getInt("play_count"));

        // Marks a track as played
        WebResource playerResource = resource().path("/player/listening");
        playerResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        postParams = new MultivaluedMapImpl();
        postParams.putSingle("id", track0Id);
        postParams.putSingle("date", new Date().getTime());
        postParams.putSingle("duration", track0Length / 2 + 1);
        response = playerResource.post(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Check the tracks info
        albumResource = resource().path("/album/" + album0Id);
        albumResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        response = albumResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        tracks = json.optJSONArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(2, tracks.length());
        track0 = tracks.getJSONObject(0);
        Assert.assertEquals(1, track0.getInt("play_count"));
    }
}
