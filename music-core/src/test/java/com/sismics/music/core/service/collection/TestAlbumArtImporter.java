package com.sismics.music.core.service.collection;

import com.sismics.music.core.service.albumart.AlbumArtImporter;
import junit.framework.Assert;
import org.junit.Test;

import java.io.File;

/**
 * Test of the album art importer.
 *
 * @author jtremeaux
 */
public class TestAlbumArtImporter {
    @Test
    public void testScanDirectoryNone() throws Exception {
        AlbumArtImporter importer = new AlbumArtImporter();
        File albumArt = importer.scanDirectory(new File(getClass().getResource("/albumart/none").toURI().getPath()));
        Assert.assertNull(albumArt);
    }

    @Test
    public void testScanDirectoryMany() throws Exception {
        AlbumArtImporter importer = new AlbumArtImporter();
        File albumArt = importer.scanDirectory(new File(getClass().getResource("/albumart/many").toURI().getPath()));
        Assert.assertNotNull(albumArt);
        Assert.assertEquals("albumart.png", albumArt.getName().toLowerCase());
    }

    @Test
    public void testScanDirectoryOne() throws Exception {
        AlbumArtImporter importer = new AlbumArtImporter();
        File albumArt = importer.scanDirectory(new File(getClass().getResource("/albumart/one").toURI().getPath()));
        Assert.assertNotNull(albumArt);
        Assert.assertEquals("front.png", albumArt.getName());
    }

    @Test
    public void testScanDirectoryOneFallback() throws Exception {
        AlbumArtImporter importer = new AlbumArtImporter();
        File albumArt = importer.scanDirectory(new File(getClass().getResource("/albumart/onefallback").toURI().getPath()));
        Assert.assertNotNull(albumArt);
        Assert.assertEquals("randomfile.jpeg", albumArt.getName());
    }
}
