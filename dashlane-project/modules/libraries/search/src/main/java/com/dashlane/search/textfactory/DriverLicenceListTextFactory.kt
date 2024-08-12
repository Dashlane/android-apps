package com.dashlane.search.textfactory

import com.dashlane.search.SearchField
import com.dashlane.search.fields.DriverLicenceField
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.list.StatusText
import com.dashlane.vault.textfactory.list.toStatusText

class DriverLicenceListTextFactory(
    private val item: SummaryObject.DriverLicence,
) : DataIdentifierSearchListTextFactory {

    override fun getLine2FromField(field: SearchField<*>): StatusText? {
        if (field !is DriverLicenceField) return null
        val text = when (field) {
            DriverLicenceField.FULL_NAME -> item.fullname
            else -> null
        }
        return text?.toStatusText()
    }
}