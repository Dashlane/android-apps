package com.dashlane.vault.textfactory.list

import android.content.res.Resources
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.R
import javax.inject.Inject

class FiscalStatementListTextFactory @Inject constructor(
    private val resources: Resources,
) : DataIdentifierListTextFactory<SummaryObject.FiscalStatement> {
    override fun getTitle(item: SummaryObject.FiscalStatement): StatusText {
        return StatusText(resources.getString(R.string.fiscal_statement))
    }

    override fun getDescription(item: SummaryObject.FiscalStatement, default: StatusText): StatusText {
        if (item.fiscalNumber.isSemanticallyNull()) {
            return default
        }
        return StatusText(item.fiscalNumber!!)
    }
}