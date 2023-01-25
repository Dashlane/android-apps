package com.dashlane.core.premium

import com.dashlane.premium.R
import com.dashlane.premium.offer.details.ConflictingBillingPlatformProvider
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.AccountStatusRepository
import com.dashlane.ui.model.TextResource
import javax.inject.Inject

class ConflictingBillingPlatformProviderImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val accountStatusRepository: AccountStatusRepository
) : ConflictingBillingPlatformProvider {
    override fun getWarning(): TextResource? {
        val premiumStatus = getPremiumStatus() ?: return null
        return getWarning(premiumStatus)
    }

    private fun getWarning(premiumStatus: PremiumStatus) =
        if (currentPlanIsRenewableType(premiumStatus)) {
            when (premiumStatus.getBillingPlatform()) {
                BillingPlatform.PLAY_STORE -> null
                BillingPlatform.APP_STORE -> {
                    TextResource.StringText(stringRes = R.string.billing_platform_conflict_warning_app_store)
                }
                BillingPlatform.PAYPAL, BillingPlatform.STRIPE -> {
                    TextResource.StringText(stringRes = R.string.billing_platform_conflict_warning_web_Store)
                }
                else -> TextResource.StringText(stringRes = R.string.billing_platform_conflict_warning_fallback)
            }
        } else {
            null
        }

    private fun getPremiumStatus(): PremiumStatus? {
        val session = sessionManager.session ?: return null
        return accountStatusRepository.getPremiumStatus(session)
    }

    private fun currentPlanIsRenewableType(premiumStatus: PremiumStatus): Boolean =
        when (premiumStatus.autoRenewTrigger) {
            
            
            
            
            AutoRenewInfo.MANUAL,
            AutoRenewInfo.AUTOMATIC -> {
                premiumStatus.isPremium
            }
            
            else -> premiumStatus.willAutoRenew()
        }
}