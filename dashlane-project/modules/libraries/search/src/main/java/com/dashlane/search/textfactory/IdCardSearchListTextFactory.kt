package com.dashlane.search.textfactory

import com.dashlane.search.SearchField
import com.dashlane.search.fields.IdCardField
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.list.StatusText
import com.dashlane.vault.textfactory.list.toStatusText

class IdCardSearchListTextFactory(
    private val item: SummaryObject.IdCard,
) : DataIdentifierSearchListTextFactory {

    override fun getLine2FromField(field: SearchField<*>): StatusText? {
        if (field !is IdCardField) return null
        val text = when (field) {
            IdCardField.FULL_NAME -> item.fullname
            else -> null
        }
        return text?.toStatusText()
    }
}