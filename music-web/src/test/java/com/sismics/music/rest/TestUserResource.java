package com.sismics.music.rest;

import com.sismics.music.rest.filter.CookieAuthenticationFilter;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import junit.framework.Assert;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Locale;

/**
 * Exhaustive test of the user resource.
 * 
 * @author jtremeaux
 */
public class TestUserResource extends BaseJerseyTest {
    /**
     * Test the user resource.
     * 
     * @throws JSONException
     */
    @Test
    public void testUserResource() throws JSONException {
        // Check anonymous user information
        WebResource userResource = resource().path("/user");
        ClientResponse response = userResource.acceptLanguage(Locale.US).get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        Assert.assertTrue(json.getBoolean("is_default_password"));
        
        // Create alice user
        clientUtil.createUser("alice");

        // Login admin
        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);
        
        // List all users
        userResource = resource().path("/user/list");
        userResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        MultivaluedMapImpl getParams = new MultivaluedMapImpl();
        getParams.putSingle("sort_column", 2);
        getParams.putSingle("asc", false);
        response = userResource.queryParams(getParams).get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONArray users = json.getJSONArray("users");
        Assert.assertTrue(users.length() > 0);
        
        // Create a user KO (login length validation)
        userResource = resource().path("/user");
        userResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
        postParams.putSingle("username", "   bb  ");
        postParams.putSingle("email", "bob@music.com");
        postParams.putSingle("password", "12345678");
        response = userResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ValidationError", json.getString("type"));
        Assert.assertTrue(json.getString("message"), json.getString("message").contains("more than 3"));

        // Create a user KO (login format validation)
        userResource = resource().path("/user");
        userResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        postParams = new MultivaluedMapImpl();
        postParams.putSingle("username", "bob-");
        postParams.putSingle("email", " bob@music.com ");
        postParams.putSingle("password", "12345678");
        response = userResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ValidationError", json.getString("type"));
        Assert.assertTrue(json.getString("message"), json.getString("message").contains("alphanumeric"));

        // Create a user KO (email format validation)
        userResource = resource().path("/user");
        userResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        postParams = new MultivaluedMapImpl();
        postParams.putSingle("username", "bob");
        postParams.putSingle("email", " bobreader.com ");
        postParams.putSingle("password", "12345678");
        response = userResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ValidationError", json.getString("type"));
        Assert.assertTrue(json.getString("message"), json.getString("message").contains("must be an email"));

