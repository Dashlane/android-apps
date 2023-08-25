package com.dashlane.ext.application

import com.dashlane.core.helpers.AppSignature
import com.dashlane.url.registry.popular.PopularService
import com.dashlane.url.registry.popular.PopularServicesRegistry
import com.dashlane.url.toUrlDomain
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KnownApplicationProvider @Inject constructor(
    private val whitelistApplication: WhitelistApplication
) {

    val popularServices by lazy {
        PopularServicesRegistry().loadAll()
    }

    private val knownApplications: List<KnownApplication.App> by lazy {
        whitelistApplication.whitelistApplication + popularServices.map { it.toKnownApplication() }
    }

    fun getKnownApplication(packageName: String): KnownApplication? =
        AutofillExtraDataApplication.getAppForPackage(packageName)
            ?: TrustedBrowserApplication.getAppForPackage(packageName)
            ?: whitelistApplication.getAppForPackage(packageName)
            ?: popularServices.find { it.packageName == packageName }?.toKnownApplication()

    fun getSignature(packageName: String, url: String?): AppSignature? {
        return getKnownApplication(packageName)
            ?.takeUnless { BlacklistApplication.isAutofillBlackList(packageName) }
            ?.takeIf { it.signatures != null }
            ?.takeIf { it.isSecureToUse(url) }
            ?.signatures
    }

    fun getPackageNamesCanOpen(url: String): Set<String> {
        return knownApplications.filter { it.canOpen(url) }.map { it.packageName }.toSet()
    }

    fun getWhitelistedApplication(packageName: String) = whitelistApplication.getAppForPackage(packageName)

    private fun PopularService.toKnownApplication() =
        KnownApplication.App(packageName, null, domain.toUrlDomain(), null, null, null)
}