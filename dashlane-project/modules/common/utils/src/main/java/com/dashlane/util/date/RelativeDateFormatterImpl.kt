package com.dashlane.util.date

import android.text.format.DateUtils
import java.time.Clock
import java.time.Instant

class RelativeDateFormatterImpl(private val clock: Clock) : RelativeDateFormatter {
    override fun format(instant: Instant) =
        DateUtils.getRelativeTimeSpanString(
            instant.toEpochMilli(),
            clock.millis(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR or
                DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_SHOW_TIME
        ).toString()
}