package com.sismics.music.model

import org.json.JSONObject

import java.io.Serializable

/**
 * A track.

 * @author bgamard.
 */
class Track : Serializable {

    /**
     * Track ID.
     */
    var id: String? = null

    /**
     * Track title.
     */
    var title: String? = null

    /**
     * Track length.
     */
    var length: Int = 0

    /**
     * Build a new track from JSON data.
     * @param track JSON data
     */
    constructor(track: JSONObject) {
        this.id = track.optString("id")
        this.title = track.optString("title")
        this.length = track.optInt("length")
    }

    /**
     * Build an empty track.
     */
    constructor() {
    }

    companion object {
        private val serialVersionUID = 0L
    }
}
