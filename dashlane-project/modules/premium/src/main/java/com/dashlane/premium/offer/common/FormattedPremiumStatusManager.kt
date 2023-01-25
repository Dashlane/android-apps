package com.dashlane.premium.offer.common

import com.dashlane.premium.offer.common.model.UserBenefitStatus

interface FormattedPremiumStatusManager {

    fun getFormattedStatus(): UserBenefitStatus
}