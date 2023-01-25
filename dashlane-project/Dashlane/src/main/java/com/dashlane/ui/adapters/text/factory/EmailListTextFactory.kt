package com.dashlane.ui.adapters.text.factory

import android.content.Context
import com.dashlane.R
import com.dashlane.search.fields.EmailField
import com.dashlane.search.SearchField
import com.dashlane.ui.adapters.text.factory.DataIdentifierListTextFactory.StatusText
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.summary.SummaryObject

class EmailListTextFactory(
    private val context: Context,
    private val item: SummaryObject.Email
) : DataIdentifierListTextFactory {

    override fun getLine1(): StatusText {
        return StatusText(if (item.emailName.isSemanticallyNull()) context.getString(ITEM_TYPE_NAME_ID) else item.emailName!!)
    }

    override fun getLine2(default: StatusText): StatusText {
        if (item.emailName.isSemanticallyNull() || item.email.isSemanticallyNull()) {
            return default
        }
        return StatusText(item.email!!)
    }

    override fun getLine2FromField(field: SearchField<*>): StatusText? {
        if (field !is EmailField) return null
        val text = when (field) {
            EmailField.EMAIL -> item.email
            else -> null
        }
        return text?.toStatusText()
    }

    companion object {
        const val ITEM_TYPE_NAME_ID = R.string.email
    }
}