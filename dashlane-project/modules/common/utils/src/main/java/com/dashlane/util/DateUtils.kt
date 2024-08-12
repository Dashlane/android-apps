package com.dashlane.util

import android.content.res.Resources
import androidx.annotation.StringRes
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

fun YearMonth.toExpirationDateFormat(): String =
    String.format("%02d - %02d", monthValue, year % 100)

fun LocalDate.toIdentityFormat(resources: Resources): String? =
    formatDate(resources, R.string.date_format_identity)

internal fun LocalDate.formatDate(resources: Resources, @StringRes dateFormat: Int): String? {
    val locale = resources.configuration.locales[0]
    return try {
        val dateTimeFormatter = DateTimeFormatter.ofPattern(resources.getString(dateFormat), locale)
        dateTimeFormatter.format(this)
    } catch (e: Exception) {
        null
    }
}