package com.dashlane.premium.enddate

import com.dashlane.accountstatus.AccountStatus
import com.dashlane.accountstatus.premiumstatus.hasLifetimeEntitlement
import com.dashlane.server.api.endpoints.premium.PremiumStatus.B2cStatus.StatusCode
import com.dashlane.server.api.endpoints.premium.SubscriptionInfo
import com.dashlane.server.api.endpoints.premium.SubscriptionInfo.B2cSubscription.AutoRenewInfo.Periodicity
import com.dashlane.server.api.time.toInstant
import java.time.Clock
import java.time.Instant

interface FormattedEndDateProvider {
    val isLegacy: Boolean
    val isTrial: Boolean
    val endDate: Instant?
    fun getFormattedAutoRenewTrigger(): FormattedEndDate.Trigger?
    fun getFormattedAutoRenewPeriodicity(): FormattedEndDate.Periodicity
    fun willAutoRenew(): Boolean
    fun hasLifetimeEntitlement(): Boolean
}

class FormattedEndDateProviderImpl(
    private val accountStatus: AccountStatus,
    private val clock: Clock
) : FormattedEndDateProvider {
    private val b2cStatus = accountStatus.premiumStatus.b2cStatus
    private val b2cSubscription = accountStatus.subscriptionInfo.b2cSubscription

    override val isLegacy: Boolean
        get() = b2cStatus.statusCode == StatusCode.LEGACY
    override val isTrial: Boolean
        get() = b2cStatus.isTrial
    override val endDate: Instant?
        get() = b2cStatus.endDateUnix?.toInstant()

    override fun willAutoRenew(): Boolean {
        return b2cStatus.autoRenewal
    }

    override fun hasLifetimeEntitlement() = accountStatus.premiumStatus.hasLifetimeEntitlement(clock)

    override fun getFormattedAutoRenewPeriodicity(): FormattedEndDate.Periodicity = when (b2cSubscription.autoRenewInfo.periodicity) {
        Periodicity.MONTHLY -> FormattedEndDate.Periodicity.MONTHLY
        Periodicity.YEARLY -> FormattedEndDate.Periodicity.YEARLY
        else -> FormattedEndDate.Periodicity.UNDEFINED
    }

    override fun getFormattedAutoRenewTrigger(): FormattedEndDate.Trigger? = when (b2cSubscription.autoRenewInfo.trigger) {
        SubscriptionInfo.B2cSubscription.AutoRenewInfo.Trigger.AUTOMATIC -> FormattedEndDate.Trigger.AUTOMATIC
        SubscriptionInfo.B2cSubscription.AutoRenewInfo.Trigger.MANUAL -> FormattedEndDate.Trigger.MANUAL
        else -> null
    }
}
