package com.dashlane.premium.offer.common

import com.dashlane.accountstatus.AccountStatus
import com.dashlane.premium.offer.common.model.UserBenefitStatus

interface UserBenefitStatusProvider {

    fun getFormattedStatus(accountStatus: AccountStatus?): UserBenefitStatus
}