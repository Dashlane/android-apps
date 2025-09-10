package com.dashlane.xml.domain.time

import java.time.DateTimeException
import java.time.Instant

internal fun Instant.toXmlString(): String =
    epochSecond.toString()

internal fun String.toInstantOrNull(): Instant? {
    val epochSecond = trim().toLongOrNull() ?: return null

    return instantOrNull(epochSecond)
}

@Suppress("SwallowedException")
private fun instantOrNull(epochSecond: Long): Instant? {
    if (epochSecond == 0L || epochSecond == Int.MAX_VALUE.toLong()) return null

    return try {
        Instant.ofEpochSecond(epochSecond)
    } catch (e: DateTimeException) {
        null
    }
}