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
///**
// * Exhaustive test of the transcoder resource.
// * 
// * @author jtremeaux
// */
//public class TestTranscoderResource extends BaseJerseyTest {
//    /**
//     * Test the transcoder resource.
//     *
//     * @throws org.codehaus.jettison.json.JSONException
//     */
//    @Test
//    public void testTranscoderResource() throws JSONException {
//        // Login admin
//        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);
//
//        // List all transcoders
//        WebResource transcoderResource = target().path("/transcoder");
//        transcoderResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        ClientResponse response = transcoderResource.get(ClientResponse.class);
//        JsonObject json = response.readEntity(JsonObject.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        JSONArray transcoders = json.getJSONArray("transcoders");
//        Assert.assertEquals(0, transcoders.length());
//
//        // Create a transcoder
//        transcoderResource = target().path("/transcoder");
//        transcoderResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
//        postParams.putSingle("name", "mp3");
//        postParams.putSingle("source", "ogg");
//        postParams.putSingle("destination", "mp4");
//        postParams.putSingle("step1", "ffmpeg");
//        response = transcoderResource.put(ClientResponse.class, postParams);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        Assert.assertEquals("ok", json.getString("status"));
//
//        // List all transcoders
//        transcoderResource = target().path("/transcoder");
//        transcoderResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = transcoderResource.get(ClientResponse.class);
//        json = response.readEntity(JsonObject.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        transcoders = json.getJSONArray("transcoders");
//        Assert.assertEquals(1, transcoders.length());
//        JsonObject transcoder0 = transcoders.getJsonObject(0);
//        String transcoder0Id = transcoder0.getString("id");
//        Assert.assertEquals("mp3", transcoder0.getString("name"));
//
//        // Update a transcoder
//        transcoderResource = target().path("/transcoder/" + transcoder0Id);
//        transcoderResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        postParams = new MultivaluedMapImpl();
//        postParams.putSingle("name", "mp3 audio");
//        postParams.putSingle("source", "ogg oga aac m4a flac wav wma aif aiff ape mpc shn");
//        postParams.putSingle("destination", "mp3");
//        postParams.putSingle("step1", "ffmpeg -i %s -ab %bk -v 0 -f mp3 -");
//        response = transcoderResource.post(ClientResponse.class, postParams);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        Assert.assertEquals("ok", json.getString("status"));
//
//        // Check the update
//        transcoderResource = target().path("/transcoder");
//        transcoderResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = transcoderResource.get(ClientResponse.class);
//        json = response.readEntity(JsonObject.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        transcoders = json.getJSONArray("transcoders");
//        Assert.assertEquals(1, transcoders.length());
//        transcoder0 = transcoders.getJsonObject(0);
//        Assert.assertEquals("mp3 audio", transcoder0.getString("name"));
//
//        // Delete the transcoder
//        transcoderResource = target().path("/transcoder/" + transcoder0Id);
//        transcoderResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = transcoderResource.delete(ClientResponse.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        json = response.readEntity(JsonObject.class);
//        Assert.assertEquals("ok", json.getString("status"));
//
//        // Check the deletion
//        transcoderResource = target().path("/transcoder");
//        transcoderResource.addFilter(.cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken));
//        response = transcoderResource.get(ClientResponse.class);
//        json = response.readEntity(JsonObject.class);
//        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
//        transcoders = json.getJSONArray("transcoders");
//        Assert.assertEquals(0, transcoders.length());
//    }
//}
