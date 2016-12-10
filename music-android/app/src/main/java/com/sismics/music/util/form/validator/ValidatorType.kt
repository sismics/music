package com.sismics.music.util.form.validator

import android.content.Context

/**
 * Interface for validation types.

 * @author bgamard
 */
interface ValidatorType {

    /**
     * Returns true if the validator is validated.
     * @param text
     * *
     * @return
     */
    fun validate(text: String): Boolean

    /**
     * Returns an error message.
     * @param context
     * *
     * @return
     */
    fun getErrorMessage(context: Context): String
}
