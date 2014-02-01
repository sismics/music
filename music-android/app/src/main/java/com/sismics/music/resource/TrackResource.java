package com.sismics.music.resource;

import android.content.Context;

import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;

import java.io.File;

/**
 * Access to /track API.
 * 
 * @author bgamard
 */
public class TrackResource extends BaseResource {

    public static RequestHandle download(Context context, String id, FileAsyncHttpResponseHandler responseHandler) {
        init(context);

        return client.get(getApiUrl(context) + "/track/" + id, responseHandler);
    }
}
