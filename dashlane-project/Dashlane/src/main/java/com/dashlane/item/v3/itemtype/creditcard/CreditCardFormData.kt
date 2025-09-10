package com.dashlane.item.v3.itemtype.creditcard

import com.dashlane.item.v3.data.FormData
import com.dashlane.vault.model.R
import com.dashlane.xml.domain.SyncObject

data class CreditCardFormData(
    val cardHolder: String? = null,
    val cardNumber: String? = null,
    val cardNumberFormat: String? = null,
    val cardNumberLength: Int = 0,
    val isCardNumberEmpty: Boolean = true,
    val securityCode: String? = null,
    val isSecurityCodeEmpty: Boolean = true,
    val expirationYear: Int? = null,
    val expirationMonth: Int? = null,
    val isNoteEmpty: Boolean = false,
    val note: String? = null,
    val colorId: String = SyncObject.PaymentCreditCard.Color.BLUE_1.value,
    val color: Int = R.color.credit_card_blue1,
    val isNfcAvailable: Boolean = false,
) : FormData