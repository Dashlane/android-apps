package com.dashlane.xml.domain.time

import java.time.Month

private const val MONTH_MIN_VALUE = 1
private const val MONTH_MAX_VALUE = 12

internal fun Month.toXmlString() =
    "$value"

internal fun String.toMonthValueOrNull(): Month? {
    val month = trim().toIntOrNull() ?: return null
    if (month !in MONTH_MIN_VALUE..MONTH_MAX_VALUE) return null
    return Month.of(month)
}