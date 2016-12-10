package com.sismics.music.resource

import android.content.Context

import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams

/**
 * Access to /user API.

 * @author bgamard
 */
class UserResource : BaseResource() {
    companion object {

        /**
         * POST /user/login.
         * @param context Context
         * *
         * @param username Username
         * *
         * @param password Password
         * *
         * @param responseHandler Callback
         */
        fun login(context: Context, username: String, password: String, responseHandler: JsonHttpResponseHandler) {
            BaseResource.Companion.init(context)

            val params = RequestParams()
            params.put("username", username)
            params.put("password", password)
            params.put("remember", "true")
            BaseResource.Companion.client.post(BaseResource.Companion.getApiUrl(context)!! + "/user/login", params, responseHandler)
        }

        /**
         * GET /user.
         * @param context Context
         * *
         * @param responseHandler Callback
         */
        fun info(context: Context, responseHandler: JsonHttpResponseHandler) {
            BaseResource.Companion.init(context)

            val params = RequestParams()
            BaseResource.Companion.client.get(BaseResource.Companion.getApiUrl(context)!! + "/user", params, responseHandler)
        }

        /**
         * POST /user/logout.
         * @param context Context
         * *
         * @param responseHandler Callback
         */
        fun logout(context: Context, responseHandler: JsonHttpResponseHandler) {
            BaseResource.Companion.init(context)

            val params = RequestParams()
            BaseResource.Companion.client.post(BaseResource.Companion.getApiUrl(context)!! + "/user/logout", params, responseHandler)
        }
    }
}
