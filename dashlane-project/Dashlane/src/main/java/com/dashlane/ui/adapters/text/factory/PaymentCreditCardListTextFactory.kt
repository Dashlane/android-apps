package com.dashlane.ui.adapters.text.factory

import android.content.Context
import com.dashlane.R
import com.dashlane.search.fields.CreditCardField
import com.dashlane.search.SearchField
import com.dashlane.ui.adapters.text.factory.DataIdentifierListTextFactory.StatusText
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.model.expireDate
import com.dashlane.vault.summary.SummaryObject

class PaymentCreditCardListTextFactory(
    private val context: Context,
    private val item: SummaryObject.PaymentCreditCard
) : DataIdentifierListTextFactory {

    override fun getLine1(): StatusText {
        val defaultString = context.getString(ITEM_TYPE_NAME_ID)
        val name = item.name
        return StatusText(if (name.isNotSemanticallyNull()) name!! else defaultString)
    }

    override fun getLine2(default: StatusText): StatusText {
        val incomplete = item.isCardNumberEmpty || item.isSecurityCodeEmpty
        
        val number = if (incomplete) null else item.cardNumberObfuscate
        return item.expireDate?.getIdentityStatusText(context, number, default) ?: default
    }

    override fun getLine2FromField(field: SearchField<*>): StatusText? {
        if (field !is CreditCardField) return null
        val text = when (field) {
            CreditCardField.BANK -> item.bank
            CreditCardField.OWNER -> item.ownerName
            else -> null
        }
        return text?.toStatusText()
    }

    companion object {
        const val ITEM_TYPE_NAME_ID = R.string.creditcard
    }
}