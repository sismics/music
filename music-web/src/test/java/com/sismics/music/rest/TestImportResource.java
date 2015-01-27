package com.sismics.music.rest;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.io.Files;
import com.sismics.util.filter.TokenBasedSecurityFilter;

/**
 * Exhaustive test of the import resource.
 * 
 * @author bgamard
 */
public class TestImportResource extends BaseJerseyTest {
    /**
     * Test the import resource.
     * youtube-dl is not available on Travis, can't be tested systematically.
     *
     * @throws Exception
     */
    @Test
    @Ignore // youtube-dl is not installed on Travis
    public void testImportResource() throws Exception {
        // Login users
        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);
        
        // This test is destructive, copy the test music to a temporary directory
        Path sourceDir = Paths.get(getClass().getResource("/music/").toURI());
        File destDir = Files.createTempDir();
        FileUtils.copyDirectory(sourceDir.toFile(), destDir);
        destDir.deleteOnExit();
        
        // Admin adds a directory to the collection
        JsonObject json = target().path("/directory").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("location", destDir.toPath().toString())), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        
        // Admin import a new URL
        json = target().path("/import").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("url", "https://soundcloud.com/monstercat/au5-follow-you-volant")
                        .param("quality", "128K")
                        .param("format", "mp3")), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        
        // Admin lists imported files
        json = target().path("/import").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        JsonArray files = json.getJsonArray("files");
        Assert.assertEquals(0, files.size());
        
        // Admin checks import progression
        boolean stop = false;
        while (!stop) {
            json = target().path("/import/progress").request()
                    .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                    .get(JsonObject.class);
            JsonArray imports = json.getJsonArray("imports");
            Assert.assertEquals(1, imports.size());
            System.out.println(imports.getJsonObject(0));
            
            if (imports.getJsonObject(0).getString("status").equals("DONE")) {
                stop = true;
            }
            
            if (!stop) {
                // Admin lists imported files
                json = target().path("/import").request()
                        .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                        .get(JsonObject.class);
                files = json.getJsonArray("files");
                Assert.assertEquals(0, files.size());
            }
            
            Thread.sleep(200);
        }
        
        // Admin lists imported files
        json = target().path("/import").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        files = json.getJsonArray("files");
        Assert.assertEquals(1, files.size());
        Assert.assertEquals("Au5 - Follow You (feat Danyka Nadeau) (Volant Remix).mp3", files.getJsonObject(0).getString("file"));
        
        // Admin move the imported file to the main directory
        json = target().path("/import").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .post(Entity.form(new Form()
                        .param("file", "Au5 - Follow You (feat Danyka Nadeau) (Volant Remix).mp3")
                        .param("artist", "Au5")
                        .param("album_artist", "Remixer")
                        .param("album", "Unsorted")
                        .param("title", "Follow You (feat Danyka Nadeau)")), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        
        // Wait for watching service to index our new music
        Thread.sleep(5000);
    }
    
    /**
     * Test the import resource (retry).
     * youtube-dl is not available on Travis, can't be tested systematically.
     *
     * @throws Exception
     */
    @Test
    @Ignore // youtube-dl is not installed on Travis
    public void testImportResourceRetry() throws Exception {
        // Login users
        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);
        
        // Admin import a new URL
        JsonObject json = target().path("/import").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("url", "fakeurl")
                        .param("quality", "128K")
                        .param("format", "mp3")), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        
        // Admin lists imported files
        json = target().path("/import").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        JsonArray files = json.getJsonArray("files");
        Assert.assertEquals(0, files.size());
        
        // Admin checks import progression
        json = target().path("/import/progress").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        JsonArray imports = json.getJsonArray("imports");
        Assert.assertEquals(1, imports.size());
        Assert.assertEquals("INPROGRESS", imports.getJsonObject(0).getString("status"));
        
        Thread.sleep(5000);
        
        // Admin checks import progression
        json = target().path("/import/progress").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        imports = json.getJsonArray("imports");
        Assert.assertEquals(1, imports.size());
        JsonObject imp = imports.getJsonObject(0);
        Assert.assertEquals("ERROR", imp.getString("status"));
        
        // Retry the failed import
        json = target().path("/import/progress/" + imp.getString("id") + "/retry").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .post(null, JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        
        // Admin checks import progression
        json = target().path("/import/progress").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        imports = json.getJsonArray("imports");
        Assert.assertEquals(1, imports.size());
        Assert.assertEquals("INPROGRESS", imports.getJsonObject(0).getString("status"));
        
        Thread.sleep(5000);
        
        // Admin checks import progression
        json = target().path("/import/progress").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        imports = json.getJsonArray("imports");
        Assert.assertEquals(1, imports.size());
        imp = imports.getJsonObject(0);
        Assert.assertEquals("ERROR", imp.getString("status"));
    }
}
