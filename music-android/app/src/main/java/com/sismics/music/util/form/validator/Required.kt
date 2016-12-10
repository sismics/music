package com.sismics.music.util.form.validator

import android.content.Context

import com.sismics.music.R

/**
 * Text presence validator.

 * @author bgamard
 */
class Required : ValidatorType {

    override fun validate(text: String): Boolean {
        return text.trim { it <= ' ' }.isNotEmpty()
    }

    override fun getErrorMessage(context: Context): String {
        return context.getString(R.string.validate_error_required)
    }

}
