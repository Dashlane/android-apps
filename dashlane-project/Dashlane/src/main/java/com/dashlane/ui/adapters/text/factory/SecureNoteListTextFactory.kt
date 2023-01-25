package com.dashlane.ui.adapters.text.factory

import android.content.Context
import com.dashlane.R
import com.dashlane.search.SearchField
import com.dashlane.search.fields.SecureNoteField
import com.dashlane.ui.adapters.text.factory.DataIdentifierListTextFactory.StatusText
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.summary.SummaryObject

class SecureNoteListTextFactory(
    private val context: Context,
    private val item: SummaryObject.SecureNote
) : DataIdentifierListTextFactory {

    override fun getLine1(): StatusText {
        val title = item.title
        val incomplete = context.getString(ITEM_TYPE_NAME_ID)
        return StatusText(if (title.isNotSemanticallyNull()) title!! else incomplete)
    }

    override fun getLine2(default: StatusText): StatusText {
        return StatusText(
            if (item.secured == true) context.getString(R.string.secure_note_is_locked) else item
                .content ?: ""
        )
    }

    override fun getLine2FromField(field: SearchField<*>): StatusText? {
        if (field !is SecureNoteField || item.secured == true) return null
        val text = when (field) {
            SecureNoteField.ITEM_TYPE_NAME -> context.getString(ITEM_TYPE_NAME_ID)
            SecureNoteField.CONTENT -> item.content
            else -> null
        }
        return text?.toStatusText()
    }

    companion object {
        const val ITEM_TYPE_NAME_ID = R.string.securenote
    }
}