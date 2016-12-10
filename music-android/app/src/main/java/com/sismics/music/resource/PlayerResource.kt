package com.sismics.music.resource

import android.content.Context
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestHandle
import com.loopj.android.http.RequestParams
import com.sismics.music.util.PreferenceUtil
import org.apache.http.message.BasicNameValuePair
import java.util.*

/**
 * Access to /player API.

 * @author bgamard
 */
class PlayerResource : BaseResource() {
    companion object {
        /**
         * Post a set of tracks played before.

         * @param context Context
         * *
         * @param idList Track IDs
         * *
         * @param dateList Dates of completion
         * *
         * @param responseHandler Response handler
         * *
         * @return Request handle used to cancel
         */
        fun listened(context: Context, idList: List<String>, dateList: List<Long>,
                     responseHandler: JsonHttpResponseHandler): RequestHandle {
            BaseResource.Companion.init(context)

            val params = object : RequestParams() {
                override fun getParamsList(): List<BasicNameValuePair> {
                    val lparams = LinkedList<BasicNameValuePair>()

                    for ((key, value) in urlParams) {
                        lparams.add(BasicNameValuePair(key, value))
                    }

                    lparams.addAll(getParamsList(null, urlParamsWithObjects))
                    return lparams
                }

                private fun getParamsList(key: String?, value: Any): List<BasicNameValuePair> {
                    val params = LinkedList<BasicNameValuePair>()
                    if (value is Map<*, *>) {
                        val map = value as Map<String, Any>
                        val list = ArrayList(map.keys)
                        // Ensure consistent ordering in query string
                        Collections.sort(list)
                        for (nestedKey in list) {
                            val nestedValue = map[nestedKey]
                            if (nestedValue != null) {
                                params.addAll(getParamsList(if (key == null) nestedKey else String.format("%s[%s]", key, nestedKey),
                                        nestedValue))
                            }
                        }
                    } else if (value is List<*>) {
                        val list = value as List<Any>
                        for (nestedValue in list) {
                            params.addAll(getParamsList(key, nestedValue))
                        }
                    } else if (value is Array<*>) {
                        val list = value as Array<Any>
                        for (nestedValue in list) {
                            params.addAll(getParamsList(key, nestedValue))
                        }
                    } else if (value is Set<*>) {
                        val set = value as Set<Any>
                        for (nestedValue in set) {
                            params.addAll(getParamsList(key, nestedValue))
                        }
                    } else if (value is String) {
                        params.add(BasicNameValuePair(key, value))
                    } else if (value is Long) {
                        params.add(BasicNameValuePair(key, value.toString()))
                    }
                    return params
                }
            }

            params.put("id", idList)
            params.put("date", dateList)

            return BaseResource.Companion.client.post(BaseResource.Companion.getApiUrl(context)!! + "/player/listened", params, responseHandler)
        }

        /**
         * POST /player/command.

         * @param context Context
         * @param token Token
         * @param json JSON
         * @param responseHandler Callback
         */
        fun command(context: Context, token: String, json: String, responseHandler: JsonHttpResponseHandler) {
            BaseResource.Companion.init(context)

            val params = RequestParams()
            params.put("token", token)
            params.put("json", json)
            BaseResource.Companion.client.post(PreferenceUtil.getServerUrl(context) + "/ws/player/command", params, responseHandler)
        }
    }
}
