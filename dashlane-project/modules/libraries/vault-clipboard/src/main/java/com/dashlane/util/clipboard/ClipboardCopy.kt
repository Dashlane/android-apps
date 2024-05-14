package com.dashlane.util.clipboard

import androidx.annotation.StringRes

interface ClipboardCopy {
    fun copyToClipboard(
        data: String,
        sensitiveData: Boolean,
        autoClear: Boolean = true,
        @StringRes feedback: Int? = null
    )
}