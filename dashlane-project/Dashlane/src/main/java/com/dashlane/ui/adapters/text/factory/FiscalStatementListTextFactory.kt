package com.dashlane.ui.adapters.text.factory

import android.content.Context
import com.dashlane.R
import com.dashlane.search.SearchField
import com.dashlane.ui.adapters.text.factory.DataIdentifierListTextFactory.StatusText
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.summary.SummaryObject

class FiscalStatementListTextFactory(
    private val context: Context,
    private val item: SummaryObject.FiscalStatement
) : DataIdentifierListTextFactory {
    override fun getLine1(): StatusText {
        return StatusText(context.getString(ITEM_TYPE_NAME_ID))
    }

    override fun getLine2(default: StatusText): StatusText {
        if (item.fiscalNumber.isSemanticallyNull()) {
            return default
        }
        return StatusText(item.fiscalNumber!!)
    }

    override fun getLine2FromField(field: SearchField<*>): StatusText? = null

    companion object {
        const val ITEM_TYPE_NAME_ID = R.string.fiscal_statement
    }
}