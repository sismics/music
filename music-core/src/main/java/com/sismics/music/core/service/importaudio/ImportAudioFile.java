package com.sismics.music.core.service.importaudio;

import java.io.File;

/**
 * @author jtremeaux
 */
public class ImportAudioFile {
    private File file;

    private String title;

    private String artist;

    private String albumArtist;

    private String album;

    private Integer order;

    private Integer year;

    public ImportAudioFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbumArtist() {
        return albumArtist;
    }

    public void setAlbumArtist(String albumArtist) {
        this.albumArtist = albumArtist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}
