package com.sismics.music.resource

import android.content.Context
import com.loopj.android.http.FileAsyncHttpResponseHandler
import com.loopj.android.http.RequestHandle

/**
 * Access to /track API.

 * @author bgamard
 */
class TrackResource : BaseResource() {
    companion object {
        /**
         * Download a track.
         * @param context Context
         * *
         * @param id PlaylistTrack ID
         * *
         * @param responseHandler Response handler
         * *
         * @return Request handle used to cancel
         */
        fun download(context: Context, id: String, responseHandler: FileAsyncHttpResponseHandler): RequestHandle {
            BaseResource.Companion.init(context)

            return BaseResource.Companion.client.get(BaseResource.Companion.getApiUrl(context) + "/track/" + id, responseHandler)
        }
    }
}
