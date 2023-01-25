package com.dashlane.ui.adapters.text.factory

import android.content.Context
import com.dashlane.R
import com.dashlane.search.fields.PersonalWebsiteField
import com.dashlane.search.SearchField
import com.dashlane.ui.adapters.text.factory.DataIdentifierListTextFactory.StatusText
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.summary.SummaryObject

class PersonalWebsiteListTextFactory(
    private val context: Context,
    private val item: SummaryObject.PersonalWebsite
) : DataIdentifierListTextFactory {

    override fun getLine1(): StatusText {
        return StatusText(
            if (item.name.isNotSemanticallyNull())
                item.name!! else context.getString(ITEM_TYPE_NAME_ID)
        )
    }

    override fun getLine2(default: StatusText): StatusText {
        if (item.name.isSemanticallyNull() || item.website.isSemanticallyNull()) {
            return default
        }
        return StatusText(item.website!!)
    }

    override fun getLine2FromField(field: SearchField<*>): StatusText? {
        if (field !is PersonalWebsiteField) return null
        val text = when (field) {
            PersonalWebsiteField.WEBSITE -> item.website
            else -> null
        }
        return text?.toStatusText()
    }

    companion object {
        const val ITEM_TYPE_NAME_ID = R.string.personal_website
    }
}