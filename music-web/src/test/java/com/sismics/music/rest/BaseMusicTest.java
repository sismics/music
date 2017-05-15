package com.sismics.music.rest;

import com.google.common.collect.ImmutableMap;
import com.sismics.music.core.util.DirectoryUtil;
import org.apache.commons.io.FileUtils;
import org.junit.Before;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author jtremeaux
 */
public abstract class BaseMusicTest extends BaseJerseyTest {
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
     * Add a music directory to the collection.
     * Make a defensive copy first, as tags / album art can be modified from this directory later.
     *
     * @param directoryName The directory to add
     * @return The temp directory
     */
    protected File addDirectory(String directoryName) throws Exception {
        Path tempDirectory = Files.createTempDirectory("music");
        FileUtils.copyDirectory(new File(getClass().getResource(directoryName).toURI()), tempDirectory.toFile());

        PUT("/directory", ImmutableMap.of("location", tempDirectory.toString()));
        assertIsOk();

        return tempDirectory.toFile();
    }
}
