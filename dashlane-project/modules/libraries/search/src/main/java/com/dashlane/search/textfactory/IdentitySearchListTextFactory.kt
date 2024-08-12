package com.dashlane.search.textfactory

import com.dashlane.search.SearchField
import com.dashlane.search.fields.IdentityField
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.list.StatusText
import com.dashlane.vault.textfactory.list.toStatusText

class IdentitySearchListTextFactory(
    private val item: SummaryObject.Identity
) : DataIdentifierSearchListTextFactory {

    override fun getLine2FromField(field: SearchField<*>): StatusText? {
        if (field !is IdentityField) return null
        val text = when (field) {
            IdentityField.MIDDLE_NAME -> item.middleName
            IdentityField.LAST_NAME -> item.lastName
            IdentityField.PSEUDO -> item.pseudo
            else -> null
        }
        return text?.toStatusText()
    }
}