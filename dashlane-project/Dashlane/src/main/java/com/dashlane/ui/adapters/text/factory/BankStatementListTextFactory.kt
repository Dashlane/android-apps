package com.dashlane.ui.adapters.text.factory

import android.content.Context
import com.dashlane.R
import com.dashlane.search.fields.BankStatementField
import com.dashlane.search.SearchField
import com.dashlane.ui.adapters.text.factory.DataIdentifierListTextFactory.StatusText
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.summary.SummaryObject

class BankStatementListTextFactory(
    private val context: Context,
    private val item: SummaryObject.BankStatement
) : DataIdentifierListTextFactory {

    override fun getLine1(): StatusText {
        val defaultString = context.getString(ITEM_TYPE_NAME_ID)
        val name = item.bankAccountName
        return StatusText(if (name.isNotSemanticallyNull()) name!! else defaultString)
    }

    override fun getLine2(default: StatusText): StatusText {
        if (item.bankAccountOwner.isSemanticallyNull() ||
            item.isIBANEmpty ||
            item.isBICEmpty
        ) {
            return default
        }
        return StatusText(item.bankAccountOwner!!)
    }

    override fun getLine2FromField(field: SearchField<*>): StatusText? {
        if (field !is BankStatementField) return null
        val text = when (field) {
            BankStatementField.BANK -> item.bankAccountBank
            BankStatementField.OWNER -> item.bankAccountOwner
            else -> null
        }
        return text?.toStatusText()
    }

    companion object {
        const val ITEM_TYPE_NAME_ID = R.string.bank_statement
    }
}