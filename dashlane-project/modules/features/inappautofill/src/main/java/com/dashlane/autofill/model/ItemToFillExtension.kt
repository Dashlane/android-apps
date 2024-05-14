package com.dashlane.autofill.model

import com.dashlane.hermes.generated.definitions.MatchType
import com.dashlane.util.model.UserPermission
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.loginForUi
import com.dashlane.vault.model.titleForListNormalized
import com.dashlane.vault.model.urlForUI
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject

fun SummaryObject.Authentifiant.toItemToFill(
    matchType: MatchType? = null,
    oldPassword: SyncObfuscatedValue? = null
) = AuthentifiantItemToFill(
    itemId = id,
    matchType = matchType,
    lastUsedDate = locallyViewedDate,
    title = titleForListNormalized,
    login = loginForUi,
    url = urlForUI() ?: "",
    oldPassword = oldPassword
)

fun VaultItem<SyncObject.Authentifiant>.toItemToFill(
    matchType: MatchType? = null,
    oldPassword: SyncObfuscatedValue? = null
) = toSummary<SummaryObject.Authentifiant>().toItemToFill(matchType, oldPassword).also {
    it.syncObject = syncObject
    it.isSharedWithLimitedRight = sharingPermission != null && sharingPermission != UserPermission.ADMIN
}

fun SummaryObject.PaymentCreditCard.toItemToFill(
    matchType: MatchType? = null,
    zipCode: String?
) = CreditCardItemToFill(
    itemId = id,
    matchType = matchType,
    lastUsedDate = locallyViewedDate,
    name = name,
    cardTypeName = creditCardTypeName,
    cardNumberObfuscate = cardNumberObfuscate?.let { smallerCardNumber(it) } ?: "",
    zipCode = zipCode,
    color = color
)

fun smallerCardNumber(cardNumber: String): String {
    val startCount = cardNumber.count { it == '*' }
    return if (startCount > 4) {
        cardNumber.substring(startCount - 4)
    } else {
        cardNumber
    }
}

fun VaultItem<SyncObject.PaymentCreditCard>.toItemToFill(
    matchType: MatchType? = null,
    zipCode: String?
) = toSummary<SummaryObject.PaymentCreditCard>().toItemToFill(matchType, zipCode).also {
    it.syncObject = this.syncObject
}

fun SummaryObject.Email.toItemToFill(
    matchType: MatchType? = null
) = EmailItemToFill(id, matchType, locallyViewedDate, emailName, email)