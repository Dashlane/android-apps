package com.dashlane.vault.util

import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.util.userfeatures.UserFeaturesChecker.FeatureFlip.ATTACHMENT_ALL_ITEMS
import com.dashlane.util.userfeatures.UserFeaturesChecker.FeatureFlip.ATTACHMENT_FOR_IDS
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.SummaryObject.DriverLicence
import com.dashlane.vault.summary.SummaryObject.FiscalStatement
import com.dashlane.vault.summary.SummaryObject.IdCard
import com.dashlane.vault.summary.SummaryObject.Passport
import com.dashlane.vault.summary.SummaryObject.SocialSecurityStatement

fun SummaryObject.attachmentsAllowed(userFeaturesChecker: UserFeaturesChecker) = when {
    isShared -> false
    isIdDataType() && userFeaturesChecker.has(ATTACHMENT_FOR_IDS) -> true
    else -> this is SummaryObject.SecureNote || userFeaturesChecker.has(ATTACHMENT_ALL_ITEMS)
}

val SummaryObject.isProtected
    get() = (this as? SummaryObject.SecureNote)?.secured ?: false

fun SummaryObject.isIdDataType() = when (this) {
    is DriverLicence, is FiscalStatement, is IdCard, is Passport, is SocialSecurityStatement -> true
    else -> false
}