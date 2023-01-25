package com.dashlane.core.premium

import com.dashlane.core.premium.AutoRenewInfo.Companion.MONTHLY
import com.dashlane.core.premium.AutoRenewInfo.Companion.OTHER
import com.dashlane.core.premium.AutoRenewInfo.Companion.YEARLY
import com.dashlane.premium.offer.common.FormattedPremiumStatusManager
import com.dashlane.premium.offer.common.model.UserBenefitStatus
import com.dashlane.premium.offer.common.model.UserBenefitStatus.RenewPeriodicity
import com.dashlane.premium.offer.common.model.UserBenefitStatus.Type
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.AccountStatusRepository
import javax.inject.Inject

class FormattedPremiumStatusImpl @Inject constructor(
    val sessionManager: SessionManager,
    val accountStatusRepository: AccountStatusRepository
) : FormattedPremiumStatusManager {
    override fun getFormattedStatus(): UserBenefitStatus {
        val premiumStatus = getPremiumStatus() ?: return unknown
        val type = getUserBenefitsStatusType(premiumStatus)
        val periodicity = getUserBenefitsPeriodicity(premiumStatus)
        return UserBenefitStatus(type, periodicity)
    }

    private fun getPremiumStatus(): PremiumStatus? {
        val session = sessionManager.session ?: return null
        return accountStatusRepository.getPremiumStatus(session)
    }

    private fun getUserBenefitsStatusType(premiumStatus: PremiumStatus): Type {
        val isFamilyUser = premiumStatus.isFamilyUser
        val isEssentials = premiumStatus.premiumPlan.isEssentials
        val isAdvanced = premiumStatus.premiumPlan.isAdvanced
        val isPremium = premiumStatus.premiumPlan.isPremium
        val isPremiumPlus = premiumStatus.premiumPlan.isPremiumPlus
        val isFamilyAdmin = premiumStatus.familyMemberships?.firstOrNull() == FamilyMembership.ADMIN
        return when {
            premiumStatus.isLegacy -> Type.Legacy
            premiumStatus.isTrial -> Type.Trial
            isFamilyUser && isPremiumPlus -> Type.FamilyPlus(isFamilyAdmin)
            isPremiumPlus -> Type.PremiumPlusIndividual
            isAdvanced -> Type.AdvancedIndividual
            isEssentials -> Type.EssentialsIndividual
            isFamilyUser && isPremium -> Type.Family(isFamilyAdmin)
            isPremium -> Type.PremiumIndividual
            premiumStatus.isPremium -> Type.Unknown
            else -> Type.Free
        }
    }

    private fun getUserBenefitsPeriodicity(premiumStatus: PremiumStatus): RenewPeriodicity {
        return when (premiumStatus.autoRenewPeriodicity) {
            YEARLY -> RenewPeriodicity.YEARLY
            MONTHLY -> RenewPeriodicity.MONTHLY
            OTHER -> RenewPeriodicity.NONE
            else -> RenewPeriodicity.UNKNOWN
        }
    }

    companion object {
        private val unknown by lazy { UserBenefitStatus(Type.Unknown, RenewPeriodicity.UNKNOWN) }
    }
}