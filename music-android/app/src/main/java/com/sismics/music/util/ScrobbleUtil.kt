package com.sismics.music.util

import android.content.Context
import android.util.Log
import com.loopj.android.http.JsonHttpResponseHandler
import com.sismics.music.resource.PlayerResource
import org.apache.http.Header
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * Utility class for scrobbling.

 * @author bgamard.
 */
object ScrobbleUtil {

    /**
     * A track is complete.
     * @param context Context
     * *
     * @param trackId Track ID
     * *
     * @param date Date of completion
     */
    fun trackCompleted(context: Context, trackId: String, date: Long) {
        Log.d("ScrobbleUtil", "Track completed: $trackId at $date")
        try {
            // Get the current scrobble list
            var json = PreferenceUtil.getCachedJson(context, PreferenceUtil.Pref.SCROBBLE_JSON)
            if (json == null) {
                json = JSONObject()
                val array = JSONArray()
                json.put("scrobbles", array)
            }

            // Add the new scrobble
            val array = json.getJSONArray("scrobbles")
            val scrobble = JSONObject()
            scrobble.put("id", trackId)
            scrobble.put("date", date)
            array.put(scrobble)

            // Save the scrobble list back
            PreferenceUtil.setCachedJson(context, PreferenceUtil.Pref.SCROBBLE_JSON, json)
        } catch (e: JSONException) {
            Log.e("ScrobbleUtil", "Error adding a scrobble for track: " + trackId, e)
        }

    }

    /**
     * Try to scrobble everything.
     * @param context Context
     */
    fun sync(context: Context) {
        // Get the current scrobble list
        val json = PreferenceUtil.getCachedJson(context, PreferenceUtil.Pref.SCROBBLE_JSON) ?: return

        // Extract data
        val array = json.optJSONArray("scrobbles")
        val idList = ArrayList<String>()
        val dateList = ArrayList<Long>()
        for (i in 0..array.length() - 1) {
            val scrobble = array.optJSONObject(i)
            idList.add(scrobble.optString("id"))
            dateList.add(scrobble.optLong("date"))
        }

        if (idList.size > 0) {
            Log.d("ScrobbleUtil", "Scrobbling " + idList.size + " tracks")

            // Try to send the request to the server
            PlayerResource.listened(context, idList, dateList, object : JsonHttpResponseHandler() {
                override fun onSuccess(response: JSONObject?) {
                    Log.d("ScrobbleUtil", "Tracks successfully scrobbled")

                    // Clean the scrobble list on success
                    PreferenceUtil.setCachedJson(context, PreferenceUtil.Pref.SCROBBLE_JSON, null)
                }

                override fun onFailure(statusCode: Int, headers: Array<Header>?, responseBody: String?, e: Throwable) {
                    Log.d("ScrobbleUtil", "Error sending scrobbling request", e)
                }
            })
        }
    }
}
