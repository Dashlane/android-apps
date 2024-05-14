package com.dashlane.userfeatures

import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.featureflipping.FeatureFlipManager
import com.dashlane.server.api.endpoints.premium.PremiumStatus
import com.dashlane.session.SessionManager

class UserFeaturesCheckerImpl(
    private val sessionManager: SessionManager,
    private val accountStatusRepository: AccountStatusRepository,
    private val featureFlipManager: FeatureFlipManager
) : UserFeaturesChecker {

    private val premiumStatus: PremiumStatus?
        get() = sessionManager.session?.let { accountStatusRepository[it]?.premiumStatus }

    override fun has(vararg permissions: FeatureFlip): Boolean {
        return permissions.all { hasUserFeature(it.value) }
    }

    override fun has(vararg capabilities: PremiumStatus.Capabilitie.Capability): Boolean {
        return capabilities.all { hasCapability(it) }
    }

    private fun hasUserFeature(feature: String): Boolean {
        val featureFlips = featureFlipManager.featureFlips ?: return false
        return featureFlips.contains(feature)
    }

    private fun hasCapability(capability: PremiumStatus.Capabilitie.Capability): Boolean {
        val capabilities = premiumStatus?.capabilities ?: return false
        
        return capabilities
            .find { it.capability == capability }
            ?.takeIf { it.enabled } != null
    }

    override fun getCapabilityInfo(capability: PremiumStatus.Capabilitie.Capability): PremiumStatus.Capabilitie.Info? {
        val capabilities = premiumStatus?.capabilities ?: return null
        return capabilities
            .find { it.capability == capability }
            ?.info
    }
}
