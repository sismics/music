package com.sismics.music.rest;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;

import org.junit.Assert;
import org.junit.Test;

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
    public void testImportResource() throws Exception {
        // Login users
        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);
        
        // Admin import a new URL
        JsonObject json = target().path("/import").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("url", "https://soundcloud.com/monstercat/au5-follow-you-volant")
                        .param("quality", "128K")
                        .param("format", "mp3")), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        
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
            
            Thread.sleep(200);
        }
    }
}
