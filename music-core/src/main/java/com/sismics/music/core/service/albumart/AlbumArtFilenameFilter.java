package com.sismics.music.core.service.albumart;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Filters album art files.
 *
 * @author jtremeaux
 */
public class AlbumArtFilenameFilter implements FilenameFilter {
    @Override
    public boolean accept(File dir, String name) {
        if (name == null) {
            return false;
        }
        name = name.toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".gif") || name.endsWith(".bmp");
    }
}
