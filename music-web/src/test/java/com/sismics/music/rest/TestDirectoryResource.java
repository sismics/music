package com.sismics.music.rest;

import com.sismics.util.filter.TokenBasedSecurityFilter;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.nio.file.Paths;

/**
 * Exhaustive test of the directory resource.
 * 
 * @author jtremeaux
 */
public class TestDirectoryResource extends BaseJerseyTest {
    /**
     * Test the directory resource.
     */
    @Test
    public void testDirectoryResource() throws Exception {
        // Create alice user
        createUser("alice");

        // Login users
        String adminAuthenticationToken = login("admin", "admin", false);
        String aliceAuthenticationToken = login("alice");

        // Alice lists the directories: access to this resource is forbidden
        Response response = target().path("/directory").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, aliceAuthenticationToken).get();
        Assert.assertEquals(Status.FORBIDDEN, Status.fromStatusCode(response.getStatus()));

        // Admin creates a directory : bad request (location required)
        response = target().path("/directory").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()));
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
        JsonObject json = response.readEntity(JsonObject.class);
        Assert.assertEquals("ValidationError", json.getString("type"));
        Assert.assertTrue(json.getString("message"), json.getString("message").contains("location must be set"));

        // Admin creates a directory : OK
        json = target().path("/directory").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("location", "/vartest/music/main")), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Admin creates a directory : OK
        json = target().path("/directory").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("location", "/vartest/music/mix")), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Admin lists all directories
        json = target().path("/directory").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        JsonArray directories = json.getJsonArray("directories");
        Assert.assertNotNull(directories);
        Assert.assertEquals(2, directories.size());
        JsonObject directory0 = directories.getJsonObject(0);
        String directory0Id = directory0.getString("id");
        Assert.assertNotNull(directory0Id);
        Assert.assertNotNull("/var/music/main", directory0.getString("location"));
        Assert.assertTrue(directory0.getBoolean("active"));
        Assert.assertFalse(directory0.getBoolean("valid"));
        String directory1Id = directories.getJsonObject(1).getString("id");

        // Admin updates the directory info
        json = target().path("/directory/" + directory0Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .post(Entity.form(new Form()
                        .param("location", "/vartest/music/mainstream")
                        .param("active", "true")), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Check the update
        json = target().path("/directory").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        directories = json.getJsonArray("directories");
        Assert.assertNotNull(directories);
        Assert.assertEquals(2, directories.size());
        directory0 = directories.getJsonObject(0);
        Assert.assertNotNull(directory0.getString("id"));
        Assert.assertNotNull("/var/music/mainstream", directory0.getString("location"));
        Assert.assertTrue(directory0.getBoolean("active"));

        // Admin deletes the directories
        target().path("/directory/" + directory0Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .delete(JsonObject.class);

        target().path("/directory/" + directory1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .delete(JsonObject.class);

        // Check the deletion
        json = target().path("/directory").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        directories = json.getJsonArray("directories");
        Assert.assertNotNull(directories);
        Assert.assertEquals(0, directories.size());
    }

    /**
     * Test the collection indexing service.
     *
     * @throws Exception
     */
    @Test
    public void testCollectionIndexing() throws Exception {
        // Login users
        String adminAuthenticationToken = login("admin", "admin", false);

        // Admin adds a directory to the collection
        JsonObject json = target().path("/directory").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("location", Paths.get(getClass().getResource("/music/").toURI()).toString())), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Admin lists all directories
        json = target().path("/directory").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        JsonArray directories = json.getJsonArray("directories");
        Assert.assertNotNull(directories);
        Assert.assertEquals(1, directories.size());
        JsonObject directory0 = directories.getJsonObject(0);
        String directory0Id = directory0.getString("id");

        // Check that the albums are correctly added
        json = target().path("/album").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        JsonArray albums = json.getJsonArray("albums");
        Assert.assertNotNull(albums);
        Assert.assertEquals(2, albums.size());

        // Admin deletes the directory
        target().path("/directory/" + directory0Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .delete(JsonObject.class);

        // Check that the albums are correctly removed
        json = target().path("/album").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        albums = json.getJsonArray("albums");
        Assert.assertNotNull(albums);
        Assert.assertEquals(0, albums.size());
    }
}
