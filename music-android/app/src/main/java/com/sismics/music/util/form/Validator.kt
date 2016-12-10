package com.sismics.music.util.form

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import com.sismics.music.util.form.validator.ValidatorType
import java.util.*

/**
 * Utility for form validation.

 * @author bgamard
 */
class Validator(
        /**
         * True if the validator show validation errors.
         */
        private val showErrors: Boolean) {

    /**
     * List of validable elements.
     */
    private val validables = HashMap<View, Validable>()

    /**
     * Callback when the validation of one element has changed.
     */
    private var onValidationChanged: (() -> Unit)? = null

    /**
     * Setter of onValidationChanged.
     * @param onValidationChanged onValidationChanged
     */
    fun setOnValidationChanged(onValidationChanged: () -> Unit) {
        this.onValidationChanged = onValidationChanged
        onValidationChanged()
    }

    /**
     * Add a validable element.
     * @param editText EditText
     * *
     * @param validatorTypes Validator types
     */
    fun addValidable(context: Context, editText: EditText, vararg validatorTypes: ValidatorType) {
        val validable = Validable()
        validables.put(editText, validable)

        editText.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // NOP
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // NOP
            }

            override fun afterTextChanged(s: Editable) {
                validable.isValidated = true
                for (validatorType in validatorTypes) {
                    if (!validatorType.validate(s.toString())) {
                        if (showErrors) {
                            editText.error = validatorType.getErrorMessage(context)
                        }
                        validable.isValidated = false
                        break
                    }
                }

                if (validable.isValidated) {
                    editText.error = null
                }

                onValidationChanged?.invoke()
            }
        })
    }

    /**
     * Returns true if the element is validated.
     * @param view View to validate
     * *
     * @return
     */
    fun isValidated(view: View): Boolean {
        return validables[view]!!.isValidated
    }

    /**
     * Returns true if all elements are validated.
     * @return True if all elements are valid
     */
    val isValidated: Boolean
        get() {
            for (validable in validables.values) {
                if (!validable.isValidated) {
                    return false
                }
            }
            return true
        }
}
