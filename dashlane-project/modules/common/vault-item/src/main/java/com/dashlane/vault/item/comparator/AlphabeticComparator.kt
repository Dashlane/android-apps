package com.dashlane.vault.item.comparator

import com.dashlane.vault.item.VaultListItemState
import com.dashlane.vault.model.loginForUi
import com.dashlane.vault.model.title
import com.dashlane.vault.model.titleForList
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.identity.IdentityNameHolderService

fun alphabeticComparator(): Comparator<VaultListItemState> =
    byTitle().then(byDescription())

fun byTitle(): Comparator<VaultListItemState> {
    return compareBy(nullsLast(String.CASE_INSENSITIVE_ORDER)) { vaultListItemState ->
        vaultListItemState.title
    }
}

private fun byDescription(): Comparator<VaultListItemState> {
    return compareBy(nullsLast(String.CASE_INSENSITIVE_ORDER)) { vaultListItemState ->
        vaultListItemState.description
    }
}

fun alphabeticComparator(identityNameHolderService: IdentityNameHolderService): Comparator<SummaryObject> =
    byTitle(identityNameHolderService).then(byLoginAuthentifiant())

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
        is SummaryObject.Authentifiant -> titleForList
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
        is SummaryObject.WiFi -> ssid
        is SummaryObject.AuthCategory -> null
        is SummaryObject.Collection -> null
        is SummaryObject.DataChangeHistory -> null
        is SummaryObject.GeneratedPassword -> null
        is SummaryObject.SecureFileInfo -> null
        is SummaryObject.SecureNoteCategory -> null
        is SummaryObject.SecurityBreach -> null
    }
}