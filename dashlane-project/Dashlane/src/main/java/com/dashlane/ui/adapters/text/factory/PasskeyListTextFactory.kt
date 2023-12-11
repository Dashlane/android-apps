package com.dashlane.ui.adapters.text.factory

import com.dashlane.search.SearchField
import com.dashlane.search.fields.PasskeyField
import com.dashlane.vault.model.title
import com.dashlane.vault.summary.SummaryObject

class PasskeyListTextFactory(
    private val item: SummaryObject.Passkey
) : DataIdentifierListTextFactory {
    override fun getLine1(): DataIdentifierListTextFactory.StatusText {
        return DataIdentifierListTextFactory.StatusText(item.title.orEmpty())
    }

    override fun getLine2(default: DataIdentifierListTextFactory.StatusText): DataIdentifierListTextFactory.StatusText {
        return DataIdentifierListTextFactory.StatusText(item.userDisplayName.orEmpty())
    }

    override fun getLine2FromField(field: SearchField<*>): DataIdentifierListTextFactory.StatusText? {
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