package com.sismics.music.util;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.sismics.music.resource.PlayerResource;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for scrobbling.
 *
 * @author bgamard.
 */
public class ScrobbleUtil {

    /**
     * A track is complete.
     * @param context Context
     * @param trackId Track ID
     * @param date Date of completion
     */
    public static void trackCompleted(Context context, String trackId, long date) {
        Log.d("ScrobbleUtil", "Track completed: " + trackId + " at " + date);
        try {
            // Get the current scrobble list
            JSONObject json = PreferenceUtil.getCachedJson(context, PreferenceUtil.Pref.SCROBBLE_JSON);
            if (json == null) {
                json = new JSONObject();
                JSONArray array = new JSONArray();
                json.put("scrobbles", array);
            }

            // Add the new scrobble
            JSONArray array = json.getJSONArray("scrobbles");
            JSONObject scrobble = new JSONObject();
            scrobble.put("id", trackId);
            scrobble.put("date", date);
            array.put(scrobble);

            // Save the scrobble list back
            PreferenceUtil.setCachedJson(context, PreferenceUtil.Pref.SCROBBLE_JSON, json);
        } catch (JSONException e) {
            Log.e("ScrobbleUtil", "Error adding a scrobble for track: " + trackId, e);
        }
    }

    /**
     * Try to scrobble everything.
     * @param context Context
     */
    public static void sync(final Context context) {
        // Get the current scrobble list
        JSONObject json = PreferenceUtil.getCachedJson(context, PreferenceUtil.Pref.SCROBBLE_JSON);
        if (json == null) {
            return;
        }

        // Extract data
        JSONArray array = json.optJSONArray("scrobbles");
        List<String> idList = new ArrayList<>();
        List<Long> dateList = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject scrobble = array.optJSONObject(i);
            idList.add(scrobble.optString("id"));
            dateList.add(scrobble.optLong("date"));
        }

        if (idList.size() > 0) {
            Log.d("ScrobbleUtil", "Scrobbling " + idList.size() + " tracks");

            // Try to send the request to the server
            PlayerResource.listened(context, idList, dateList, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(JSONObject response) {
                    Log.d("ScrobbleUtil", "Tracks successfully scrobbled");

                    // Clean the scrobble list on success
                    PreferenceUtil.setCachedJson(context, PreferenceUtil.Pref.SCROBBLE_JSON, null);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable e) {
                    Log.d("ScrobbleUtil", "Error sending scrobbling request", e);
                }
            });
        }
    }
}
