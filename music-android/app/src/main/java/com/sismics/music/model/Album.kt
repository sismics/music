package com.sismics.music.model

import org.json.JSONObject

import java.io.Serializable

/**
 * An album.
 *
 * @author bgamard.
 */
class Album(val id: String, val name: String, val artistName: String) : Serializable {
    companion object {
        fun fromJson(album: JSONObject) : Album {
            return Album(
                    album.optString("id"),
                    album.optString("name"),
                    album.optJSONObject("artist")
                            .optString("name"))
        }
        private val serialVersionUID = 0L
    }
}
