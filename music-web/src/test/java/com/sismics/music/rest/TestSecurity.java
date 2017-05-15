package com.sismics.music.rest;

import com.sismics.util.filter.TokenBasedSecurityFilter;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test of the security layer.
 * 
 * @author jtremeaux
 */
public class TestSecurity extends BaseJerseyTest {
    /**
     * Test of the security layer.
     */
    @Test
    public void testSecurity() {
        // Create a user
        createUser("testsecurity");

        // Changes a user's email KO : the user is not connected
        Response response = target().path("/user/update").request()
                .post(Entity.form(new Form().param("email", "testsecurity2@music.com")));
        assertEquals(Status.FORBIDDEN, Status.fromStatusCode(response.getStatus()));
        JsonObject json = response.readEntity(JsonObject.class);
        assertEquals("ForbiddenError", json.getString("type"));
        assertEquals("You don't have access to this resource", json.getString("message"));

        // User testsecurity logs in
        String testSecurityAuthenticationToken = login("testsecurity");

        // User testsecurity creates a new user KO : no permission
        response = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, testSecurityAuthenticationToken)
                .put(Entity.form(new Form()));
        assertEquals(Status.FORBIDDEN, Status.fromStatusCode(response.getStatus()));
        assertEquals("ForbiddenError", json.getString("type"));
        assertEquals("You don't have access to this resource", json.getString("message"));

        // User testsecurity changes his email OK
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, testSecurityAuthenticationToken)
                .post(Entity.form(
                        new Form()
                        .param("email", "testsecurity2@music.com")
                        .param("locale", "en")), JsonObject.class);
        assertEquals("ok", json.getString("status"));

        // User testsecurity logs out
        json = target().path("/user/logout").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, testSecurityAuthenticationToken)
                .post(Entity.form(new Form()), JsonObject.class);
        testSecurityAuthenticationToken = getAuthenticationCookie(response);
        assertTrue(StringUtils.isEmpty(testSecurityAuthenticationToken));

        // User testsecurity logs out KO : he is not connected anymore
        response = target().path("/user/logout").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, testSecurityAuthenticationToken)
                .post(Entity.form(new Form()));
        assertEquals(Status.FORBIDDEN, Status.fromStatusCode(response.getStatus()));

        // User testsecurity logs in with a long lived session
        testSecurityAuthenticationToken = login("testsecurity", "12345678", true);

        // User testsecurity logs out
        logout();
    }
}