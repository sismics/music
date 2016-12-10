package com.sismics.music.util.form.validator

import android.content.Context

import com.sismics.music.R

import java.util.regex.Pattern

/**
 * Email validator.

 * @author bgamard
 */
class Email : ValidatorType {

    override fun validate(text: String): Boolean {
        return EMAIL_PATTERN.matcher(text).matches()
    }

    override fun getErrorMessage(context: Context): String {
        return context.resources.getString(R.string.validate_error_email)
    }

    companion object {

        /**
         * Pattern de validation.
         */
        private val EMAIL_PATTERN = Pattern.compile(".+@.+\\..+")
    }
}
