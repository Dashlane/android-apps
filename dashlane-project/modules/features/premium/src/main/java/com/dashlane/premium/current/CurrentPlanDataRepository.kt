package com.dashlane.premium.current

import com.dashlane.premium.current.model.CurrentPlanType
import com.dashlane.premium.current.model.CurrentPlanType.ADVANCED
import com.dashlane.premium.current.model.CurrentPlanType.B2B
import com.dashlane.premium.current.model.CurrentPlanType.ESSENTIALS
import com.dashlane.premium.current.model.CurrentPlanType.FAMILY_ADMIN
import com.dashlane.premium.current.model.CurrentPlanType.FAMILY_INVITEE
import com.dashlane.premium.current.model.CurrentPlanType.FAMILY_PLUS_ADMIN
import com.dashlane.premium.current.model.CurrentPlanType.FAMILY_PLUS_INVITEE
import com.dashlane.premium.current.model.CurrentPlanType.FREE
import com.dashlane.premium.current.model.CurrentPlanType.LEGACY
import com.dashlane.premium.current.model.CurrentPlanType.PREMIUM
import com.dashlane.premium.current.model.CurrentPlanType.PREMIUM_FREE_FOR_LIFE
import com.dashlane.premium.current.model.CurrentPlanType.PREMIUM_FREE_OF_CHARGE
import com.dashlane.premium.current.model.CurrentPlanType.PREMIUM_PLUS
import com.dashlane.premium.current.model.CurrentPlanType.TRIAL
import com.dashlane.premium.current.model.CurrentPlanType.UNKNOWN
import com.dashlane.premium.current.other.CurrentBenefitsBuilder
import com.dashlane.premium.current.other.CurrentPlanStatusProvider
import com.dashlane.premium.offer.common.model.UserBenefitStatus.Type.AdvancedIndividual
import com.dashlane.premium.offer.common.model.UserBenefitStatus.Type.EssentialsIndividual
import com.dashlane.premium.offer.common.model.UserBenefitStatus.Type.Family
import com.dashlane.premium.offer.common.model.UserBenefitStatus.Type.FamilyPlus
import com.dashlane.premium.offer.common.model.UserBenefitStatus.Type.Free
import com.dashlane.premium.offer.common.model.UserBenefitStatus.Type.Legacy
import com.dashlane.premium.offer.common.model.UserBenefitStatus.Type.PremiumIndividual
import com.dashlane.premium.offer.common.model.UserBenefitStatus.Type.PremiumPlusIndividual
import com.dashlane.premium.offer.common.model.UserBenefitStatus.Type.Trial
import com.dashlane.premium.offer.common.model.UserBenefitStatus.Type.Unknown
import com.dashlane.userfeatures.UserFeaturesChecker
import com.dashlane.userfeatures.canShowVpn
import javax.inject.Inject

internal class CurrentPlanDataRepository @Inject constructor(
    private val userFeaturesChecker: UserFeaturesChecker,
    private val statusProvider: CurrentPlanStatusProvider
) {

    fun isVpnAllowed() = userFeaturesChecker.canShowVpn()

    fun getBenefits() = CurrentBenefitsBuilder(userFeaturesChecker).build(getType().isFamily())

    fun getType(): CurrentPlanType {
        if (statusProvider.isB2bUser()) {
            return B2B
        }
        return when (val status = statusProvider.getAccountStatus()) {
            Legacy -> LEGACY
            Free -> FREE
            Trial -> TRIAL
            AdvancedIndividual -> ADVANCED
            EssentialsIndividual -> ESSENTIALS
            PremiumIndividual -> {
                when {
                    statusProvider.hasLifeTimeEntitlement() -> PREMIUM_FREE_FOR_LIFE
                    statusProvider.isVpnDeniedDueToNoPayment() -> PREMIUM_FREE_OF_CHARGE
                    else -> PREMIUM
                }
            }
            PremiumPlusIndividual -> PREMIUM_PLUS
            is Family -> FAMILY_ADMIN.takeIf { status.isAdmin } ?: FAMILY_INVITEE
            is FamilyPlus -> FAMILY_PLUS_ADMIN.takeIf { status.isAdmin } ?: FAMILY_PLUS_INVITEE
            Unknown -> UNKNOWN
        }
    }

    private fun CurrentPlanType.isFamily() =
        this == FAMILY_INVITEE || this == FAMILY_ADMIN
}