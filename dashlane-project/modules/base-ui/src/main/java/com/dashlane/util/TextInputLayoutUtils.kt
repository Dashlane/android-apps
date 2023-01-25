package com.dashlane.util

import android.text.method.PasswordTransformationMethod
import com.google.android.material.textfield.TextInputLayout



inline fun TextInputLayout.addOnFieldVisibilityToggleListener(crossinline listener: (visible: Boolean) -> Unit) {
    val editText = this.editText ?: throw IllegalStateException("editText is null")
    editText.addTextChangedListener {
        var wasFieldShown = false
        afterTextChanged {
            val isFieldShown = editText.transformationMethod !is PasswordTransformationMethod
            if (wasFieldShown != isFieldShown) {
                wasFieldShown = isFieldShown
                listener(isFieldShown)
            }
        }
    }
}