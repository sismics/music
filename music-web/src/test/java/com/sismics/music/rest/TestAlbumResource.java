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

/**
 * Exhaustive test of the album resource.
 * 
 * @author jtremeaux
 */
public class TestAlbumResource extends BaseJerseyTest {
    /**
     * Test the album resource.
     *
     * @throws Exception
     */
    @Test
    public void testAlbumResource() throws Exception {
        // Login users
        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);

        // Admin adds a album to the collection
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
        Assert.assertNotNull(album0.optString("name"));
        Assert.assertNotNull(album0.optBoolean("albumart"));

        // Get an album by its ID.
        albumResource = resource().path("/album/" + album0Id);
        albumResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        response = albumResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(album0Id, json.optString("id"));
        Assert.assertEquals("Coachella 2010 Day 01 Mixtape", json.optString("name"));
        Assert.assertTrue(json.optBoolean("albumart"));
        JSONObject albumArtist = json.getJSONObject("artist");
        Assert.assertEquals("[A] Proxy", albumArtist.optString("name"));
        JSONArray tracks = json.optJSONArray("tracks");
        Assert.assertNotNull(tracks);
        Assert.assertEquals(2, tracks.length());
        JSONObject track0 = tracks.getJSONObject(0);
        Assert.assertEquals(1, track0.optInt("order"));
        JSONObject artist = track0.getJSONObject("artist");
        Assert.assertEquals("Jay-Z", artist.optString("name"));

        // Get an album art.
        albumResource = resource().path("/album/" + album0Id + "/albumart/small");
        albumResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        response = albumResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));

        // Get an album art.
        albumResource = resource().path("/album/" + album0Id + "/albumart/large");
        albumResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        response = albumResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));

        // Get an album art: KO, this size doesn't exist.
        albumResource = resource().path("/album/" + album0Id + "/albumart/huge");
        albumResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        response = albumResource.get(ClientResponse.class);
        Assert.assertEquals(Status.NOT_FOUND, Status.fromStatusCode(response.getStatus()));
    }
}
