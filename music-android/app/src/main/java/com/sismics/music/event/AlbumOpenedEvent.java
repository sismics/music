package com.sismics.music.event;

import com.sismics.music.model.Album;
import com.sismics.music.model.Artist;

/**
 * Open album event.
 *
 * @author bgamard
 */
public class AlbumOpenedEvent {

    private Artist artist;
    private Album album;

    public AlbumOpenedEvent(Artist artist, Album album) {
        this.artist = artist;
        this.album = album;
    }

    public Album getAlbum() {
        return album;
    }

    public Artist getArtist() {
        return artist;
    }
}
