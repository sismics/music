package com.sismics.music.util.form.validator

import android.content.Context

import com.sismics.music.R

import java.util.regex.Pattern

/**
 * Alphanumeric validator.

 * @author bgamard
 */
class Alphanumeric : ValidatorType {

    override fun validate(text: String): Boolean {
        return ALPHANUMERIC_PATTERN.matcher(text).matches()
    }

    override fun getErrorMessage(context: Context): String {
        return context.getString(R.string.validate_error_alphanumeric)
    }

    companion object {

        private val ALPHANUMERIC_PATTERN = Pattern.compile("[a-zA-Z0-9_]+")
    }

}
