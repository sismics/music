package com.sismics.music.rest;

import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;

/**
 * Test the locale resource.
 * 
 * @author jtremeaux
 */
public class TestLocaleResource extends BaseJerseyTest {
    /**
     * Test the locale resource.
     */
    @Test
    public void testLocaleResource() {
        JsonObject json = target().path("/locale").request().get(JsonObject.class);
        JsonArray locale = json.getJsonArray("locales");
        Assert.assertTrue(locale.size() > 0);
    }
}