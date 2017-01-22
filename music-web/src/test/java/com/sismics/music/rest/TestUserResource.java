package com.sismics.music.rest;

import com.sismics.util.filter.TokenBasedSecurityFilter;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Locale;

/**
 * Exhaustive test of the user resource.
 * 
 * @author jtremeaux
 */
public class TestUserResource extends BaseJerseyTest {
    /**
     * Test the user resource.
     */
    @Test
    public void testUserResource() {
        // Check anonymous user information
        JsonObject json = target().path("/user").request()
                .acceptLanguage(Locale.US)
                .get(JsonObject.class);
        Assert.assertTrue(json.getBoolean("is_default_password"));
        
        // Create alice user
        createUser("alice");

        // Login admin
        String adminAuthenticationToken = login("admin", "admin", false);
        
        // List all users
        json = target().path("/user/list")
                .queryParam("sort_column", 2)
                .queryParam("asc", false)
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        JsonArray users = json.getJsonArray("users");
        Assert.assertTrue(users.size() > 0);
        
        // Create a user KO (login length validation)
        Response response = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("username", "   bb  ")
                        .param("email", "bob@music.com")
                        .param("password", "12345678")));
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
        json = response.readEntity(JsonObject.class);
        Assert.assertEquals("ValidationError", json.getString("type"));
        Assert.assertTrue(json.getString("message"), json.getString("message").contains("more than 3"));

        // Create a user KO (login format validation)
        response = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("username", "bob-")
                        .param("email", "bob@music.com")
                        .param("password", "12345678")));
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
        json = response.readEntity(JsonObject.class);
        Assert.assertEquals("ValidationError", json.getString("type"));
        Assert.assertTrue(json.getString("message"), json.getString("message").contains("alphanumeric"));

        // Create a user KO (email format validation)
        response = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("username", "bob")
                        .param("email", "bobmusic.com")
                        .param("password", "12345678")));
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
        json = response.readEntity(JsonObject.class);
        Assert.assertEquals("ValidationError", json.getString("type"));
        Assert.assertTrue(json.getString("message"), json.getString("message").contains("must be an email"));

        // Create a user bob OK
        Form form = new Form()
                .param("username", " bob ")
                .param("email", " bob@music.com ")
                .param("password", " 12345678 ")
                .param("locale", "ko");
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(form), JsonObject.class);

        // Create a user bob KO : duplicate username
        response = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(form));
        Assert.assertNotSame(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.readEntity(JsonObject.class);
        Assert.assertEquals("AlreadyExistingUsername", json.getString("type"));

        // Check if a username is free : OK
        target().path("/user/check_username").queryParam("username", "carol").request().get(JsonObject.class);

        
        // Check if a username is free : KO
        response = target().path("/user/check_username").queryParam("username", "alice").request().get();
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.readEntity(JsonObject.class);
        Assert.assertEquals("ko", json.getString("status"));

        // Login alice with extra whitespaces
        response = target().path("/user/login").request()
                .post(Entity.form(new Form()
                        .param("username", " alice ")
                        .param("password", " 12345678 ")));
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        String aliceAuthToken = getAuthenticationCookie(response);

        // Login user bob
        String bobAuthToken = login("bob");

        // Check alice user information
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, aliceAuthToken)
                .get(JsonObject.class);
        Assert.assertEquals("alice@music.com", json.getString("email"));
        Assert.assertFalse(json.getBoolean("first_connection"));
        Assert.assertFalse(json.getBoolean("is_default_password"));
        
        // Check bob user information
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, bobAuthToken)
                .get(JsonObject.class);
        Assert.assertEquals("bob@music.com", json.getString("email"));
        Assert.assertEquals("ko", json.getString("locale"));
        
        // Test login KO (user not found)
        response = target().path("/user/login").request()
                .post(Entity.form(new Form()
                        .param("username", "intruder")
                        .param("password", "12345678")));
        Assert.assertEquals(Status.FORBIDDEN, Status.fromStatusCode(response.getStatus()));

        // Test login KO (wrong password)
        response = target().path("/user/login").request()
                .post(Entity.form(new Form()
                        .param("username", "alice")
                        .param("password", "error")));
        Assert.assertEquals(Status.FORBIDDEN, Status.fromStatusCode(response.getStatus()));

        // User alice updates her information + changes her email
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, aliceAuthToken)
                .post(Entity.form(new Form()
                        .param("email", " alice2@music.com ")
                        .param("locale", " en ")), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        
        // Check the update
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, aliceAuthToken)
                .get(JsonObject.class);
        Assert.assertEquals("alice2@music.com", json.getString("email"));
        
        // Delete user alice
        target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, aliceAuthToken)
                .delete();
        
        // Check the deletion
        response = target().path("/user/login").request()
                .post(Entity.form(new Form()
                        .param("username", "alice")
                        .param("password", "12345678")));
        Assert.assertEquals(Status.FORBIDDEN, Status.fromStatusCode(response.getStatus()));
    }

    /**
     * Test the user resource admin functions.
     */
    @Test
    public void testUserResourceAdmin() {
        // Create admin_user1 user
        createUser("admin_user1");

        // Login admin
        String adminAuthenticationToken = login("admin", "admin", false);

        // Check admin information
        JsonObject json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        Assert.assertTrue(json.getBoolean("first_connection"));
        Assert.assertTrue(json.getBoolean("is_default_password"));

        // User admin updates his information
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .post(Entity.form(new Form()
                        .param("first_connection", "false")), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Check admin information update
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        Assert.assertFalse(json.getBoolean("first_connection"));

        // User admin update admin_user1 information
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .post(Entity.form(new Form()
                        .param("email", " alice2@music.com ")
                        .param("locale", " en ")), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        
        // User admin deletes himself: forbidden
        Response response = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .delete();
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
        json = response.readEntity(JsonObject.class);
        Assert.assertEquals("ForbiddenError", json.getString("type"));

        // User admin deletes user admin_user1
        json = target().path("/user/admin_user1").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .delete(JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        
        // User admin deletes user admin_user1 : KO (user doesn't exist)
        response = target().path("/user/admin_user1").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .delete();
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
        json = response.readEntity(JsonObject.class);
        Assert.assertEquals("UserNotFound", json.getString("type"));
    }

    /**
     * Test the user authentication to Last.fm.
     */
    @Test
    @Ignore
    public void testUserLastFmRegistration() {
        // Create and login user
        createUser("user_lastfm0");
        String lastFm0AuthenticationToken = login("user_lastfm0");

        // The user checks his information
        JsonObject json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, lastFm0AuthenticationToken)
                .get(JsonObject.class);
        Assert.assertFalse(json.getBoolean("lastfm_connected"));

        // User user updates his information
        json = target().path("/user/lastfm").request()
            .cookie(TokenBasedSecurityFilter.COOKIE_NAME, lastFm0AuthenticationToken)
            .put(Entity.form(new Form()
                    .param("username", System.getenv("LASTFM_USER"))
                    .param("password", System.getenv("LASTFM_PASSWORD"))), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // The user checks his information
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, lastFm0AuthenticationToken)
                .get(JsonObject.class);
        Assert.assertTrue(json.getBoolean("lastfm_connected"));

        // The user checks his Last.fm information
        json = target().path("/user/lastfm").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, lastFm0AuthenticationToken)
                .get(JsonObject.class);
        Assert.assertEquals(System.getenv("LASTFM_USER"), json.getString("username"));
        Assert.assertNotNull(json.getJsonNumber("registered_date").longValue());
        Assert.assertNotNull(json.getJsonNumber("play_count").longValue());
        Assert.assertNotNull(json.getString("url"));
        Assert.assertNotNull(json.getString("image_url"));
    }
}
