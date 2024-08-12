package com.dashlane.search.textfactory

import com.dashlane.search.SearchField
import com.dashlane.search.fields.PasskeyField
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.list.StatusText
import com.dashlane.vault.textfactory.list.toStatusText

class PasskeySearchListTextFactory(
    private val item: SummaryObject.Passkey
) : DataIdentifierSearchListTextFactory {
    override fun getLine2FromField(field: SearchField<*>): StatusText? {
        if (field !is PasskeyField) return null
        val text = when (field) {
            PasskeyField.USERNAME -> item.userDisplayName
            PasskeyField.WEBSITE -> item.rpId
            PasskeyField.TITLE -> item.itemName
            PasskeyField.NOTE -> item.note
            else -> null
        }
        return text?.toStatusText()
    }
}