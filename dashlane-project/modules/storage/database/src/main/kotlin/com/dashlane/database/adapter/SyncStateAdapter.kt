package com.dashlane.database.adapter

import com.dashlane.vault.model.SyncState
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

internal object SyncStateAdapter : JsonAdapter<SyncState>() {
    private val map =
        enumValues<SyncState>().associateBy(SyncState::code)

    @FromJson
    override fun fromJson(reader: JsonReader): SyncState {
        val code = reader.nextString()
        return map[code] ?: error("Invalid sync state code $code")
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: SyncState?) {
        value ?: return
        writer.value(value.code)
    }
}