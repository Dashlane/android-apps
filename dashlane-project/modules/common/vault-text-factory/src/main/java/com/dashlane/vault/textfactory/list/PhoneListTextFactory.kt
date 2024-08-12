package com.dashlane.vault.textfactory.list

import android.content.res.Resources
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.R
import javax.inject.Inject

class PhoneListTextFactory @Inject constructor(
    private val resources: Resources,
) : DataIdentifierListTextFactory<SummaryObject.Phone> {

    override fun getTitle(item: SummaryObject.Phone): StatusText {
        return StatusText(
            if (item.phoneName.isSemanticallyNull()) {
                resources.getString(R.string.phone)
            } else {
                item.phoneName!!
            }
        )
    }

    override fun getDescription(item: SummaryObject.Phone, default: StatusText): StatusText {
        if (item.phoneName.isSemanticallyNull() || item.number.isSemanticallyNull()) {
            return default
        }
        return StatusText(item.number!!)
    }
}