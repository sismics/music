package com.sismics.music.model;

import android.app.Activity;
import android.content.Context;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.sismics.music.listener.CallbackListener;
import com.sismics.music.resource.UserResource;
import com.sismics.music.service.PlaylistService;
import com.sismics.music.util.PreferenceUtil;

import org.json.JSONObject;

/**
 * Global context of the application.
 * 
 * @author bgamard
 */
public class ApplicationContext {
    /**
     * Singleton's instance.
     */
    private static ApplicationContext applicationContext;
    
    /**
     * Response of GET /user
     */
    private JSONObject userInfo;

    private PlaylistService playlistService;
    
    /**
     * Private constructor.
     */
    private ApplicationContext() {
        playlistService = new PlaylistService();
    }
    
    /**
     * Returns a singleton of ApplicationContext.
     * 
     * @return Singleton of ApplicationContext
     */
    public static ApplicationContext getInstance() {
        if (applicationContext == null) {
            applicationContext = new ApplicationContext();
        }
        return applicationContext;
    }
    
    /**
     * Returns true if current user is logged in.
     * @return True if the user is logged in
     */
    public boolean isLoggedIn() {
        return userInfo != null && !userInfo.optBoolean("anonymous");
    }

    /**
     * Setter of userInfo
     * @param json JSON
     */
    public void setUserInfo(Context context, JSONObject json) {
        this.userInfo = json;
        PreferenceUtil.setCachedJson(context, PreferenceUtil.Pref.CACHED_USER_INFO_JSON, json);
    }

    /**
     * Asynchronously get user info.
     * @param activity Activity
     * @param callbackListener Callback
     */
    public void fetchUserInfo(final Activity activity, final CallbackListener callbackListener) {
        UserResource.info(activity.getApplicationContext(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(final JSONObject json) {
                // Save data in application context
                if (!json.optBoolean("anonymous", true)) {
                    setUserInfo(activity.getApplicationContext(), json);
                }
            }

            @Override
            public void onFinish() {
                if (callbackListener != null) {
                    callbackListener.onComplete();
                }
            }
        });
    }

    public PlaylistService getPlaylistService() {
        return playlistService;
    }
}
