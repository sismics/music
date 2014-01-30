package com.sismics.music.resource;

import android.content.Context;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Access to /album API.
 * 
 * @author bgamard
 */
public class AlbumResource extends BaseResource {

    public static void list(Context context, JsonHttpResponseHandler responseHandler) {
        init(context);

        client.get(getApiUrl(context) + "/album", responseHandler);
    }
}
