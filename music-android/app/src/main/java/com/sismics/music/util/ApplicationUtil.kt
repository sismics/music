package com.sismics.music.util

import android.content.Context
import android.content.pm.PackageManager.NameNotFoundException

/**
 * Utility class on general application data.

 * @author bgamard
 */
object ApplicationUtil {

    /**
     * Returns version name.
     * @param context Context
     * @return Version name
     */
    fun getVersionName(context: Context): String {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.versionName
        } catch (e: NameNotFoundException) {
            return ""
        }
    }

    /**
     * Returns version number.
     * @param context Context
     * @return Version code
     */
    fun getVersionCode(context: Context): Int {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.versionCode
        } catch (e: NameNotFoundException) {
            return 0
        }

    }
}
