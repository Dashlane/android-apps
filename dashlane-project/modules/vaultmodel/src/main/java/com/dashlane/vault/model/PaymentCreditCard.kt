package com.dashlane.vault.model

import com.dashlane.util.obfuscated.toSyncObfuscatedValue
import com.dashlane.util.time.isExpired
import com.dashlane.util.time.isExpiringSoon
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject
import java.time.Month
import java.time.Year
import java.time.YearMonth

@SuppressWarnings("kotlin:S107")
fun createPaymentCreditCard(
    dataIdentifier: CommonDataIdentifierAttrs = CommonDataIdentifierAttrsImpl(),
    name: String? = null,
    cardNumber: SyncObfuscatedValue? = null,
    cardNote: String? = null,
    owner: String? = null,
    securityCode: String? = null,
    expireMonth: Month? = null,
    expireYear: Year? = null,
    startMonth: Month? = null,
    startYear: Year? = null,
    issueNumber: String? = null,
    color: SyncObject.PaymentCreditCard.Color = SyncObject.PaymentCreditCard.Color.BLUE_1, 
    billingAddress: String? = null,
    bank: CreditCardBank? = null
): VaultItem<SyncObject.PaymentCreditCard> {
    return dataIdentifier.toVaultItem(
        SyncObject.PaymentCreditCard {
            this.name = name
            this.cardNumber = cardNumber
            this.cCNote = cardNote.toSyncObfuscatedValue()
            this.ownerName = owner
            this.securityCode = securityCode.toSyncObfuscatedValue()
            this.expireMonth = expireMonth
            this.expireYear = expireYear
            this.startMonth = startMonth
            this.startYear = startYear
            this.issueNumber = issueNumber
            this.color = color
            this.linkedBillingAddress = billingAddress
            this.bank = bank?.bankDescriptor
            this.setCommonDataIdentifierAttrs(dataIdentifier)
        })
}

val SyncObject.PaymentCreditCard.usageLogCode68Data2: Int
    get() {
        val date = expireDate
        return when {
            date?.isExpired() == true -> 0
            date?.isExpiringSoon() == true -> 1
            else -> if (date != null) 2 else 3
        }
    }

val SummaryObject.PaymentCreditCard.expireDate: YearMonth?
    get() {
        val expireYear = this.expireYear ?: return null
        val expireMonth = this.expireMonth ?: return null
        return YearMonth.of(expireYear.value, expireMonth)
    }

val SyncObject.PaymentCreditCard.expireDate: YearMonth?
    get() {
        val expireYear = this.expireYear ?: return null
        val expireMonth = this.expireMonth ?: return null
        return YearMonth.of(expireYear.value, expireMonth)
    }

var SyncObject.PaymentCreditCard.Builder.expireDate: YearMonth?
    get() {
        val expireYear = this.expireYear ?: return null
        val expireMonth = this.expireMonth ?: return null
        return YearMonth.of(expireYear.value, expireMonth)
    }
    set(value) {
        if (value == null) {
            expireYear = null
            expireMonth = null
        } else {
            expireYear = Year.of(value.year)
            expireMonth = value.month
        }
    }

val SyncObject.PaymentCreditCard.issueDate: YearMonth?
    get() {
        val issueYear = this.startYear ?: return null
        val issueMonth = this.startMonth ?: return null
        return YearMonth.of(issueYear.value, issueMonth)
    }

var SyncObject.PaymentCreditCard.Builder.issueDate: YearMonth?
    get() {
        val issueYear = this.startYear ?: return null
        val issueMonth = this.startMonth ?: return null
        return YearMonth.of(issueYear.value, issueMonth)
    }
    set(value) {
        if (value == null) {
            startYear = null
            startMonth = null
        } else {
            startYear = Year.of(value.year)
            startMonth = value.month
        }
    }

fun VaultItem<SyncObject.PaymentCreditCard>.copySyncObject(builder: SyncObject.PaymentCreditCard.Builder.() -> Unit = {}):
        VaultItem<SyncObject.PaymentCreditCard> {
    return this.copy(syncObject = this.syncObject.copy(builder))
}
