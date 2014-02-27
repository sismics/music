package com.sismics.music.rest;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test the theme resource.
 * 
 * @author jtremeaux
 */
public class TestThemeResource extends BaseJerseyTest {
    /**
     * Test the theme resource.
     * 
     * @throws JSONException
     */
    @Test
    public void testThemeResource() {
        JsonObject json = target().path("/theme").request().get(JsonObject.class);
        JsonArray theme = json.getJsonArray("themes");
        Assert.assertTrue(theme.size() > 0);
    }
 }