package com.dashlane.vault.util

import com.dashlane.featureflipping.FeatureFlip.ATTACHMENT_ALL_ITEMS
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.vault.summary.SummaryObject

fun SummaryObject.attachmentsAllowed(userFeaturesChecker: UserFeaturesChecker, isAccountFrozen: Boolean = false) = when {
    isShared -> false
    else -> (this is SummaryObject.SecureNote || userFeaturesChecker.has(ATTACHMENT_ALL_ITEMS)) && !isAccountFrozen
}
