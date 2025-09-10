package com.dashlane.database.adapter

import com.dashlane.xml.XmlTransaction
import com.dashlane.xml.asObjectNode
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.toObject
import com.dashlane.xml.domain.toTransaction
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

internal object SyncObjectAdapter : JsonAdapter<SyncObject>() {
    private val xmlDataAdapter = XmlDataAdapter()

    @FromJson
    override fun fromJson(reader: JsonReader): SyncObject {
        val xmlData = xmlDataAdapter.fromJson(reader)
        return XmlTransaction(xmlData.asObjectNode()).toObject()
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: SyncObject?) {
        if (value == null) {
            writer.nullValue()
            return
        }
        val xmlData = value.toTransaction().node
        xmlDataAdapter.toJson(writer, xmlData)
    }
}