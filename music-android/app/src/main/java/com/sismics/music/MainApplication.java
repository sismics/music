package com.sismics.music;

import android.app.Application;
import android.content.Context;

import com.androidquery.callback.BitmapAjaxCallback;
import com.sismics.music.model.ApplicationContext;
import com.sismics.music.util.PreferenceUtil;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender.Method;
import org.acra.sender.HttpSender.Type;
import org.json.JSONObject;

/**
 * Main application.
 * 
 * @author bgamard
 */
@ReportsCrashes(
        formUri = "http://acraviz.sismics.com/api",
        formUriBasicAuthLogin = BuildConfig.APPLICATION_ID,
        formUriBasicAuthPassword = "TsEThfGJ6OvhfAN3xilxLbGU"
)
public class MainApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        ACRA.init(this);
    }

    @Override
    public void onCreate() {
        if (ACRA.isACRASenderServiceProcess()) {
            return;
        }

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
