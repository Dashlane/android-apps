package com.dashlane.home.vaultlist.comparator

import com.dashlane.feature.home.data.Filter
import com.dashlane.vault.model.loginForUi
import com.dashlane.vault.model.title
import com.dashlane.vault.model.titleForListNormalized
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.identity.IdentityNameHolderService

fun alphabeticComparator(
    filter: Filter,
    identityNameHolderService: IdentityNameHolderService
): Comparator<SummaryObject> =
    when (filter) {
        Filter.FILTER_PASSWORD -> byTitle(identityNameHolderService).then(byLoginAuthentifiant())
        Filter.FILTER_SECURE_NOTE,
        Filter.ALL_VISIBLE_VAULT_ITEM_TYPES -> byTitle(identityNameHolderService)
        else -> compareBy { 0 }
    }

private fun byTitle(identityNameHolderService: IdentityNameHolderService): Comparator<SummaryObject> {
    return compareBy(nullsLast(String.CASE_INSENSITIVE_ORDER)) { summaryObject ->
        summaryObject.getComparableField(identityNameHolderService)
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
        is SummaryObject.Secret -> title
        else -> null
    }
}