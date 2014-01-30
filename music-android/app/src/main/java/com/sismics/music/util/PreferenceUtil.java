package com.sismics.music.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import org.json.JSONObject;

/**
 * Utility class on preferences.
 * 
 * @author bgamard
 */
public class PreferenceUtil {

    public enum Pref {
        CACHED_USER_INFO_JSON,
        SERVER_URL,
        CACHED_ALBUMS_LIST_JSON
    }

    /**
     * Returns a preference of boolean type.
     * @param context Context
     * @param key Shared preference key
     * @return Shared preference value
     */
    public static boolean getBooleanPreference(Context context, Pref key, boolean defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(key.name(), defaultValue);
    }
    
    /**
     * Returns a preference of string type.
     * @param context Context
     * @param key Shared preference key
     * @return Shared preference value
     */
    public static String getStringPreference(Context context, Pref key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(key.name(), null);
    }
    
    /**
     * Returns a preference of integer type.
     * @param context Context
     * @param key Shared preference key
     * @return Shared preference value
     */
    public static int getIntegerPreference(Context context, Pref key, int defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            String pref = sharedPreferences.getString(key.name(), "");
            try {
                return Integer.parseInt(pref);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } catch (ClassCastException e) {
            return sharedPreferences.getInt(key.name(), defaultValue);
        }
        
    }
    
    /**
     * Update JSON cache.
     * @param context Context
     * @param key Shared preference key
     * @param json JSON data
     */
    public static void setCachedJson(Context context, Pref key, JSONObject json) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(key.name(), json != null ? json.toString() : null).commit();
    }
    
    /**
     * Returns a JSON cache.
     * @param context Context
     * @param key Shared preference key
     * @return JSON data
     */
    public static JSONObject getCachedJson(Context context, Pref key) {
        try {
            return new JSONObject(getStringPreference(context, key));
        } catch (Exception e) {
            // The cache is not parsable, clean this up
            setCachedJson(context, key, null);
            return null;
        }
    }
    
    /**
     * Update server URL.
     * @param context Context
     */
    public static void setServerUrl(Context context, String serverUrl) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(Pref.SERVER_URL.name(), serverUrl).commit();
    }
    
    /**
     * Empty user caches.
     * @param context Context
     */
    public static void resetUserCache(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sharedPreferences.edit();
        editor.putString(Pref.CACHED_USER_INFO_JSON.name(), null);
        editor.commit();
    }
    
    /**
     * Returns cleaned server URL.
     * @param context Context
     * @return Server URL
     */
    public static String getServerUrl(Context context) {
        String serverUrl = getStringPreference(context, Pref.SERVER_URL);
        if (serverUrl == null) {
            return null;
        }
        
        // Trim
        serverUrl = serverUrl.trim();
        
        if (!serverUrl.startsWith("http")) {
            // Try to add http
            serverUrl = "http://" + serverUrl;
        }
        
        if (serverUrl.endsWith("/")) {
            // Delete last /
            serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
        }
        
        // Remove /api
        if (serverUrl.endsWith("/api")) {
            serverUrl = serverUrl.substring(0, serverUrl.length() - 4);
        }
        
        return serverUrl;
    }
}
