package com.dashlane.userfeatures

import com.dashlane.core.premium.PremiumStatus
import com.dashlane.featureflipping.FeatureFlipManager
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.AccountStatusRepository
import com.dashlane.util.asOptJSONObjectSequence
import com.dashlane.util.userfeatures.UserFeaturesChecker
import org.json.JSONObject

class UserFeaturesCheckerImpl(
    private val sessionManager: SessionManager,
    private val accountStatusRepository: AccountStatusRepository,
    private val featureFlipManager: FeatureFlipManager
) : UserFeaturesChecker {

    private val premiumStatus: PremiumStatus?
        get() = sessionManager.session?.let { accountStatusRepository.getPremiumStatus(it) }

    override fun has(vararg permissions: UserFeaturesChecker.FeatureFlip): Boolean {
        return permissions.all { hasUserFeature(it.value) }
    }

    override fun has(vararg permissions: UserFeaturesChecker.Capability): Boolean {
        return permissions.all { hasCapability(it.value) }
    }

    override fun getFeatureInfo(feature: UserFeaturesChecker.Capability): JSONObject {
        val capabilities = premiumStatus?.capabilities ?: return JSONObject()
        return capabilities.asOptJSONObjectSequence()
            .find { it?.optString("capability") == feature.value }
            ?.optJSONObject("info") ?: JSONObject()
    }

    private fun hasCapability(feature: String): Boolean {
        val capabilities = premiumStatus?.capabilities ?: return false
        
        return capabilities.asOptJSONObjectSequence()
            .find { it?.optString("capability") == feature }
            
            ?.optBoolean("enabled", false) == true
    }

    private fun hasUserFeature(feature: String): Boolean {
        val featureFlips = featureFlipManager.featureFlips ?: return false
        return featureFlips.contains(feature)
    }
}
