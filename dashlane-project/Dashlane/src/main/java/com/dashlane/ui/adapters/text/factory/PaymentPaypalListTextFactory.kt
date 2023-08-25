package com.dashlane.ui.adapters.text.factory

import android.content.Context
import com.dashlane.R
import com.dashlane.search.SearchField
import com.dashlane.ui.adapters.text.factory.DataIdentifierListTextFactory.StatusText
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.summary.SummaryObject

class PaymentPaypalListTextFactory(
    private val context: Context,
    private val item: SummaryObject.PaymentPaypal
) : DataIdentifierListTextFactory {

    override fun getLine1(): StatusText {
        val title = item.name
        val incomplete = context.getString(ITEM_TYPE_NAME_ID)
        return StatusText(if (title.isNotSemanticallyNull()) title!! else incomplete)
    }

    override fun getLine2(default: StatusText): StatusText {
        return when {
            item.login.isSemanticallyNull() -> default
            item.isPasswordEmpty -> StatusText(
                context.getString(R.string.incomplete_reason_missing_password_list_line_2),
                true
            )
            else -> StatusText(item.login!!)
        }
    }

    override fun getLine2FromField(field: SearchField<*>): StatusText? = null

    companion object {
        const val ITEM_TYPE_NAME_ID = R.string.paypal
    }
}