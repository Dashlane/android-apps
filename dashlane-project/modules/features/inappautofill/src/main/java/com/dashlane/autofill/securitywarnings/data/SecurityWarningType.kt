package com.dashlane.autofill.securitywarnings.data

import android.os.Parcelable
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import kotlinx.parcelize.Parcelize

sealed class SecurityWarningType : Parcelable {
    abstract val formSource: AutoFillFormSource?
    abstract val doNotShowAgainChecked: Boolean

    @Parcelize
    data class Mismatch(
        override val formSource: AutoFillFormSource?,
        override val doNotShowAgainChecked: Boolean
    ) : SecurityWarningType(), Parcelable

    @Parcelize
    data class Unknown(
        override val formSource: AutoFillFormSource?
    ) : SecurityWarningType(), Parcelable {
        override val doNotShowAgainChecked: Boolean
            get() = false
    }

    @Parcelize
    data class Incorrect(
        override val formSource: AutoFillFormSource?,
        override val doNotShowAgainChecked: Boolean
    ) : SecurityWarningType(), Parcelable
}
