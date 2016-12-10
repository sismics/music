package com.sismics.music.util

import android.content.Context
import android.preference.PreferenceManager
import com.loopj.android.http.PersistentCookieStore
import org.json.JSONObject

/**
 * Utility class on preferences.

 * @author bgamard
 */
object PreferenceUtil {

    enum class Pref {
        CACHED_USER_INFO_JSON,
        SERVER_URL,
        CACHED_ALBUMS_LIST_JSON,
        SCROBBLE_JSON,
        OFFLINE_MODE,
        PLAYER_TOKEN
    }

    /**
     * Returns a preference of boolean type.

     * @param context Context
     * *
     * @param key Shared preference key
     * *
     * @return Shared preference value
     */
    fun getBooleanPreference(context: Context, key: Pref, defaultValue: Boolean): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPreferences.getBoolean(key.name, defaultValue)
    }

    /**
     * Returns a preference of string type.

     * @param context Context
     * *
     * @param key Shared preference key
     * *
     * @return Shared preference value
     */
    fun getStringPreference(context: Context, key: Pref): String {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPreferences.getString(key.name, null)
    }

    /**
     * Returns a preference of integer type.

     * @param context Context
     * *
     * @param key Shared preference key
     * *
     * @return Shared preference value
     */
    fun getIntegerPreference(context: Context, key: Pref, defaultValue: Int): Int {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        try {
            val pref = sharedPreferences.getString(key.name, "")
            try {
                return Integer.parseInt(pref)
            } catch (e: NumberFormatException) {
                return defaultValue
            }

        } catch (e: ClassCastException) {
            return sharedPreferences.getInt(key.name, defaultValue)
        }

    }

    /**
     * Update JSON cache.

     * @param context Context
     * *
     * @param key Shared preference key
     * *
     * @param json JSON data
     */
    fun setCachedJson(context: Context, key: Pref, json: JSONObject?) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPreferences.edit().putString(key.name, json?.toString()).commit()
    }

    /**
     * Returns a JSON cache.

     * @param context Context
     * *
     * @param key Shared preference key
     * *
     * @return JSON data
     */
    fun getCachedJson(context: Context, key: Pref): JSONObject? {
        try {
            return JSONObject(getStringPreference(context, key))
        } catch (e: Exception) {
            // The cache is not parsable, clean this up
            setCachedJson(context, key, null)
            return null
        }

    }

    /**
     * Remove server URL.
     * @param context Context
     */
    fun removeServerUrl(context: Context) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPreferences.edit().remove(Pref.SERVER_URL.name).commit()
    }

    /**
     * Update server URL.
     * @param context Context
     */
    fun setServerUrl(context: Context, serverUrl: String) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPreferences.edit().putString(Pref.SERVER_URL.name, serverUrl).commit()
    }

    /**
     * Update player token.
     * @param context Context
     */
    fun setPlayerToken(context: Context, serverUrl: String) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPreferences.edit().putString(Pref.PLAYER_TOKEN.name, serverUrl).commit()
    }

    /**
     * Empty user caches.
     * @param context Context
     */
    fun resetUserCache(context: Context) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putString(Pref.CACHED_USER_INFO_JSON.name, null)
        editor.putString(Pref.CACHED_ALBUMS_LIST_JSON.name, null)
        editor.putString(Pref.PLAYER_TOKEN.name, null)
        editor.commit()
    }

    /**
     * Returns cleaned server URL.
     * @param context Context
     * *
     * @return Server URL
     */
    fun getServerUrl(context: Context): String {
        var serverUrl: String = getStringPreference(context, Pref.SERVER_URL)

        // Trim
        serverUrl = serverUrl.trim { it <= ' ' }

        if (!serverUrl.startsWith("http")) {
            // Try to add http
            serverUrl = "http://" + serverUrl
        }

        if (serverUrl.endsWith("/")) {
            // Delete last /
            serverUrl = serverUrl.substring(0, serverUrl.length - 1)
        }

        // Remove /api
        if (serverUrl.endsWith("/api")) {
            serverUrl = serverUrl.substring(0, serverUrl.length - 4)
        }

        return serverUrl
    }

    /**
     * Returns auth token cookie from shared preferences.

     * @return Auth token
     */
    fun getAuthToken(context: Context): String? {
        val cookieStore = PersistentCookieStore(context)
        val cookieList = cookieStore.cookies
        for (cookie in cookieList) {
            if (cookie.name == "auth_token") {
                return cookie.value
            }
        }

        return null
    }
}
