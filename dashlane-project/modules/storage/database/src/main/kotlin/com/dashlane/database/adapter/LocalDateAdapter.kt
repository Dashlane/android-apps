package com.dashlane.database.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.LocalDate

internal object LocalDateAdapter {
    @ToJson
    fun toJson(src: LocalDate): String {
        return src.toString()
    }

    @FromJson
    fun fromJson(value: String): LocalDate {
        return LocalDate.parse(value)
    }
}