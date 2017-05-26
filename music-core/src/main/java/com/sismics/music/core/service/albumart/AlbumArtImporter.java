package com.sismics.music.core.service.albumart;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

/**
 * Import album arts from a directory.
 *
 * @author jtremeaux
 */
public class AlbumArtImporter {
    final static FilenameFilter ALBUM_ART_FILENAME_FILTER = new AlbumArtFilenameFilter();

    /**
     * Get the album art file from the directory.
     *
     * @param directory Directory
     * @return Album art file
     */
    public File scanDirectory(Path directory) {
        Map<Integer, File> fileMap = new TreeMap<>();
        for (File file : directory.toFile().listFiles(ALBUM_ART_FILENAME_FILTER)) { // XXX nio-ize
            String name = file.getName().toLowerCase();
            if (name.startsWith("albumart.")) {
                fileMap.put(0, file);
            } else if (name.startsWith("cover.")) {
                fileMap.put(1, file);
            } else if (name.startsWith("front.")) {
                fileMap.put(2, file);
            } else if (!fileMap.containsKey(3)) {
                fileMap.put(3, file);
            }
        }
        if (!fileMap.isEmpty()) {
            return fileMap.values().iterator().next();
        } else {
            return null;
        }
    }
}
