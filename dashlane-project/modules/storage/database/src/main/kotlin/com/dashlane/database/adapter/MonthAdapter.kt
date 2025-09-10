package com.dashlane.database.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.DateTimeException
import java.time.Month

internal object MonthAdapter {
    @ToJson
    fun toJson(src: Month): Int {
        return src.value
    }

    @FromJson
    fun fromJson(value: Long): Month? {
        return try {
            Month.of(value.toInt())
        } catch (e: DateTimeException) {
            null
        }
    }
}