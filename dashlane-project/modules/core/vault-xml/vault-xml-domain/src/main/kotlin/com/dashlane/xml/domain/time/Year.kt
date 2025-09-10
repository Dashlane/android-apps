package com.dashlane.xml.domain.time

import java.time.Year

internal fun Year.toXmlString() =
    "$value"

internal fun String.toYearValueOrNull(): Year? {
    val year = trim().toIntOrNull() ?: return null
    if (year !in Year.MIN_VALUE..Year.MAX_VALUE) return null
    return Year.of(year)
}