package com.sismics.music.resource;

import android.content.Context;

import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.sismics.music.util.PreferenceUtil;

import java.io.File;

/**
 * Access to /track API.
 * 
 * @author bgamard
 */
public class TrackResource extends BaseResource {
    /**
     * Download a track.
     *
     * @param context Context
     * @param id Track ID
     * @param responseHandler Response handler
     * @return Request handle used to cancel
     */
    public static RequestHandle download(Context context, String id, FileAsyncHttpResponseHandler responseHandler) {
        init(context);

        return client.get(getApiUrl(context) + "/track/" + id, responseHandler);
    }

    /**
     * POST /track/id/like.
     *
     * @param context Context
     * @param id Track ID
     * @param responseHandler Callback
     */
    public static void like(Context context, String id, JsonHttpResponseHandler responseHandler) {
        init(context);

        client.post(getApiUrl(context) + "/track/" + id + "/like", responseHandler);
    }

    /**
     * DELETE /track/id/like.
     *
     * @param context Context
     * @param id Track ID
     * @param responseHandler Callback
     */
    public static void unlike(Context context, String id, JsonHttpResponseHandler responseHandler) {
        init(context);

        client.delete(getApiUrl(context) + "/track/" + id + "/like", responseHandler);
    }
}
