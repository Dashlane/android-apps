package com.dashlane.autofill.actionssources.model

import android.content.pm.ApplicationInfo
import com.dashlane.ext.application.KnownApplication

sealed class ActionedFormSourceIcon {

    data class InstalledApplicationIcon(val applicationInfo: ApplicationInfo) : ActionedFormSourceIcon()

    class NotInstalledApplicationIcon private constructor(
        val title: String,
        val url: String
    ) : ActionedFormSourceIcon() {
        constructor(knownApplication: KnownApplication? = null) : this(
            knownApplication?.packageName ?: "",
            knownApplication?.mainDomain ?: ""
        )
    }

    data class UrlIcon(val url: String) : ActionedFormSourceIcon()

    object IncorrectSignatureIcon : ActionedFormSourceIcon()
}
