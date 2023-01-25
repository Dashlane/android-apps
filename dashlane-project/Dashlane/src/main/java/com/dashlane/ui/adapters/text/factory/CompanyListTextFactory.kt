package com.dashlane.ui.adapters.text.factory

import android.content.Context
import com.dashlane.R
import com.dashlane.search.fields.CompanyField
import com.dashlane.search.SearchField
import com.dashlane.ui.adapters.text.factory.DataIdentifierListTextFactory.StatusText
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.summary.SummaryObject

class CompanyListTextFactory(
    private val context: Context,
    private val item: SummaryObject.Company
) : DataIdentifierListTextFactory {

    override fun getLine1(): StatusText {
        return StatusText(if (item.name.isSemanticallyNull()) context.getString(ITEM_TYPE_NAME_ID) else item.name!!)
    }

    override fun getLine2(default: StatusText): StatusText {
        if (item.name.isSemanticallyNull() || item.jobTitle.isSemanticallyNull()) {
            return default
        }
        return StatusText(item.jobTitle!!)
    }

    override fun getLine2FromField(field: SearchField<*>): StatusText? {
        if (field !is CompanyField) return null
        val text = when (field) {
            CompanyField.JOB_TITLE -> item.jobTitle
            else -> null
        }
        return text?.toStatusText()
    }

    companion object {
        const val ITEM_TYPE_NAME_ID = R.string.company
    }
}