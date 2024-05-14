package com.dashlane.vault.model

import com.dashlane.xml.domain.SyncObject

fun SyncObject.PaymentCreditCard.Color?.getColorResource(): Int {
    return when (this) {
        SyncObject.PaymentCreditCard.Color.BLUE_1 -> R.color.credit_card_blue1
        SyncObject.PaymentCreditCard.Color.BLUE_2 -> R.color.credit_card_blue2
        SyncObject.PaymentCreditCard.Color.RED -> R.color.credit_card_red
        SyncObject.PaymentCreditCard.Color.GOLD -> R.color.credit_card_gold
        SyncObject.PaymentCreditCard.Color.GREEN_1 -> R.color.credit_card_green1
        SyncObject.PaymentCreditCard.Color.WHITE -> R.color.credit_card_white
        SyncObject.PaymentCreditCard.Color.GREEN_2 -> R.color.credit_card_green2
        SyncObject.PaymentCreditCard.Color.SILVER -> R.color.credit_card_silver
        SyncObject.PaymentCreditCard.Color.BLACK -> R.color.credit_card_black
        SyncObject.PaymentCreditCard.Color.ORANGE -> R.color.credit_card_orange
        else -> R.color.credit_card_blue1
    }
}