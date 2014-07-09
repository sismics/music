package com.sismics.music.core.util;

import java.nio.file.Path;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

/**
 * Parse a folder name and extract metadata.
 * 
 * @author bgamard
 */
public class DirectoryNameParser {
    /**
     * Artist name.
     */
    private String artistName;

    /**
     * Album name.
     */
    private String albumName;
    
    /**
     * Parse a file path.
     * 
     * @param file File path
     */
    public DirectoryNameParser(Path file) throws Exception {
        String[] data = file.getFileName().toString().split(" - ");
        if (data.length == 2) {
            artistName = data[0].trim();
            albumName = data[1].trim();
        }
        
        if (Strings.isNullOrEmpty(artistName) || Strings.isNullOrEmpty(albumName)) {
            throw new Exception("Directory name invalid: " + file.getFileName().toString());
        }
    }

    /**
     * Getter of artistName.
     *
     * @return the artistName
     */
    public String getArtistName() {
        return artistName;
    }

    /**
     * Getter of albumName.
     *
     * @return the albumName
     */
    public String getAlbumName() {
        return albumName;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("artistName", artistName)
                .add("albumName", albumName)
                .toString();
    }
}
