package com.dashlane.premium.current.other

import com.dashlane.premium.offer.common.model.UserBenefitStatus

interface CurrentPlanStatusProvider {
    fun isB2bUser(): Boolean
    fun getAccountStatus(): UserBenefitStatus.Type
    fun hasLifeTimeEntitlement(): Boolean
    fun isVpnDeniedDueToNoPayment(): Boolean
}