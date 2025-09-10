package com.dashlane.database

import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.cryptography.toCryptographyMarker
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson

@JsonClass(generateAdapter = true)
data class DatabaseIndex(
    @Json(name = "version")
    val version: Int = VERSION,
    @Json(name = "summaryVersion")
    val summaryVersion: Int = SUMMARY_VERSION,
    @Json(name = "user")
    val user: String,
) {
    companion object {
        internal const val VERSION = 1

        internal const val SUMMARY_VERSION = 27

        internal val DEFAULT_CRYPTOGRAPHY = CryptographyMarker.Flexible.Defaults.noDerivation

        internal val JSON_ADAPTER: JsonAdapter<DatabaseIndex> = Moshi.Builder()
            .add(CryptographyMarkerAdapter)
            .build()
            .adapter(DatabaseIndex::class.java)
    }

    @Throws(DatabaseIndexUserNotMatchException::class)
    fun requireUserMatch(userName: String) {
        if (this.user != userName) throw DatabaseIndexUserNotMatchException("Index doesn't match user name")
    }

    private object CryptographyMarkerAdapter {
        @ToJson
        fun toJson(src: CryptographyMarker): String {
            return src.value
        }

        @FromJson
        fun fromJson(value: String): CryptographyMarker {
            return value.toCryptographyMarker()
        }
    }
}