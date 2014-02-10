package com.sismics.music.core.dao.dbi;

import com.sismics.music.core.model.dbi.Directory;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test the directory model.
 *
 * @author jtremeaux
 */
public class DirectoryTest {
    @Test
    public void testUpdateNameFromLocation() {
        Directory directory = new Directory();
        directory.setLocation(null);
        directory.updateNameFromLocation();
        Assert.assertNull(directory.getName());

        directory.setLocation("/var");
        directory.updateNameFromLocation();
        Assert.assertEquals("var", directory.getName());

        directory.setLocation("/var/");
        directory.updateNameFromLocation();
        Assert.assertEquals("var", directory.getName());

        directory.setLocation("/var/music");
        directory.updateNameFromLocation();
        Assert.assertEquals("music", directory.getName());

        directory.setLocation("/var/music/");
        directory.updateNameFromLocation();
        Assert.assertEquals("music", directory.getName());

        directory.setLocation("/var/music/main");
        directory.updateNameFromLocation();
        Assert.assertEquals("main", directory.getName());

        directory.setLocation("//");
        directory.updateNameFromLocation();
        Assert.assertEquals("music", directory.getName());
    }
}
