package com.dashlane.changemasterpassword

import android.content.Intent
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class ChangeMasterPasswordOrigin : Parcelable {
    abstract val sender: String
    abstract val fromLogin: Boolean

    @Parcelize
    data object Settings : ChangeMasterPasswordOrigin() {
        override val sender get() = "settings"
        override val fromLogin get() = false
    }

    @Parcelize
    data object Recovery : ChangeMasterPasswordOrigin() {
        override val sender get() = "recovery"
        override val fromLogin get() = true
    }

    @Parcelize
    data class Migration(
        val authTicket: String,
        val successIntent: Intent
    ) : ChangeMasterPasswordOrigin() {
        override val sender get() = "migration"
        override val fromLogin get() = true
    }
}