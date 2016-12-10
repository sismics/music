package com.sismics.music.model

import android.app.Activity
import android.content.Context

import com.loopj.android.http.JsonHttpResponseHandler
import com.sismics.music.resource.UserResource
import com.sismics.music.util.PreferenceUtil

import org.json.JSONObject

/**
 * Global context of the application.
 *
 * @author bgamard
 */
object ApplicationContext {

    /**
     * Response of GET /user
     */
    private var userInfo: JSONObject? = null

    /**
     * Returns true if current user is logged in.
     *
     * @return True if the user is logged in
     */
    val isLoggedIn: Boolean
        get() = userInfo != null && !userInfo!!.optBoolean("anonymous")

    /**
     * Setter of userInfo.
     *
     * @param json JSON
     */
    fun setUserInfo(context: Context, json: JSONObject?) {
        userInfo = json
        PreferenceUtil.setCachedJson(context, PreferenceUtil.Pref.CACHED_USER_INFO_JSON, json)
    }

    /**
     * Asynchronously get user info.
     *
     * @param activity Activity
     * @param callbackListener Callback
     */
    fun fetchUserInfo(activity: Activity, callbackListener: () -> Unit) {
        UserResource.info(activity.applicationContext, object : JsonHttpResponseHandler() {
            override fun onSuccess(json: JSONObject?) {
                // Save data in application context
                if (!json!!.optBoolean("anonymous", true)) {
                    setUserInfo(activity.applicationContext, json)
                }
            }

            override fun onFinish() {
                callbackListener()
            }
        })
    }
}