package com.dashlane.util.time

import java.time.Month
import java.time.temporal.ChronoField

fun String.toMonthOrNull(): Month? =
    trim().toIntOrNull()?.toMonthOrNull()

fun Int.toMonthOrNull(): Month? =
    if (ChronoField.MONTH_OF_YEAR.range().isValidValue(this.toLong())) Month.of(this) else null