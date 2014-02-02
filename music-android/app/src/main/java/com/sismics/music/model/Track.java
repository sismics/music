package com.sismics.music.model;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * A track.
 *
 * @author bgamard.
 */
public class Track implements Serializable {
    private static final long serialVersionUID = 0L;

    /**
     * Track ID.
     */
    private String id;

    /**
     * Track title.
     */
    private String title;

    /**
     * Track length.
     */
    private int length;

    /**
     * Build a new track from JSON data.
     * @param track JSON data
     */
    public Track(JSONObject track) {
        this.id = track.optString("id");
        this.title = track.optString("title");
        this.length = track.optInt("length");
    }

    /**
     * Build an empty track.
     */
    public Track() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
