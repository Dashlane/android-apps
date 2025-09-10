package com.dashlane.autofill.phishing

import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import com.dashlane.autofill.util.formSourceIdentifier
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.featureflipping.canUseAntiPhishing
import com.dashlane.preference.PreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.url.toUrlDomainOrNull
import javax.inject.Inject

class AntiPhishingCapabilityChecker @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val sessionManager: SessionManager,
    private val userFeaturesChecker: UserFeaturesChecker,
) {
    private val userPreferencesManager: UserPreferencesManager?
        get() = sessionManager.session?.let { session -> preferencesManager[session.username] }

    fun showAntiPhishingWarning(formSource: AutoFillFormSource, itemWebsite: String?): Boolean {
        val currentUserPreferencesManager = userPreferencesManager ?: return false
        val canSeeAntiPhishingAlerts =
            currentUserPreferencesManager.isAntiPhishingEnable && userFeaturesChecker.canUseAntiPhishing()
        val itemUrlDomain = itemWebsite?.toUrlDomainOrNull() ?: return false
        val suspiciousUrlDomain = (formSource as? WebDomainFormSource)?.formSourceIdentifier
            ?.toUrlDomainOrNull()
            ?: return false
        val doesWebsitesMatch = itemUrlDomain == suspiciousUrlDomain

        return canSeeAntiPhishingAlerts && !doesWebsitesMatch
    }
}