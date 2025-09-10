package com.dashlane.vault.util

import com.dashlane.vault.model.loginForUi
import com.dashlane.vault.model.title
import com.dashlane.vault.model.titleForListNormalized
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.identity.IdentityNameHolderService

fun comparatorAlphabeticAuthentifiant(identityNameHolderService: IdentityNameHolderService): Comparator<SummaryObject> =
    byTitle(identityNameHolderService).then(byLoginAuthentifiant())

fun comparatorAlphabeticSecureNote(): Comparator<SummaryObject.SecureNote> =
    compareBy(nullsLast(String.CASE_INSENSITIVE_ORDER)) {
        it.title
    }

private fun byTitle(identityNameHolderService: IdentityNameHolderService): Comparator<SummaryObject> {
    return compareBy(nullsLast(String.CASE_INSENSITIVE_ORDER)) {
        it.getComparableField(identityNameHolderService)
    }
}

private fun byLoginAuthentifiant(): Comparator<SummaryObject> {
    return compareBy(nullsLast(String.CASE_INSENSITIVE_ORDER)) {
        when (it) {
            is SummaryObject.Authentifiant -> it.loginForUi
            is SummaryObject.Passkey -> it.userDisplayName
            else -> null
        }
    }
}

fun SummaryObject.getComparableField(identityNameHolderService: IdentityNameHolderService): String? {
    return when (this) {
        is SummaryObject.Address -> addressName
        is SummaryObject.Authentifiant -> titleForListNormalized
        is SummaryObject.Company -> name
        is SummaryObject.DriverLicence -> identityNameHolderService.getOwner(this)
        is SummaryObject.Email -> emailName
        is SummaryObject.FiscalStatement -> fiscalNumber
        is SummaryObject.IdCard -> identityNameHolderService.getOwner(this)
        is SummaryObject.Identity -> fullName
        is SummaryObject.Passport -> identityNameHolderService.getOwner(this)
        is SummaryObject.PaymentCreditCard -> ownerName
        is SummaryObject.PersonalWebsite -> name
        is SummaryObject.Phone -> phoneName
        is SummaryObject.SocialSecurityStatement -> identityNameHolderService.getOwner(this)
        is SummaryObject.SecureNote -> title
        is SummaryObject.BankStatement -> bankAccountName
        is SummaryObject.Passkey -> title
        is SummaryObject.WiFi -> ssid
        is SummaryObject.AuthCategory -> null
        is SummaryObject.Collection -> null
        is SummaryObject.DataChangeHistory -> null
        is SummaryObject.GeneratedPassword -> null
        is SummaryObject.Secret -> null
        is SummaryObject.SecureFileInfo -> null
        is SummaryObject.SecureNoteCategory -> null
        is SummaryObject.SecurityBreach -> null
    }
}