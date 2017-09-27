package com.sismics.music.model;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * An artist.
 *
 * @author bgamard.
 */
public class Artist implements Serializable {
    private static final long serialVersionUID = 0L;

    /**
     * Artist ID.
     */
    private String id;

    /**
     * Artist name.
     */
    private String name;

    public Artist() {
    }

    /**
     * Build a new artist from JSON data.
     * @param artist JSON data
     */
    public Artist(JSONObject artist) {
        this.id = artist.optString("id");
        this.name = artist.optString("name");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
