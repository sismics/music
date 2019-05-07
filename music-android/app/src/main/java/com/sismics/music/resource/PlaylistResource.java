package com.sismics.music.resource;

import android.content.Context;

import com.loopj.android.http.JsonHttpResponseHandler;

/**
 * Access to /playlist API.
 * 
 * @author bgamard
 */
public class PlaylistResource extends BaseResource {
    /**
     * List all playlists
     * @param context Context
     * @param responseHandler Response handler
     */
    public static void list(Context context, int offset, String search, JsonHttpResponseHandler responseHandler) {
        init(context);

        client.get(getApiUrl(context) + "/playlist?limit=20&offset=" + offset + "&search=" + search, responseHandler);
    }

    /**
     * Get the playlist data.
     * @param context Context
     * @param id Playlist ID
     * @param responseHandler Response handler
     */
    public static void info(Context context, String id, JsonHttpResponseHandler responseHandler) {
        init(context);

        client.get(getApiUrl(context) + "/playlist/" + id, responseHandler);
    }
}
