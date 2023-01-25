package com.dashlane.util.time

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneOffset

internal const val EXPIRING_WARNING_DELAY_MONTHS = 3L



fun LocalDate.toSeconds(): Long =
    toInstant().epochSecond

fun LocalDate.toInstant(): Instant =
    atTime(LocalTime.NOON).atZone(ZoneOffset.UTC).toInstant()

fun LocalDate.yearMonth(): YearMonth =
    YearMonth.of(year, month)



fun LocalDate.isExpired(clock: Clock = Clock.systemDefaultZone()) =
    this < LocalDate.now(clock)



fun LocalDate.isExpiringSoon(clock: Clock = Clock.systemDefaultZone()) =
    this <= LocalDate.now(clock).plusMonths(EXPIRING_WARNING_DELAY_MONTHS)
