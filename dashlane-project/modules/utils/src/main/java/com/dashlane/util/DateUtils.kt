package com.dashlane.util

import android.content.Context
import androidx.annotation.StringRes
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

fun YearMonth.toExpirationDateFormat(): String =
    String.format("%02d - %02d", monthValue, year % 100)

fun LocalDate.toIdentityFormat(context: Context): String? =
    formatDate(context, R.string.date_format_identity)

internal fun LocalDate.formatDate(context: Context, @StringRes dateFormat: Int): String? {
    val locale = context.resources.configuration.locales[0]
    return try {
        val dateTimeFormatter = DateTimeFormatter.ofPattern(context.getString(dateFormat), locale)
        dateTimeFormatter.format(this)
    } catch (e: Exception) {
        null
    }
}