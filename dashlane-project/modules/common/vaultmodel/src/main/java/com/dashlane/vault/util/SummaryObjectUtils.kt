package com.dashlane.vault.util

import com.dashlane.userfeatures.FeatureFlip.ATTACHMENT_ALL_ITEMS
import com.dashlane.userfeatures.UserFeaturesChecker
import com.dashlane.vault.summary.SummaryObject

fun SummaryObject.attachmentsAllowed(userFeaturesChecker: UserFeaturesChecker) = when {
    isShared -> false
    else -> this is SummaryObject.SecureNote || userFeaturesChecker.has(ATTACHMENT_ALL_ITEMS)
}

val SummaryObject.isProtected
    get() = (this as? SummaryObject.SecureNote)?.secured ?: false