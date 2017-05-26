package com.sismics.music.resource;

import android.content.Context;

import com.loopj.android.http.JsonHttpResponseHandler;

/**
 * Access to /album API.
 * 
 * @author bgamard
 */
public class AlbumResource extends BaseResource {
    /**
     * List all albums
     * @param context Context
     * @param responseHandler Response handler
     */
    public static void list(Context context, JsonHttpResponseHandler responseHandler) {
        init(context);

        client.get(getApiUrl(context) + "/album?limit=100000", responseHandler);
    }

    /**
     * Get the album data.
     * @param context Context
     * @param id Album ID
     * @param responseHandler Response handler
     */
    public static void info(Context context, String id, JsonHttpResponseHandler responseHandler) {
        init(context);

        client.get(getApiUrl(context) + "/album/" + id, responseHandler);
    }
}
