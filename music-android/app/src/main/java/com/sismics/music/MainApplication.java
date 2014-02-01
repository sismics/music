package com.sismics.music;

import android.app.Application;

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
/*@ReportsCrashes(formKey = "",
        httpMethod = Method.PUT,
        reportType = Type.JSON,
        formUri = "http://acralyzer.sismics.com/music-report",
        formUriBasicAuthLogin = "reporter",
        formUriBasicAuthPassword = "jOS9ezJR",
        mode = ReportingInteractionMode.TOAST,
        forceCloseDialogAfterToast = true,
        resToastText = R.string.crash_toast_text)*/
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        // ACRA.init(this);

        // Fetching /user/info from cache
        JSONObject json = PreferenceUtil.getCachedJson(getApplicationContext(), PreferenceUtil.Pref.CACHED_USER_INFO_JSON);
        ApplicationContext.getInstance().setUserInfo(getApplicationContext(), json);

        super.onCreate();
    }
    
    @Override
    public void onLowMemory() {
        BitmapAjaxCallback.clearCache();
    }
}
