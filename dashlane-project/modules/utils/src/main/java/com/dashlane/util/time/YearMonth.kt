package com.dashlane.util.time

import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth

fun YearMonth.atFirstDayOfMonth(): LocalDate =
    atDay(1)

fun YearMonth.isExpired(clock: Clock = Clock.systemDefaultZone()) =
    this < YearMonth.now(clock)

fun YearMonth.isExpiringSoon(clock: Clock = Clock.systemDefaultZone()) =
    this <= YearMonth.now(clock).plusMonths(EXPIRING_WARNING_DELAY_MONTHS)
