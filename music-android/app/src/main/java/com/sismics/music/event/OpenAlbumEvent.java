package com.sismics.music.event;

import com.sismics.music.model.Album;

/**
 * Open album event.
 *
 * @author bgamard
 */
public class OpenAlbumEvent {

    private Album album;

    public OpenAlbumEvent(Album album) {
        this.album = album;
    }

    public Album getAlbum() {
        return album;
    }
}
