package com.dashlane.autofill.pause.model

import android.content.pm.ApplicationInfo
import com.dashlane.ext.application.KnownApplication

sealed class AutofillFormSourceIcon {
    data class WebsiteUrl(val url: String) : AutofillFormSourceIcon()

    data class InstalledApplication(val applicationInfo: ApplicationInfo) : AutofillFormSourceIcon()

    class NotInstalledApplication private constructor(
        val mainDomain: String?,
    ) : AutofillFormSourceIcon() {
        constructor(knownApplication: KnownApplication? = null) : this(
            knownApplication?.mainDomain,
        )
    }

    data object UnknownApplication : AutofillFormSourceIcon()
}