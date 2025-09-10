package com.dashlane.autofill.util

internal inline fun <T> tryOrNull(block: () -> T): T? = try {
    block()
} catch (ignored: Exception) {
    null
}