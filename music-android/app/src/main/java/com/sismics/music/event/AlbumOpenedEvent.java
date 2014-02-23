package com.sismics.music.event;

import com.sismics.music.model.Album;

/**
 * Open album event.
 *
 * @author bgamard
 */
public class AlbumOpenedEvent {

    private Album album;

    public AlbumOpenedEvent(Album album) {
        this.album = album;
    }

    public Album getAlbum() {
        return album;
    }
}
