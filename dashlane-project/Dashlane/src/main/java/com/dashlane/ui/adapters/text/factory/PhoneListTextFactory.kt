package com.dashlane.ui.adapters.text.factory

import android.content.Context
import com.dashlane.R
import com.dashlane.search.SearchField
import com.dashlane.ui.adapters.text.factory.DataIdentifierListTextFactory.StatusText
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.summary.SummaryObject

class PhoneListTextFactory(
    private val context: Context,
    private val item: SummaryObject.Phone
) : DataIdentifierListTextFactory {

    override fun getLine1(): StatusText {
        return StatusText(
            if (item.phoneName.isSemanticallyNull())
                context.getString(ITEM_TYPE_NAME_ID) else item.phoneName!!
        )
    }

    override fun getLine2(default: StatusText): StatusText {
        if (item.phoneName.isSemanticallyNull() || item.number.isSemanticallyNull()) {
            return default
        }
        return StatusText(item.number!!)
    }

    override fun getLine2FromField(field: SearchField<*>): StatusText? = null

    companion object {
        const val ITEM_TYPE_NAME_ID = R.string.phone
    }
}