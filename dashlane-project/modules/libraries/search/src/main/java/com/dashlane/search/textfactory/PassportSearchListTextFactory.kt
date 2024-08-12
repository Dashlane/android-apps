package com.dashlane.search.textfactory

import com.dashlane.search.SearchField
import com.dashlane.search.fields.PassportField
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.list.StatusText
import com.dashlane.vault.textfactory.list.toStatusText

class PassportSearchListTextFactory(
    private val item: SummaryObject.Passport,
) : DataIdentifierSearchListTextFactory {

    override fun getLine2FromField(field: SearchField<*>): StatusText? {
        if (field !is PassportField) return null
        val text = when (field) {
            PassportField.FULL_NAME -> item.fullname
            else -> null
        }
        return text?.toStatusText()
    }
}