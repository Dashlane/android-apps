package com.dashlane.xml.domain.time

import java.time.DateTimeException
import java.time.LocalDate

private const val DATE_PART_COUNT = 3

internal fun LocalDate.toXmlString() =
    "$year-$monthValue-$dayOfMonth"

internal fun String.toLocalDateOrNull(): LocalDate? {
    val dateParts = trim().split('-')

    return localDateOrNull(dateParts)
}

private fun localDateOrNull(dateParts: List<String>): LocalDate? {
    if (dateParts.count() != DATE_PART_COUNT) return null

    val year: Int
    val month: Int
    val day: Int
    try {
        year = dateParts[0].toInt()
        month = dateParts[1].toInt()
        day = dateParts[2].toInt()
    } catch (e: NumberFormatException) {
        return null
    }

    return localDateOrNull(year, month, day)
}

@Suppress("SwallowedException")
private fun localDateOrNull(year: Int, month: Int, dayOfMonth: Int): LocalDate? {
    if (year == 0 && month == 0 && dayOfMonth == 0) return null

    return try {
        LocalDate.of(year, month, dayOfMonth)
    } catch (e: DateTimeException) {
        null
    }
}