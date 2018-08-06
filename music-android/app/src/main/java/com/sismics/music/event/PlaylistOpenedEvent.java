package com.sismics.music.event;

import com.sismics.music.model.Playlist;

/**
 * Open playlist event.
 *
 * @author jtremeaux
 */
public class PlaylistOpenedEvent {

    private Playlist playlist;

    public PlaylistOpenedEvent(Playlist playlist) {
        this.playlist = playlist;
    }

    public Playlist getPlaylist() {
        return playlist;
    }
}
