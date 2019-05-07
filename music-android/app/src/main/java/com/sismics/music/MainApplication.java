package com.sismics.music;

import android.app.Application;
import android.content.Context;

import com.androidquery.callback.BitmapAjaxCallback;
import com.sismics.music.model.ApplicationContext;
import com.sismics.music.util.PreferenceUtil;

import org.json.JSONObject;

/**
 * Main application.
 * 
 * @author bgamard
 */
public class MainApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        // Fetching /user from cache
        JSONObject json = PreferenceUtil.getCachedJson(getApplicationContext(), PreferenceUtil.Pref.CACHED_USER_INFO_JSON);
        ApplicationContext.getInstance().setUserInfo(getApplicationContext(), json);

        super.onCreate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        BitmapAjaxCallback.clearCache();
    }
}
