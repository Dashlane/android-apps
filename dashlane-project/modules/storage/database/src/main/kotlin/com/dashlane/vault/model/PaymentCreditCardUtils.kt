package com.dashlane.vault.model

import com.dashlane.xml.domain.SyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject

val SyncObject.PaymentCreditCard.cardNumberLastFourDigits: String?
    get() = getLastDigits(this.cardNumber)

private fun getLastDigits(cardNumber: SyncObfuscatedValue?): String? =
    cardNumber
        ?.takeIf { it.length > 4 }
        ?.takeIf { it.isNotSemanticallyNull() }
        ?.takeLast(4)
        ?.toString()

val SyncObject.PaymentCreditCard.cardNumberObfuscate: String
    get() = cardNumber
        ?.let {
            val length = it.length
            if (length > 4) {
                String(CharArray(length - 4) { '*' }) + it.substring(length - 4)
            } else {
                null
            }
        }
        ?: cardNumber?.toString()
        ?: ""