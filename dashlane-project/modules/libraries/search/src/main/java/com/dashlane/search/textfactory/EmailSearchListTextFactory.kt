package com.dashlane.search.textfactory

import com.dashlane.search.SearchField
import com.dashlane.search.fields.EmailField
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.list.StatusText
import com.dashlane.vault.textfactory.list.toStatusText

class EmailSearchListTextFactory(
    private val item: SummaryObject.Email
) : DataIdentifierSearchListTextFactory {

    override fun getLine2FromField(field: SearchField<*>): StatusText? {
        if (field !is EmailField) return null
        val text = when (field) {
            EmailField.EMAIL -> item.email
            else -> null
        }
        return text?.toStatusText()
    }
}