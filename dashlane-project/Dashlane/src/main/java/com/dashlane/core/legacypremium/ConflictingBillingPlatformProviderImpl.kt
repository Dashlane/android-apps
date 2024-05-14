package com.dashlane.core.legacypremium

import com.dashlane.accountstatus.AccountStatus
import com.dashlane.accountstatus.premiumstatus.isPremium
import com.dashlane.accountstatus.premiumstatus.planType
import com.dashlane.accountstatus.subscription.autoRenewTrigger
import com.dashlane.accountstatus.subscription.willAutoRenew
import com.dashlane.premium.R
import com.dashlane.premium.offer.details.ConflictingBillingPlatformProvider
import com.dashlane.server.api.endpoints.premium.PremiumStatus
import com.dashlane.server.api.endpoints.premium.PremiumStatus.B2cStatus.PlanType
import com.dashlane.server.api.endpoints.premium.SubscriptionInfo
import com.dashlane.server.api.endpoints.premium.SubscriptionInfo.B2cSubscription.AutoRenewInfo.Trigger.AUTOMATIC
import com.dashlane.server.api.endpoints.premium.SubscriptionInfo.B2cSubscription.AutoRenewInfo.Trigger.MANUAL
import com.dashlane.ui.model.TextResource
import com.dashlane.util.inject.OptionalProvider
import javax.inject.Inject

class ConflictingBillingPlatformProviderImpl @Inject constructor(
    private val accountStatusProvider: OptionalProvider<AccountStatus>
) : ConflictingBillingPlatformProvider {

    private val premiumStatus: PremiumStatus?
        get() = accountStatusProvider.get()?.premiumStatus
    private val subscriptionInfo: SubscriptionInfo?
        get() = accountStatusProvider.get()?.subscriptionInfo

    override fun getWarning(): TextResource? {
        val premiumStatus = premiumStatus ?: return null
        val subscriptionInfo = subscriptionInfo ?: return null

        return getWarning(premiumStatus, subscriptionInfo)
    }

    private fun getWarning(premiumStatus: PremiumStatus, subscriptionInfo: SubscriptionInfo) =
        if (currentPlanIsRenewableType(premiumStatus, subscriptionInfo)) {
            when (premiumStatus.planType) {
                PlanType.PLAYSTORE,
                PlanType.PLAYSTORE_RENEWABLE -> null
                PlanType.IOS_RENEWABLE,
                PlanType.MAC_RENEWABLE,
                PlanType.IOS,
                PlanType.MAC -> {
                    TextResource.StringText(stringRes = R.string.billing_platform_conflict_warning_app_store)
                }
                PlanType.PAYPAL,
                PlanType.PAYPAL_RENEWABLE,
                PlanType.STRIPE ->
                    TextResource.StringText(stringRes = R.string.billing_platform_conflict_warning_web_Store)
                else -> TextResource.StringText(stringRes = R.string.billing_platform_conflict_warning_fallback)
            }
        } else {
            null
        }

    private fun currentPlanIsRenewableType(premiumStatus: PremiumStatus, subscriptionInfo: SubscriptionInfo): Boolean =
        when (subscriptionInfo.autoRenewTrigger) {
            
            
            
            
            MANUAL,
            AUTOMATIC -> {
                premiumStatus.isPremium
            }
            
            null -> subscriptionInfo.willAutoRenew
        }
}