package com.dashlane.vault.textfactory.list

import android.content.Context
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.R
import com.dashlane.vault.util.getAddressCompleteWithoutName
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AddressListTextFactory @Inject constructor(
    @ApplicationContext private val context: Context,
) : DataIdentifierListTextFactory<SummaryObject.Address> {

    override fun getTitle(item: SummaryObject.Address): StatusText {
        val label = item.addressName?.takeUnless { it.isSemanticallyNull() }
            ?: return StatusText(context.getString(R.string.address))
        return StatusText(label)
    }

    override fun getDescription(item: SummaryObject.Address, default: StatusText): StatusText {
        val text = item.getAddressCompleteWithoutName(context)
        if (item.addressName.isSemanticallyNull() || text.isSemanticallyNull()) {
            return default
        }
        return StatusText(text)
    }
}