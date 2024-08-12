package com.dashlane.vault.textfactory.list

import android.content.res.Resources
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.R
import javax.inject.Inject

class PaymentPaypalListTextFactory @Inject constructor(
    private val resources: Resources,
) : DataIdentifierListTextFactory<SummaryObject.PaymentPaypal> {

    override fun getTitle(item: SummaryObject.PaymentPaypal): StatusText {
        val title = item.name
        return StatusText(if (title.isNotSemanticallyNull()) title!! else resources.getString(R.string.paypal))
    }

    override fun getDescription(item: SummaryObject.PaymentPaypal, default: StatusText): StatusText {
        return when {
            item.login.isSemanticallyNull() -> default
            item.isPasswordEmpty -> StatusText(
                resources.getString(R.string.incomplete_reason_missing_password_list_line_2),
                true
            )
            else -> StatusText(item.login!!)
        }
    }
}