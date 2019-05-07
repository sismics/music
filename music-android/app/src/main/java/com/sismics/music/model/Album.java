package com.sismics.music.model;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * An album.
 *
 * @author bgamard.
 */
public class Album implements Serializable {
    private static final long serialVersionUID = 0L;

    private String id;
    private String name;
    private String artistId;

    public Album() {
    }

    public Album(String id, String name, String artistId) {
        this.id = id;
        this.name = name;
        this.artistId = artistId;
    }

    /**
     * Build a new album from JSON data.
     * @param album JSON data
     */
    public Album(JSONObject album) {
        this.id = album.optString("id");
        this.name = album.optString("name");
        this.artistId = album.optJSONObject("artist").optString("id");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getArtistId() {
        return artistId;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Album
                && ((Album) obj).id.equals(id);
    }
}
