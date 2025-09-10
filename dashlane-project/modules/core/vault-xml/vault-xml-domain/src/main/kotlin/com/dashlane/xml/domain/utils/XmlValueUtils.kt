package com.dashlane.xml.domain.utils

internal fun String.toBooleanValue(): Boolean {
    val trimmedValue = trim()
    return trimmedValue.equals("true", ignoreCase = true) ||
        trimmedValue.equals("YES", ignoreCase = true) ||
        trimmedValue.toIntOrNull().let { it != null && it != 0 }
}

internal fun String.toLongValueOrNull(): Long? =
    trim().toLongOrNull()

internal fun String.toDoubleValueOrNull(): Double? =
    trim().toDoubleOrNull()