package com.sismics.music

import android.app.Application
import com.androidquery.callback.BitmapAjaxCallback
import com.sismics.music.model.ApplicationContext
import com.sismics.music.util.PreferenceUtil

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
class MainApplication : Application() {
    override fun onCreate() {
        // ACRA.init(this);

        // Fetching /user from cache
        val json = PreferenceUtil.getCachedJson(applicationContext, PreferenceUtil.Pref.CACHED_USER_INFO_JSON)
        ApplicationContext.setUserInfo(applicationContext, json)

        super.onCreate()
    }

    override fun onLowMemory() {
        BitmapAjaxCallback.clearCache()
    }
}
