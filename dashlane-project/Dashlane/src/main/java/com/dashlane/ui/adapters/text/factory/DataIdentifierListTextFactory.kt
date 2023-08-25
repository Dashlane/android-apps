package com.dashlane.ui.adapters.text.factory

import android.content.Context
import com.dashlane.R
import com.dashlane.search.SearchField
import com.dashlane.util.time.isExpired
import com.dashlane.util.time.isExpiringSoon
import com.dashlane.util.isSemanticallyNull
import java.time.LocalDate
import java.time.YearMonth

interface DataIdentifierListTextFactory {

    data class StatusText(val text: String, val isWarning: Boolean = false, val textToHighlight: String? = null)

    fun getLine1(): StatusText

    fun getLine2(default: StatusText): StatusText

    fun getLine2FromField(field: SearchField<*>): StatusText?

    fun LocalDate.getIdentityStatusText(context: Context, number: String?, default: StatusText): StatusText =
        getIdentityStatusText(context, isExpired(), isExpiringSoon(), number, default)

    fun YearMonth.getIdentityStatusText(context: Context, number: String?, default: StatusText): StatusText =
        getIdentityStatusText(context, isExpired(), isExpiringSoon(), number, default)

    private fun getIdentityStatusText(
        context: Context,
        expired: Boolean,
        expiringSoon: Boolean,
        number: String?,
        default: StatusText
    ) = when {
        expired -> StatusText(context.getString(R.string.item_expired), true)
        expiringSoon -> StatusText(context.getString(R.string.item_expiring_soon), true)
        number.isSemanticallyNull() -> default
        else -> StatusText(number!!)
    }
}

internal fun String.toStatusText(): DataIdentifierListTextFactory.StatusText =
    DataIdentifierListTextFactory.StatusText(text = this)