package com.dashlane.search.textfactory

import com.dashlane.search.SearchField
import com.dashlane.search.fields.AddressField
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.list.StatusText
import com.dashlane.vault.textfactory.list.toStatusText

class AddressSearchListTextFactory(
    private val item: SummaryObject.Address
) : DataIdentifierSearchListTextFactory {

    override fun getLine2FromField(field: SearchField<*>): StatusText? {
        if (field !is AddressField) return null

        val text = when (field) {
            AddressField.FULL -> item.addressFull
            AddressField.BUILDING -> item.building
            AddressField.CITY -> item.city
            AddressField.DOOR -> item.door
            AddressField.FLOOR -> item.floor
            AddressField.STREET_NAME -> item.streetName
            AddressField.ZIP -> item.zipCode
            else -> null
        }
        return text?.toStatusText()
    }
}