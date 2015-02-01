package com.sismics.music.core.util;

import java.nio.file.Path;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

/**
 * Parse a folder name and extract metadata or revert this process.
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
     * File name.
     */
    private String fileName;
    
    /**
     * Unknown artist name.
     */
    private static final String UNKNOWN_ARTIST = "Unknown";
    
    /**
     * Parse a file path into artist name and album name.
     * Never fails.
     * 
     * @param file File path
     */
    public DirectoryNameParser(Path file) throws Exception {
        fileName = file.getFileName().toString();
        String[] data = fileName.split(" - ", 2);
        if (data.length == 2) {
            artistName = data[0].trim();
            albumName = data[1].trim();
        }
        
        if (Strings.isNullOrEmpty(artistName) || Strings.isNullOrEmpty(albumName)) {
            artistName = UNKNOWN_ARTIST;
            albumName = fileName;
        }
    }
    
    /**
     * Rebuild a directory name from artist name and album name.
     * 
     * @param artistName Artist name
     * @param albumName Album name
     */
    public DirectoryNameParser(String artistName, String albumName) {
        if (UNKNOWN_ARTIST.equals(artistName)) {
            fileName = albumName;
        } else {
            fileName = artistName + " - " + albumName;
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
    
    /**
     * Getter of fileName.
     *
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("fileName", fileName)
                .add("artistName", artistName)
                .add("albumName", albumName)
                .toString();
    }
}
