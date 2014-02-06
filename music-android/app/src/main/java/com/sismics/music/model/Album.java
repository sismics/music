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

    /**
     * Album ID.
     */
    private String id;

    /**
     * Album name.
     */
    private String name;

    /**
     * Artist name.
     */
    private String artistName;

    /**
     * Build a new album from JSON data.
     * @param album JSON data
     */
    public Album(JSONObject album) {
        this.id = album.optString("id");
        this.name = album.optString("name");
        JSONObject artist = album.optJSONObject("artist");
        this.artistName = artist.optString("name");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }
}
