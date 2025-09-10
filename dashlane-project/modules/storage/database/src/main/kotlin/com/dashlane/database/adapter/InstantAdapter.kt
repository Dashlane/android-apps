package com.dashlane.database.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.Instant

internal object InstantAdapter {
    @ToJson
    fun toJson(src: Instant): Long {
        return src.toEpochMilli()
    }

    @FromJson
    fun fromJson(value: Long): Instant {
        return Instant.ofEpochMilli(value)
    }
}