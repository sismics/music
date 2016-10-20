package com.sismics.music.model

import org.json.JSONObject

/**
 * A track.

 * @author bgamard.
 */
class Track(val id: String, val title: String, val length: Int = 0) {

    companion object {
        fun fromJson(track: JSONObject) : Track {
            return Track(
                    track.optString("id"),
                    track.optString("title"),
                    track.optInt("length"))
        }
    }
}
