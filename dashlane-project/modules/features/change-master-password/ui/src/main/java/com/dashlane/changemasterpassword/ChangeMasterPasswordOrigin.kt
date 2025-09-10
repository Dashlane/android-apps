package com.dashlane.changemasterpassword

import android.content.Intent
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class ChangeMasterPasswordOrigin : Parcelable {
    abstract val fromLogin: Boolean

    @Parcelize
    data object Settings : ChangeMasterPasswordOrigin() {
        override val fromLogin get() = false
    }

    @Parcelize
    data object BiometricRecovery : ChangeMasterPasswordOrigin() {
        override val fromLogin get() = true
    }

    @Parcelize
    data object AccountRecovery : ChangeMasterPasswordOrigin() {
        override val fromLogin get() = true
    }

    @Parcelize
    data class Migration(
        val authTicket: String,
        val successIntent: Intent,
    ) : ChangeMasterPasswordOrigin() {
        override val fromLogin get() = true
    }
}