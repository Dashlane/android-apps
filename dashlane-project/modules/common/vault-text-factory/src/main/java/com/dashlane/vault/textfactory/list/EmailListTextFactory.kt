package com.dashlane.vault.textfactory.list

import android.content.res.Resources
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.R
import javax.inject.Inject

class EmailListTextFactory @Inject constructor(
    private val resources: Resources,
) : DataIdentifierListTextFactory<SummaryObject.Email> {

    override fun getTitle(item: SummaryObject.Email): StatusText {
        return StatusText(if (item.emailName.isSemanticallyNull()) resources.getString(R.string.email) else item.emailName!!)
    }

    override fun getDescription(item: SummaryObject.Email, default: StatusText): StatusText {
        if (item.emailName.isSemanticallyNull() || item.email.isSemanticallyNull()) {
            return default
        }
        return StatusText(item.email!!)
    }
}