package com.dashlane.util.clipboard

import androidx.annotation.StringRes
import com.dashlane.dagger.singleton.SingletonProvider

object ClipboardUtils {

    @JvmStatic
    fun copyToClipboard(
        data: String,
        sensitiveData: Boolean,
        autoClear: Boolean = true,
        @StringRes feedback: Int? = null
    ) {
        SingletonProvider.getClipboardCopy().copyToClipboard(data, sensitiveData, autoClear, feedback)
    }
}