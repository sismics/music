package com.sismics.music.model;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * A playlist.
 *
 * @author jtremeaux.
 */
public class Playlist implements Serializable {
    private static final long serialVersionUID = 0L;

    private String id;
    private String name;

    public Playlist() {
    }

    /**
     * Build a new playlist from JSON data.
     * @param playlist JSON data
     */
    public Playlist(JSONObject playlist) {
        this.id = playlist.optString("id");
        this.name = playlist.optString("name");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Playlist
                && ((Playlist) obj).id.equals(id);
    }
}
