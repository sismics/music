package com.sismics.music.resource

import android.content.Context

import com.loopj.android.http.JsonHttpResponseHandler

/**
 * Access to /album API.
 *
 * @author bgamard
 */
class AlbumResource : BaseResource() {
    companion object {
        /**
         * List all albums.
         *
         * @param context Context
         * @param responseHandler Response handler
         */
        fun list(context: Context, responseHandler: JsonHttpResponseHandler) {
            BaseResource.init(context)

            BaseResource.client.get(BaseResource.getApiUrl(context)!! + "/album?limit=0", responseHandler)
        }

        /**
         * Get the album data.
         *
         * @param context Context
         * @param id Album ID
         * @param responseHandler Response handler
         */
        fun info(context: Context, id: String, responseHandler: JsonHttpResponseHandler) {
            BaseResource.init(context)

            BaseResource.client.get(BaseResource.getApiUrl(context) + "/album/" + id, responseHandler)
        }
    }
}
