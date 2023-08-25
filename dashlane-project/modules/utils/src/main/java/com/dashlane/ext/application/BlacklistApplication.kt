package com.dashlane.ext.application

import com.dashlane.core.helpers.AppSignature

object BlacklistApplication {
    data class AutofillBlackList(
        override val packageName: String
    ) : KnownApplication {
        override val signatures: AppSignature? = null
        override val mainDomain: String? = null

        override fun isSecureToUse(url: String?): Boolean {
            return false
        }

        override fun canOpen(url: String?): Boolean {
            return false
        }
    }

    fun isAutofillBlackList(packageName: String): Boolean {
        return blackList.any { it.packageName == packageName }
    }

    private val blackList = listOf(
        blackListAutoFillApp("com.dashlane"),
        blackListAutoFillApp("com.dashlane.autofill"),
        blackListAutoFillApp("com.android"),
        blackListAutoFillApp("android"),
        blackListAutoFillApp("com.android.launcher"),
        blackListAutoFillApp("com.android.systemui"),
        blackListAutoFillApp("com.android.settings"),
        blackListAutoFillApp("com.android.launcher3"),
        blackListAutoFillApp("com.android.launcher2"),
        blackListAutoFillApp("com.android.captiveportallogin"),
        blackListAutoFillApp("com.samsung.android.email.provider"),
        blackListAutoFillApp("com.android.email"),
        blackListAutoFillApp("com.android.vending"),
        blackListAutoFillApp("mobi.mgeek.TunnyBrowser"),
        blackListAutoFillApp("com.android.mms"),
        blackListAutoFillApp("com.touchtype.swiftkey"),
        blackListAutoFillApp("com.lge.email"),
        blackListAutoFillApp("com.sec.android.app.billing"),
        blackListAutoFillApp("com.box.android"),
        blackListAutoFillApp("com.logmein.ignitionpro.android"),
        blackListAutoFillApp("com.appsverse.photon"),
        blackListAutoFillApp("fr.creditagricole.androidapp"),
        blackListAutoFillApp("com.google.android.apps.messaging"),
        blackListAutoFillApp("com.sec.android.app.myfiles"),
        blackListAutoFillApp("com.samsung.android.applock"),
        blackListAutoFillApp("com.oneplus.applocker"),
        blackListAutoFillApp("cris.org.in.prs.ima"),
        blackListAutoFillApp("com.forgepond.locksmith"),
        blackListAutoFillApp("com.android.htmlviewer"),
        blackListAutoFillApp("com.ea.gp.fifaultimate"),
        blackListAutoFillApp("com.android.calendar"),
        blackListAutoFillApp("com.google.android.apps.docs"),
        blackListAutoFillApp("com.android.contacts")
    )

    private fun blackListAutoFillApp(
        packageName: String
    ) = AutofillBlackList(packageName)
}