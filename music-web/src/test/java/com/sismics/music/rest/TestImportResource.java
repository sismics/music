package com.sismics.music.rest;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.sismics.music.core.util.DirectoryUtil;
import com.sismics.util.filter.TokenBasedSecurityFilter;
import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Exhaustive test of the import resource.
 * 
 * @author bgamard
 */
public class TestImportResource extends BaseJerseyTest {
    @Before
    public void setUp() throws Exception {
        super.setUp();

        // Cleanup imported files
        for (File file : DirectoryUtil.getImportAudioDirectory().listFiles()) {
            file.delete();
        }
        DirectoryUtil.getImportAudioDirectory().delete();
    }

    /**
     * Copy some music collection to a temporary directory (in case the collection service modifies, the collection,
     * the test wouldn't be idempotent).
     *
     * @param source The source collection
     * @return The directory of the copy
     */
    public File copyTempResource(String source) throws Exception {
        // This test is destructive, copy the test music to a temporary directory
        Path sourceDir = Paths.get(getClass().getResource(source).toURI());
        File destDir = Files.createTempDir();
        FileUtils.copyDirectory(sourceDir.toFile(), destDir);
        destDir.deleteOnExit();
        return destDir;
    }

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
        String adminAuthenticationToken = login("admin", "admin", false);
        
        // This test is destructive, copy the test music to a temporary directory
        File collectionDir = copyTempResource("/music/");

