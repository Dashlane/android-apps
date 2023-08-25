package com.dashlane.vault.util

import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.vault.model.loginForUi
import com.dashlane.vault.model.titleForListNormalized
import com.dashlane.vault.summary.SummaryObject

fun comparatorAlphabeticAuthentifiant(): Comparator<SummaryObject.Authentifiant> = byTitle().then(byLogin())

fun comparatorAlphabeticSecureNote(): Comparator<SummaryObject.SecureNote> =
    compareBy(nullsLast(String.CASE_INSENSITIVE_ORDER)) {
        it.title
    }

fun comparatorAlphabeticAllVisibleItems(): Comparator<SummaryObject> =
    comparatorAlphabeticSummaryObject()
        .then(byLoginAuthentifiant())

private fun byTitle(): Comparator<SummaryObject.Authentifiant> {
    return compareBy(nullsLast(String.CASE_INSENSITIVE_ORDER)) {
        it.titleForListNormalized
    }
}

private fun byLogin(): Comparator<SummaryObject.Authentifiant> {
    return compareBy(nullsLast(String.CASE_INSENSITIVE_ORDER)) {
        it.loginForUi
    }
}

private fun byLoginAuthentifiant(): Comparator<SummaryObject> {
    return compareBy(nullsLast(String.CASE_INSENSITIVE_ORDER)) {
        if (it is SummaryObject.Authentifiant) {
            it.loginForUi
        } else {
            null
        }
    }
}

private fun comparatorAlphabeticSummaryObject(): Comparator<SummaryObject> {
    return compareBy(nullsLast(String.CASE_INSENSITIVE_ORDER)) {
        it.getComparableField()
    }
}

fun SummaryObject.getComparableField(): String? {
    val identityUtil = IdentityUtil(SingletonProvider.getMainDataAccessor())
    return when (this) {
        is SummaryObject.Address -> addressName
        is SummaryObject.Authentifiant -> titleForListNormalized
        is SummaryObject.Company -> name
        is SummaryObject.DriverLicence -> identityUtil.getOwner(this)
        is SummaryObject.Email -> emailName
        is SummaryObject.FiscalStatement -> fiscalNumber
        is SummaryObject.IdCard -> identityUtil.getOwner(this)
        is SummaryObject.Identity -> fullName
        is SummaryObject.Passport -> identityUtil.getOwner(this)
        is SummaryObject.PaymentPaypal -> name
        is SummaryObject.PaymentCreditCard -> ownerName
        is SummaryObject.PersonalWebsite -> name
        is SummaryObject.Phone -> phoneName
        is SummaryObject.SocialSecurityStatement -> identityUtil.getOwner(this)
        is SummaryObject.SecureNote -> title
        is SummaryObject.BankStatement -> bankAccountName
        else -> null
    }
}