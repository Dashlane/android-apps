package com.dashlane.premium.utils

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object PriceUtils {
    const val MICRO = 1_000_000
}

fun Double.toFormattedPrice(currencyCode: String, locale: Locale): String =
    NumberFormat.getCurrencyInstance(locale).apply {
        currency = Currency.getInstance(currencyCode)
        maximumFractionDigits = 2
        minimumFractionDigits = 0
    }.format(this)