        // Admin adds a directory to the collection
        JsonObject json = target().path("/directory").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("location", collectionDir.toPath().toString())), JsonObject.class);
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
        
        // Admin cleanup imports
        json = target().path("/import/progress/cleanup").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .post(Entity.form(new Form()), JsonObject.class);
        
        // Wait for watching service to index our new music
        Thread.sleep(3000);
        
        // Admin import a new URL
        json = target().path("/import").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("url", "https://soundcloud.com/monstercat/au5-follow-you-volant")
                        .param("quality", "128K")
                        .param("format", "mp3")), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Wait for the process to start
        Thread.sleep(1000);
        
        // Admin check import progession
        json = target().path("/import/progress").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        JsonArray imports = json.getJsonArray("imports");
        Assert.assertEquals(1, imports.size());
        Assert.assertEquals("INPROGRESS", imports.getJsonObject(0).getString("status"));
        
        // Admin kills the current import
        json = target().path("/import/progress/" + imports.getJsonObject(0).getString("id") + "/kill").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .post(Entity.form(new Form()), JsonObject.class);
        
        // Wait for the process to be killed
        Thread.sleep(3000);
        
        // Admin check import progession
        json = target().path("/import/progress").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        imports = json.getJsonArray("imports");
        Assert.assertEquals(1, imports.size());
        Assert.assertEquals("ERROR", imports.getJsonObject(0).getString("status"));
    }
    
    /**
     * Test the import resource (retry).
     * youtube-dl is not available on Travis, can't be tested systematically.
     *
     */
    @Test
    @Ignore // youtube-dl is not installed on Travis
    public void testImportResourceRetry() throws Exception {
        // Login users
        String adminAuthenticationToken = login("admin", "admin", false);
        
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
    
    @Test
    @Ignore // youtube-dl is not installed on Travis
    public void testDependecies() throws Exception {
        // Login users
        String adminAuthenticationToken = login("admin", "admin", false);
        
        // Admin checks dependencies
        JsonObject json = target().path("/import/dependencies").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        System.out.println(json);
    }
    
    /**
     * Test the import resource (upload).
     *
     */
    @Test
    @SuppressWarnings("resource")
    public void testImportUpload() throws Exception {
        // Login users
        String adminAuthenticationToken = login("admin", "admin", false);

        // Admin import a ZIP
        try (InputStream is = Resources.getResource("music-album.zip").openStream()) {
            StreamDataBodyPart streamDataBodyPart = new StreamDataBodyPart("file", is, "music-album.zip");
            JsonObject json = target()
                    .register(MultiPartFeature.class)
                    .path("/import/upload").request()
                    .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                    .put(Entity.entity(new FormDataMultiPart().bodyPart(streamDataBodyPart),
                            MediaType.MULTIPART_FORM_DATA_TYPE), JsonObject.class);
            Assert.assertEquals("ok", json.getString("status"));
        }

        // Admin lists imported files
        JsonObject json = target().path("/import").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        JsonArray files = json.getJsonArray("files");
        Assert.assertEquals(3, files.size());

        // Admin import a single track
        try (InputStream is = Resources.getResource("music/Kevin MacLeod - Robot Brain/Robot Brain A.mp3").openStream()) {
            StreamDataBodyPart streamDataBodyPart = new StreamDataBodyPart("file", is, "Robot Brain A.mp3");
            json = target()
                    .register(MultiPartFeature.class)
                    .path("/import/upload").request()
                    .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                    .put(Entity.entity(new FormDataMultiPart().bodyPart(streamDataBodyPart),
                            MediaType.MULTIPART_FORM_DATA_TYPE), JsonObject.class);
            Assert.assertEquals("ok", json.getString("status"));
        }

        // Admin lists imported files
        json = target().path("/import").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        files = json.getJsonArray("files");
        Assert.assertEquals(4, files.size());

        // Admin import a non audio
        try (InputStream is = Resources.getResource("log4j.properties").openStream()) {
            StreamDataBodyPart streamDataBodyPart = new StreamDataBodyPart("file", is, "log4j.properties");
            Response response = target()
                    .register(MultiPartFeature.class)
                    .path("/import/upload").request()
                    .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                    .put(Entity.entity(new FormDataMultiPart().bodyPart(streamDataBodyPart),
                            MediaType.MULTIPART_FORM_DATA_TYPE));
            Assert.assertEquals(Status.INTERNAL_SERVER_ERROR, Status.fromStatusCode(response.getStatus()));
            json = response.readEntity(JsonObject.class);
            Assert.assertEquals("ImportError", json.getString("type"));
            Assert.assertEquals("File not supported", json.getString("message"));
        }

        // Admin lists imported files
        json = target().path("/import").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        files = json.getJsonArray("files");
        Assert.assertEquals(4, files.size());
    }

    /**
     * Test the tagging of imported music.
     *
     */
    @Test
    public void testTagImportedMusic() throws Exception {
        // Login users
        String adminAuthenticationToken = login("admin", "admin", false);

        // This test is destructive, copy the test music to a temporary directory
        File collectionDir = copyTempResource("/music2/");

        // Admin adds a directory to the collection
        JsonObject json = target().path("/directory").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("location", collectionDir.toPath().toString())), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Admin check that the collection is initialized properly
        json = target().path("/album")
                .queryParam("sort_column", "1")
                .queryParam("asc", "false")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        Assert.assertEquals(2, json.getJsonNumber("total").intValue());
        JsonArray albums = json.getJsonArray("albums");
        JsonObject album0 = albums.getJsonObject(0);
        String album0Id = album0.getString("id");
        String album0Name = album0.getString("name");
        long album0UpdateDate = album0.getJsonNumber("update_date").longValue();
        JsonObject artist0 = album0.getJsonObject("artist");
        String artist0Name = artist0.getString("name");
        JsonObject album1 = albums.getJsonObject(1);
        String album1Id = album1.getString("id");
        String album1Name = album1.getString("name");
        long album1UpdateDate = album1.getJsonNumber("update_date").longValue();
        JsonObject artist1 = album1.getJsonObject("artist");
        String artist1Name = artist1.getString("name");

        // Admin import a file
        try (InputStream is = Resources.getResource("music1/[A] Proxy - Coachella 2010 Day 01 Mixtape/03 Let It Loose Ft. Pharrell (Wale).mp3").openStream()) {
            StreamDataBodyPart streamDataBodyPart = new StreamDataBodyPart("file", is, "03 Let It Loose Ft. Pharrell (Wale).mp3");
            json = target()
                    .register(MultiPartFeature.class)
                    .path("/import/upload").request()
                    .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                    .put(Entity.entity(new FormDataMultiPart().bodyPart(streamDataBodyPart),
                            MediaType.MULTIPART_FORM_DATA_TYPE), JsonObject.class);
            Assert.assertEquals("ok", json.getString("status"));
        }

        // Admin lists imported files
        json = target().path("/import").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        JsonArray files = json.getJsonArray("files");
        Assert.assertEquals(1, files.size());
        String file0Name = files.getJsonObject(0).getString("file");

        // Admin tag 1 file of the 1st album
        json = target().path("/import").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .post(Entity.form(new Form()
                        .param("file", file0Name)
                        .param("order", "0")
                        .param("title", "tag0title")
                        .param("artist", artist1Name)
                        .param("album", album1Name)), JsonObject.class);

        // Wait for watching service to index our new music
        Thread.sleep(3000);

        // Check that the track is properly added, and is listed as most recent
        json = target().path("/album")
                .queryParam("sort_column", "1")
                .queryParam("asc", "false")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        Assert.assertEquals(2, json.getJsonNumber("total").intValue());
        Assert.assertEquals(album1Id, json.getJsonArray("albums").getJsonObject(0).getString("id"));
        Assert.assertEquals(album0Id, json.getJsonArray("albums").getJsonObject(1).getString("id"));
        Assert.assertTrue(json.getJsonArray("albums").getJsonObject(0).getJsonNumber("update_date").longValue() > album0UpdateDate);
        Assert.assertEquals(json.getJsonArray("albums").getJsonObject(1).getJsonNumber("update_date").longValue(), album0UpdateDate);
    }
}
