package com.dashlane.vault.textfactory.list.utils

import android.content.res.Resources
import com.dashlane.util.isSemanticallyNull
import com.dashlane.util.time.isExpired
import com.dashlane.util.time.isExpiringSoon
import com.dashlane.vault.textfactory.R
import com.dashlane.vault.textfactory.list.StatusText
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth

internal fun LocalDate.getIdentityStatusText(resources: Resources, number: String?, default: StatusText, clock: Clock): StatusText =
    getIdentityStatusText(resources, isExpired(clock), isExpiringSoon(clock), number, default)

internal fun YearMonth.getIdentityStatusText(resources: Resources, number: String?, default: StatusText, clock: Clock): StatusText =
    getIdentityStatusText(resources, isExpired(clock), isExpiringSoon(clock), number, default)

private fun getIdentityStatusText(
    resources: Resources,
    expired: Boolean,
    expiringSoon: Boolean,
    number: String?,
    default: StatusText,
) = when {
    expired -> StatusText(resources.getString(R.string.item_expired), true)
    expiringSoon -> StatusText(resources.getString(R.string.item_expiring_soon), true)
    number.isSemanticallyNull() -> default
    else -> StatusText(number!!)
}
