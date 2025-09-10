package com.dashlane.database.adapter

import com.dashlane.xml.domain.utils.Country
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

internal object CountryAdapter {
    @ToJson
    fun toJson(src: Country): String {
        return src.isoCode
    }

    @FromJson
    fun fromJson(value: String): Country? {
        return Country.forIsoCodeOrNull(value)
    }
}