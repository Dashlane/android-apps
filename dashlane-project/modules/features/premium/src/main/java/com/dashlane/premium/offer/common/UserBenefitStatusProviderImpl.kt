package com.dashlane.premium.offer.common

import com.dashlane.accountstatus.AccountStatus
import com.dashlane.accountstatus.premiumstatus.isAdvancedPlan
import com.dashlane.accountstatus.premiumstatus.isFamilyAdmin
import com.dashlane.accountstatus.premiumstatus.isFamilyPlan
import com.dashlane.accountstatus.premiumstatus.isLegacy
import com.dashlane.accountstatus.premiumstatus.isPremium
import com.dashlane.accountstatus.premiumstatus.isPremiumPlusPlan
import com.dashlane.accountstatus.premiumstatus.isTrial
import com.dashlane.accountstatus.subscription.autoRenewPeriodicity
import com.dashlane.premium.offer.common.model.UserBenefitStatus
import com.dashlane.premium.offer.common.model.UserBenefitStatus.RenewPeriodicity
import com.dashlane.premium.offer.common.model.UserBenefitStatus.Type
import com.dashlane.server.api.endpoints.premium.PremiumStatus
import com.dashlane.server.api.endpoints.premium.SubscriptionInfo
import com.dashlane.server.api.endpoints.premium.SubscriptionInfo.B2cSubscription.AutoRenewInfo.Periodicity.MONTHLY
import com.dashlane.server.api.endpoints.premium.SubscriptionInfo.B2cSubscription.AutoRenewInfo.Periodicity.OTHER
import com.dashlane.server.api.endpoints.premium.SubscriptionInfo.B2cSubscription.AutoRenewInfo.Periodicity.YEARLY
import javax.inject.Inject

class UserBenefitStatusProviderImpl @Inject constructor() : UserBenefitStatusProvider {

    override fun getFormattedStatus(accountStatus: AccountStatus?): UserBenefitStatus {
        accountStatus ?: return UNKNOWN

        val type = getUserBenefitsStatusType(accountStatus.premiumStatus)
        val periodicity = getUserBenefitsPeriodicity(accountStatus.subscriptionInfo)
        return UserBenefitStatus(type, periodicity)
    }

    private fun getUserBenefitsStatusType(premiumStatus: PremiumStatus): Type {
        val isAdvanced = premiumStatus.isAdvancedPlan
        val isPremium = premiumStatus.isPremium
        val isPremiumPlus = premiumStatus.isPremiumPlusPlan
        val isFamilyUser = premiumStatus.isFamilyPlan
        val isFamilyAdmin = premiumStatus.isFamilyAdmin
        return when {
            premiumStatus.isLegacy -> Type.Legacy
            premiumStatus.isTrial -> Type.Trial
            isFamilyUser && isPremiumPlus -> Type.FamilyPlus(isFamilyAdmin)
            isPremiumPlus -> Type.PremiumPlusIndividual
            isAdvanced -> Type.AdvancedIndividual

            isFamilyUser && isPremium -> Type.Family(isFamilyAdmin)
            isPremium -> Type.PremiumIndividual
            premiumStatus.isPremium -> Type.Unknown
            else -> Type.Free
        }
    }

    private fun getUserBenefitsPeriodicity(subscriptionInfo: SubscriptionInfo): RenewPeriodicity {
        return when (subscriptionInfo.autoRenewPeriodicity) {
            YEARLY -> RenewPeriodicity.YEARLY
            MONTHLY -> RenewPeriodicity.MONTHLY
            OTHER -> RenewPeriodicity.NONE
            else -> RenewPeriodicity.UNKNOWN
        }
    }

    companion object {
        private val UNKNOWN by lazy { UserBenefitStatus(Type.Unknown, RenewPeriodicity.UNKNOWN) }
    }
}