        // Create a user bob OK
        userResource = resource().path("/user");
        userResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        postParams = new MultivaluedMapImpl();
        postParams.putSingle("username", " bob ");
        postParams.putSingle("email", " bob@music.com ");
        postParams.putSingle("password", " 12345678 ");
        postParams.putSingle("locale", "ko");
        response = userResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));

        // Create a user bob KO : duplicate username
        response = userResource.put(ClientResponse.class, postParams);
        Assert.assertNotSame(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("AlreadyExistingUsername", json.getString("type"));

        // Check if a username is free : OK
        userResource = resource().path("/user/check_username");
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("username", "carol");
        response = userResource.queryParams(queryParams).get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));

        // Check if a username is free : KO
        userResource = resource().path("/user/check_username");
        queryParams = new MultivaluedMapImpl();
        queryParams.add("username", "alice");
        response = userResource.queryParams(queryParams).get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ko", json.getString("status"));

        // Login alice with extra whitespaces
        userResource = resource().path("/user/login");
        postParams = new MultivaluedMapImpl();
        postParams.putSingle("username", " alice ");
        postParams.putSingle("password", " 12345678 ");
        response = userResource.post(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        String aliceAuthToken = clientUtil.getAuthenticationCookie(response);

        // Login user bob
        String bobAuthToken = clientUtil.login("bob");

        // Check alice user information
        userResource = resource().path("/user");
        userResource.addFilter(new CookieAuthenticationFilter(aliceAuthToken));
        response = userResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("alice@music.com", json.getString("email"));
        Assert.assertEquals("default.less", json.getString("theme"));
        Assert.assertFalse(json.getBoolean("first_connection"));
        Assert.assertFalse(json.getBoolean("is_default_password"));
        
        // Check bob user information
        userResource = resource().path("/user");
        userResource.addFilter(new CookieAuthenticationFilter(bobAuthToken));
        response = userResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("bob@music.com", json.getString("email"));
        Assert.assertEquals("ko", json.getString("locale"));
        
        // Test login KO (user not found)
        userResource = resource().path("/user/login");
        postParams.putSingle("username", "intruder");
        postParams.putSingle("password", "12345678");
        response = userResource.post(ClientResponse.class, postParams);
        Assert.assertEquals(Status.FORBIDDEN, Status.fromStatusCode(response.getStatus()));

        // Test login KO (wrong password)
        userResource = resource().path("/user/login");
        postParams.putSingle("username", "alice");
        postParams.putSingle("password", "error");
        response = userResource.post(ClientResponse.class, postParams);
        Assert.assertEquals(Status.FORBIDDEN, Status.fromStatusCode(response.getStatus()));

        // User alice updates her information + changes her email
        userResource = resource().path("/user");
        userResource.addFilter(new CookieAuthenticationFilter(aliceAuthToken));
        postParams = new MultivaluedMapImpl();
        postParams.add("email", " alice2@music.com ");
        postParams.add("theme", " highcontrast.less ");
        postParams.add("locale", " en ");
        response = userResource.post(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        
        // Check the update
        userResource = resource().path("/user");
        userResource.addFilter(new CookieAuthenticationFilter(aliceAuthToken));
        response = userResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("highcontrast.less", json.getString("theme"));
        
        // Delete user alice
        userResource = resource().path("/user");
        userResource.addFilter(new CookieAuthenticationFilter(aliceAuthToken));
        response = userResource.delete(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        
        // Check the deletion
        userResource = resource().path("/user/login");
        postParams.putSingle("username", "alice");
        postParams.putSingle("password", "12345678");
        response = userResource.post(ClientResponse.class, postParams);
        Assert.assertEquals(Status.FORBIDDEN, Status.fromStatusCode(response.getStatus()));
    }

    /**
     * Test the user resource admin functions.
     * 
     * @throws JSONException
     */
    @Test
    public void testUserResourceAdmin() throws JSONException {
        // Create admin_user1 user
        clientUtil.createUser("admin_user1");

        // Login admin
        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);

        // Check admin information
        WebResource userResource = resource().path("/user");
        userResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        ClientResponse response = userResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        Assert.assertTrue(json.getBoolean("first_connection"));
        Assert.assertTrue(json.getBoolean("is_default_password"));

        // User admin updates his information
        userResource = resource().path("/user");
        userResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
        postParams.add("first_connection", false);
        response = userResource.post(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Check admin information update
        userResource = resource().path("/user");
        userResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        response = userResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertFalse(json.getBoolean("first_connection"));

        // User admin update admin_user1 information
        userResource = resource().path("/user");
        userResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        postParams = new MultivaluedMapImpl();
        postParams.add("email", " alice2@music.com ");
        postParams.add("theme", " highcontrast.less");
        postParams.add("locale", " en ");
        response = userResource.post(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        
        // User admin deletes himself: forbidden
        userResource = resource().path("/user");
        userResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        response = userResource.delete(ClientResponse.class);
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ForbiddenError", json.getString("type"));

        // User admin deletes himself: forbidden
        userResource = resource().path("/user/admin");
        userResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        response = userResource.delete(ClientResponse.class);
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ForbiddenError", json.getString("type"));

        // User admin deletes user admin_user1
        userResource = resource().path("/user/admin_user1");
        userResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        response = userResource.delete(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        
        // User admin deletes user admin_user1 : KO (user doesn't exist)
        userResource = resource().path("/user/admin_user1");
        userResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        response = userResource.delete(ClientResponse.class);
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("UserNotFound", json.getString("type"));
    }

    /**
     * Test the user authentication to Last.fm.
     *
     * @throws JSONException
     */
    @Test
    @Ignore
    public void testUserLastFmRegistration() throws JSONException {
        // Create and login user
        clientUtil.createUser("user_lastfm0");
        String lastFm0AuthenticationToken = clientUtil.login("user_lastfm0");

        // The user checks his informatino
        WebResource userResource = resource().path("/user");
        userResource.addFilter(new CookieAuthenticationFilter(lastFm0AuthenticationToken));
        ClientResponse response = userResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        Assert.assertFalse(json.getBoolean("lastfm_connected"));

        // User user updates his information
        userResource = resource().path("/user/lastfm");
        userResource.addFilter(new CookieAuthenticationFilter(lastFm0AuthenticationToken));
        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
        postParams.add("username", System.getenv("LASTFM_USER"));
        postParams.add("password", System.getenv("LASTFM_PASSWORD"));
        response = userResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // The user checks his informatino
        userResource = resource().path("/user");
        userResource.addFilter(new CookieAuthenticationFilter(lastFm0AuthenticationToken));
        response = userResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertTrue(json.getBoolean("lastfm_connected"));
    }
}
