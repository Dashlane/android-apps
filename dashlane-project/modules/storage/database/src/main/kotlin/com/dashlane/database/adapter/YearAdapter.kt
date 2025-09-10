package com.dashlane.database.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.DateTimeException
import java.time.Year

internal object YearAdapter {
    @ToJson
    fun toJson(src: Year): Int {
        return src.value
    }

    @FromJson
    fun fromJson(value: Long): Year? {
        return try {
            Year.of(value.toInt())
        } catch (e: DateTimeException) {
            null
        }
    }
}