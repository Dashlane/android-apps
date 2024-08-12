package com.dashlane.search.textfactory

import com.dashlane.search.SearchField
import com.dashlane.search.fields.BankStatementField
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.list.StatusText
import com.dashlane.vault.textfactory.list.toStatusText

class BankStatementSearchListTextFactory(
    private val item: SummaryObject.BankStatement
) : DataIdentifierSearchListTextFactory {

    override fun getLine2FromField(field: SearchField<*>): StatusText? {
        if (field !is BankStatementField) return null
        val text = when (field) {
            BankStatementField.BANK -> item.bankAccountBank
            BankStatementField.OWNER -> item.bankAccountOwner
            else -> null
        }
        return text?.toStatusText()
    }
}