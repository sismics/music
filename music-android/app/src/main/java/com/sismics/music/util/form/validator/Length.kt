package com.sismics.music.util.form.validator

import android.content.Context

import com.sismics.music.R

/**
 * Text length validator.

 * @author bgamard
 */
class Length(val minLength: Int, val maxLength: Int) : ValidatorType {

    /**
     * True if the last validation error was about a string too short.
     */
    private var tooShort: Boolean = false

    override fun validate(text: String): Boolean {
        tooShort = text.trim { it <= ' ' }.length < minLength
        return text.trim { it <= ' ' }.length >= minLength && text.trim { it <= ' ' }.length <= maxLength
    }

    override fun getErrorMessage(context: Context): String {
        if (tooShort) {
            return context.resources.getString(R.string.validate_error_length_min, minLength)
        }
        return context.resources.getString(R.string.validate_error_length_max, maxLength)
    }
}
