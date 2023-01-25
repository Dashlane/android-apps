package com.dashlane.util.time

import java.time.Year
import java.time.temporal.ChronoField

fun String.toYearOrNull(): Year? =
    trim().toIntOrNull()?.toYearOrNull()

fun Int.toYearOrNull(): Year? =
    if (ChronoField.YEAR.range().isValidValue(this.toLong())) Year.of(this) else null