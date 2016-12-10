package com.sismics.music.util

import android.app.Activity
import android.app.AlertDialog
import com.sismics.music.R

/**
 * Utility class for dialogs.
 *
 * @author bgamard
 */
object DialogUtil {

    /**
     * Create a dialog with an OK button.
     *
     * @param activity Context activity
     * @param title Dialog title
     * @param message Dialog message
     */
    fun showOkDialog(activity: Activity?, title: Int, message: Int) {
        if (activity == null || activity.isFinishing) {
            return
        }

        AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .setNeutralButton(R.string.ok) {
                    dialog, id -> dialog.dismiss()
                }.create().show()
    }
}
