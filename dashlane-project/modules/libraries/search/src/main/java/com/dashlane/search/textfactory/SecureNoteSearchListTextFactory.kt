package com.dashlane.search.textfactory

import android.content.Context
import com.dashlane.search.R
import com.dashlane.search.SearchField
import com.dashlane.search.fields.SecureNoteField
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.list.StatusText
import com.dashlane.vault.textfactory.list.toStatusText

class SecureNoteSearchListTextFactory(
    private val context: Context,
    private val item: SummaryObject.SecureNote
) : DataIdentifierSearchListTextFactory {

    override fun getLine2FromField(field: SearchField<*>): StatusText? {
        if (field != SecureNoteField.ITEM_TYPE_NAME || item.secured == true) return null
        return context.getString(R.string.securenote).toStatusText()
    }
}