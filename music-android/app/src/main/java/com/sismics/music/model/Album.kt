package com.sismics.music.model

import org.json.JSONObject

import java.io.Serializable

/**
 * An album.

 * @author bgamard.
 */
class Album
/**
 * Build a new album from JSON data.
 * @param album JSON data
 */
(album: JSONObject) : Serializable {

    /**
     * Album ID.
     */
    var id: String? = null

    /**
     * Album name.
     */
    var name: String? = null

    /**
     * Artist name.
     */
    var artistName: String? = null

    init {
        this.id = album.optString("id")
        this.name = album.optString("name")
        val artist = album.optJSONObject("artist")
        this.artistName = artist.optString("name")
    }

    companion object {
        private val serialVersionUID = 0L
    }
}
