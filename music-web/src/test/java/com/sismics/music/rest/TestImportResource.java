package com.sismics.music.rest;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.sismics.util.filter.TokenBasedSecurityFilter;
import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Exhaustive test of the import resource.
 * 
 * @author bgamard
 */
public class TestImportResource extends BaseMusicTest {
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
     */
    @Test
    @Ignore // youtube-dl is not installed on Travis
    public void shouldImportFromWeb() throws Exception {
        // Login users
        String adminAuthenticationToken = login("admin", "admin", false);
        
        // This test is destructive, copy the test music to a temporary directory
        File collectionDir = copyTempResource("/music/");

        // Admin adds a directory to the collection
        JsonObject json = target().path("/directory").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("location", collectionDir.toPath().toString())), JsonObject.class);
        assertEquals("ok", json.getString("status"));
        
        // Admin import a new URL
        json = target().path("/import").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("url", "https://soundcloud.com/monstercat/au5-follow-you-volant")
                        .param("quality", "128K")
                        .param("format", "mp3")), JsonObject.class);
        assertEquals("ok", json.getString("status"));
        
        // Admin lists imported files
        json = target().path("/import").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        JsonArray files = json.getJsonArray("files");
        assertEquals(0, files.size());
        
        // Admin checks import progression
        boolean stop = false;
        while (!stop) {
            json = target().path("/import/progress").request()
                    .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                    .get(JsonObject.class);
            JsonArray imports = json.getJsonArray("imports");
            assertEquals(1, imports.size());
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
                assertEquals(0, files.size());
            }
            
            Thread.sleep(200);
        }
        
        // Admin lists imported files
        json = target().path("/import").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        files = json.getJsonArray("files");
        assertEquals(1, files.size());
        assertEquals("Au5 - Follow You (feat Danyka Nadeau) (Volant Remix).mp3", files.getJsonObject(0).getString("file"));
        
        // Admin move the imported file to the main directory
        json = target().path("/import").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .post(Entity.form(new Form()
                        .param("file", "Au5 - Follow You (feat Danyka Nadeau) (Volant Remix).mp3")
                        .param("artist", "Au5")
                        .param("album_artist", "Remixer")
                        .param("album", "Unsorted")
                        .param("title", "Follow You (feat Danyka Nadeau)")), JsonObject.class);
        assertEquals("ok", json.getString("status"));
        
        // Admin cleanup imports
        target().path("/import/progress/cleanup").request()
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
        assertEquals("ok", json.getString("status"));

        // Wait for the process to start
        Thread.sleep(1000);
        
        // Admin check import progession
        json = target().path("/import/progress").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        JsonArray imports = json.getJsonArray("imports");
        assertEquals(1, imports.size());
        assertEquals("INPROGRESS", imports.getJsonObject(0).getString("status"));
        
        // Admin kills the current import
        target().path("/import/progress/" + imports.getJsonObject(0).getString("id") + "/kill").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .post(Entity.form(new Form()), JsonObject.class);
        
        // Wait for the process to be killed
        Thread.sleep(3000);
        
        // Admin check import progession
        json = target().path("/import/progress").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        imports = json.getJsonArray("imports");
        assertEquals(1, imports.size());
        assertEquals("ERROR", imports.getJsonObject(0).getString("status"));
    }
    
    /**
     * Test the import resource (retry).
     * youtube-dl is not available on Travis, can't be tested systematically.
     *
     */
    @Test
    @Ignore // youtube-dl is not installed on Travis
    public void shouldRetryImport() throws Exception {
        // Login users
        String adminAuthenticationToken = login("admin", "admin", false);
        
        // Admin import a new URL
        JsonObject json = target().path("/import").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("url", "fakeurl")
                        .param("quality", "128K")
                        .param("format", "mp3")), JsonObject.class);
        assertEquals("ok", json.getString("status"));
        
        // Admin lists imported files
        json = target().path("/import").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        JsonArray files = json.getJsonArray("files");
        assertEquals(0, files.size());
        
        // Admin checks import progression
        json = target().path("/import/progress").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        JsonArray imports = json.getJsonArray("imports");
        assertEquals(1, imports.size());
        assertEquals("INPROGRESS", imports.getJsonObject(0).getString("status"));
        
        Thread.sleep(5000);
        
        // Admin checks import progression
        json = target().path("/import/progress").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        imports = json.getJsonArray("imports");
        assertEquals(1, imports.size());
        JsonObject imp = imports.getJsonObject(0);
        assertEquals("ERROR", imp.getString("status"));
        
        // Retry the failed import
        json = target().path("/import/progress/" + imp.getString("id") + "/retry").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .post(null, JsonObject.class);
        assertEquals("ok", json.getString("status"));
        
        // Admin checks import progression
        json = target().path("/import/progress").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        imports = json.getJsonArray("imports");
        assertEquals(1, imports.size());
        assertEquals("INPROGRESS", imports.getJsonObject(0).getString("status"));
        
        Thread.sleep(5000);
        
        // Admin checks import progression
        json = target().path("/import/progress").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        imports = json.getJsonArray("imports");
        assertEquals(1, imports.size());
        imp = imports.getJsonObject(0);
        assertEquals("ERROR", imp.getString("status"));
    }
    
    @Test
    @Ignore // youtube-dl is not installed on Travis
    public void shouldTestDependencies() throws Exception {
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
    public void shouldImportFromMp3File() throws Exception {
        // Login users
        loginAdmin();

        // Admin import a single track
        PUT("/import/upload", new HashMap<>(), ImmutableMap.of("file", getFile("/music/Kevin MacLeod - Robot Brain/Robot Brain A.mp3")));
        assertIsOk();

        // Admin lists imported files
        GET("/import");
        assertIsOk();
        JsonObject json = getJsonResult();
        JsonArray files = json.getJsonArray("files");
        assertEquals(1, files.size());

        // Admin import a non audio
        PUT("/import/upload", new HashMap<>(), ImmutableMap.of("file", getFile("/log4j.properties")));
        assertIsInternalServerError();
        json = getJsonResult();
        assertEquals("ImportError", json.getString("type"));
        assertEquals("File not supported", json.getString("message"));

        // Admin lists imported files
        GET("/import");
        assertIsOk();
        json = getJsonResult();
        files = json.getJsonArray("files");
        assertEquals(1, files.size());
    }

    /**
     * Test the import resource (upload).
     *
     */
    @Test
    public void shouldImportFromZipFile() throws Exception {
        // Login users
        loginAdmin();

        // Admin import a ZIP
        PUT("/import/upload", new HashMap<>(), ImmutableMap.of("file", getFile("/music-album.zip")));
        assertIsOk();

        // Admin lists imported files
        GET("/import");
        assertIsOk();
        JsonObject json = getJsonResult();
        JsonArray files = json.getJsonArray("files");
        assertEquals(3, files.size());
    }

    /**
     * Test the tagging of imported music.
     *
     */
    @Test
    public void shouldTagImportedMusic() throws Exception {
        // Login users
        loginAdmin();

        // Admin adds a directory to the collection
        addDirectory("/music2/");

        // Admin check that the collection is initialized properly
        GET("/album", ImmutableMap.of(
                "sort_column", "1",
                "asc", "false"));
        assertIsOk();
        JsonObject json = getJsonResult();
        assertEquals(2, json.getJsonNumber("total").intValue());
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
        PUT("/import/upload", new HashMap<>(), ImmutableMap.of("file", getFile("/music1/[A] Proxy - Coachella 2010 Day 01 Mixtape/03 Let It Loose Ft. Pharrell (Wale).mp3")));
        assertIsOk();

        // Admin lists imported files
        GET("/import");
        assertIsOk();
        json = getJsonResult();
        JsonArray files = json.getJsonArray("files");
        assertEquals(1, files.size());
        String file0Name = files.getJsonObject(0).getString("file");

        // Admin tag 1 file of the 1st album
        POST("/import", ImmutableMap.<String, String>builder()
                .put("file", file0Name)
                .put("order", "0")
                .put("title", "tag0title")
                .put("artist", artist1Name)
                .put("album", album1Name)
                .build());
        assertIsOk();

        // Wait for watching service to index our new music
        Thread.sleep(3000);

        // Check that the track is properly added, and is listed as most recent
        GET("/album", ImmutableMap.of(
                "sort_column", "1",
                "asc", "false"));
        assertIsOk();
        json = getJsonResult();
        assertEquals(2, json.getJsonNumber("total").intValue());
        assertEquals(album1Id, json.getJsonArray("albums").getJsonObject(0).getString("id"));
        assertEquals(album0Id, json.getJsonArray("albums").getJsonObject(1).getString("id"));
        assertTrue(json.getJsonArray("albums").getJsonObject(0).getJsonNumber("update_date").longValue() > album0UpdateDate);
        assertEquals(json.getJsonArray("albums").getJsonObject(1).getJsonNumber("update_date").longValue(), album0UpdateDate);
    }

    /**
     * Test the suggestion of imported music with ID3 tags.
     *
     */
    @Test
    @Ignore // TODO Fixme
    public void shouldSuggestTagWithId3() throws Exception {
        // Login users
        loginAdmin();

        // Admin import a zip file: OK
        PUT("/import/upload", new HashMap<>(), ImmutableMap.of("file", getFile("/tagging/withid3_1.zip")));
        assertIsOk();

        // Admin lists imported files: OK, tags read from ID3
        GET("/import");
        assertIsOk();
        JsonObject json = getJsonResult();
        JsonArray files = json.getJsonArray("files");
        assertEquals(2, files.size());
        JsonObject file = files.getJsonObject(0);
        assertEquals("01Track.mp3", file.getString("file"));
        assertEquals("AllttA", file.getString("title"));
        assertEquals("Alltta1", file.getString("artist"));
        assertEquals("Alltta2", file.getString("albumArtist"));
        assertEquals("The Upper Hand", file.getString("album"));
        assertEquals(1, file.getInt("order"));
        assertEquals(2017, file.getInt("year"));

        // Admin clear the imports: OK
        POST("/import/progress/cleanup");

        // Admin import a zip file: OK
        PUT("/import/upload", new HashMap<>(), ImmutableMap.of("file", getFile("/tagging/withid3_2.zip")));
        assertIsOk();

        // Admin lists imported files: OK, guess missing tags
        GET("/import");
        assertIsOk();
        json = getJsonResult();
        files = json.getJsonArray("files");
        assertEquals(2, files.size());
        file = files.getJsonObject(0);
        assertEquals("01Track.mp3", file.getString("file"));
        assertEquals("AllttA", file.getString("title"));
        assertEquals("Alltta", file.getString("artist"));
        assertEquals("Alltta", file.getString("albumArtist"));
        assertEquals("The Upper Hand", file.getString("album"));
        assertEquals(1, file.getInt("order"));
        assertEquals(2017, file.getInt("year"));
    }
}
