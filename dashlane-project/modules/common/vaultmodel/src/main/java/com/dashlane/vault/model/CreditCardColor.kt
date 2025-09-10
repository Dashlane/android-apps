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

fun SyncObject.PaymentCreditCard.Color.getColorName(): Int =
    when (this) {
        SyncObject.PaymentCreditCard.Color.NO_TYPE -> R.string.creditcard_color_no_type
        SyncObject.PaymentCreditCard.Color.RED -> R.string.creditcard_color_red
        SyncObject.PaymentCreditCard.Color.GOLD -> R.string.creditcard_color_gold
        SyncObject.PaymentCreditCard.Color.GREEN_1 -> R.string.creditcard_color_green
        SyncObject.PaymentCreditCard.Color.WHITE -> R.string.creditcard_color_white
        SyncObject.PaymentCreditCard.Color.GREEN_2 -> R.string.creditcard_color_amex_green
        SyncObject.PaymentCreditCard.Color.SILVER -> R.string.creditcard_color_silver
        SyncObject.PaymentCreditCard.Color.BLACK -> R.string.creditcard_color_black
        SyncObject.PaymentCreditCard.Color.BLUE_1 -> R.string.creditcard_color_blue
        SyncObject.PaymentCreditCard.Color.BLUE_2 -> R.string.creditcard_color_dark_blue
        SyncObject.PaymentCreditCard.Color.ORANGE -> R.string.creditcard_color_orange
    }