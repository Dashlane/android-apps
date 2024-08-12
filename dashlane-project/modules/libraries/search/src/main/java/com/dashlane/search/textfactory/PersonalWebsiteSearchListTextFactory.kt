package com.dashlane.search.textfactory

import com.dashlane.search.SearchField
import com.dashlane.search.fields.PersonalWebsiteField
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.list.StatusText
import com.dashlane.vault.textfactory.list.toStatusText

class PersonalWebsiteSearchListTextFactory(
    private val item: SummaryObject.PersonalWebsite
) : DataIdentifierSearchListTextFactory {

    override fun getLine2FromField(field: SearchField<*>): StatusText? {
        if (field !is PersonalWebsiteField) return null
        val text = when (field) {
            PersonalWebsiteField.WEBSITE -> item.website
            else -> null
        }
        return text?.toStatusText()
    }
}