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
    /**
     * Download a track.
     * @param context Context
     * @param id PlaylistTrack ID
     * @param responseHandler Response handler
     * @return Request handle used to cancel
     */
    public static RequestHandle download(Context context, String id, FileAsyncHttpResponseHandler responseHandler) {
        init(context);

        return client.get(getApiUrl(context) + "/track/" + id, responseHandler);
    }
}
