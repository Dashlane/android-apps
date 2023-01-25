package com.dashlane.ui.adapters.text.factory

import android.content.Context
import com.dashlane.R
import com.dashlane.search.SearchField
import com.dashlane.search.fields.AddressField
import com.dashlane.ui.adapters.text.factory.DataIdentifierListTextFactory.StatusText
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.getAddressCompleteWithoutName

class AddressListTextFactory(
    private val context: Context,
    private val item: SummaryObject.Address
) : DataIdentifierListTextFactory {

    override fun getLine1(): StatusText {
        val label = item.addressName?.takeUnless { it.isSemanticallyNull() }
            ?: return StatusText(context.getString(ITEM_TYPE_NAME_ID))
        return StatusText(label)
    }

    override fun getLine2(default: StatusText): StatusText {
        val text = item.getAddressCompleteWithoutName(context)
        if (item.addressName.isSemanticallyNull() || text.isSemanticallyNull()) {
            return default
        }
        return StatusText(text)
    }

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

    companion object {
        const val ITEM_TYPE_NAME_ID = R.string.address
    }
}