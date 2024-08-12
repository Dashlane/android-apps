package com.dashlane.search.textfactory

import com.dashlane.search.SearchField
import com.dashlane.search.fields.CreditCardField
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.list.StatusText
import com.dashlane.vault.textfactory.list.toStatusText

class PaymentCreditCardSearchListTextFactory(
    private val item: SummaryObject.PaymentCreditCard
) : DataIdentifierSearchListTextFactory {

    override fun getLine2FromField(field: SearchField<*>): StatusText? {
        if (field !is CreditCardField) return null
        val text = when (field) {
            CreditCardField.BANK -> item.bank
            CreditCardField.OWNER -> item.ownerName
            else -> null
        }
        return text?.toStatusText()
    }
}