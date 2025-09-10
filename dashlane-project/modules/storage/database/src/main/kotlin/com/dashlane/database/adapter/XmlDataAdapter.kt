package com.dashlane.database.adapter

import com.dashlane.xml.XmlData
import com.dashlane.xml.domain.SyncObjectType
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter

internal class XmlDataAdapter : JsonAdapter<XmlData>() {
    override fun toJson(writer: JsonWriter, value: XmlData?) {
        when (value) {
            is XmlData.ItemNode -> writer.value(value.value)
            is XmlData.ItemNodeObfuscated -> writer.value(value.value.toString())
            is XmlData.ListNode -> writeListNode(writer, value)
            is XmlData.CollectionNode -> writeCollectionNode(writer, value)
            is XmlData.ObjectNode -> writeObjectNode(writer, value)
            null -> writer.nullValue()
        }
    }

    private fun writeListNode(writer: JsonWriter, value: XmlData.ListNode) {
        writeListNodeData(writer, value.value)
    }

    private fun writeListNodeData(
        writer: JsonWriter,
        data: List<XmlData>,
    ) {
        writer.beginArray()
        for (value in data) {
            toJson(writer, value)
        }
        writer.endArray()
    }

    private fun writeCollectionNode(writer: JsonWriter, value: XmlData.CollectionNode) {
        writer.beginObject()
        writer.name(VaultItemJsonName.FIELD_COLLECTION)
        writeObjectNodeData(writer, value.value)
        writer.endObject()
    }

    private fun writeObjectNode(writer: JsonWriter, value: XmlData.ObjectNode) {
        writer.beginObject()
        writer.name(VaultItemJsonName.FIELD_TYPE)
        writer.value(value.type)
        writer.name(VaultItemJsonName.FIELD_OBJECT)
        writeObjectNodeData(writer, value.data)
        writer.endObject()
    }

    private fun writeObjectNodeData(
        writer: JsonWriter,
        data: Map<String, XmlData>,
    ) {
        writer.beginObject()
        for ((key, value) in data) {
            writer.name(key)
            toJson(writer, value)
        }
        writer.endObject()
    }

    override fun fromJson(reader: JsonReader): XmlData =
        readXmlData(reader)

    private fun readXmlData(reader: JsonReader, key: String? = null, syncObjectType: SyncObjectType? = null) =
        when (val token = reader.peek()) {
            JsonReader.Token.STRING -> readItemNode(reader, key, syncObjectType)
            JsonReader.Token.BEGIN_ARRAY -> readListNode(reader)
            JsonReader.Token.BEGIN_OBJECT -> readObjectOrCollectionNode(reader)
            else -> error("Unexpected token $token")
        }

    private fun readItemNode(reader: JsonReader, key: String? = null, syncObjectType: SyncObjectType? = null): XmlData {
        val value = reader.nextString()
        return if (key != null && syncObjectType != null && syncObjectType.sensitiveFields.contains(key)) {
            XmlData.ItemNodeObfuscated(value)
        } else {
            XmlData.ItemNode(value)
        }
    }

    private fun readListNode(
        reader: JsonReader,
    ): XmlData.ListNode {
        reader.beginArray()
        val data = mutableListOf<XmlData>().also {
            while (reader.peek() != JsonReader.Token.END_ARRAY) {
                it.add(fromJson(reader))
            }
        }
        reader.endArray()
        return XmlData.ListNode(data)
    }

    private fun readObjectOrCollectionNode(reader: JsonReader): XmlData {
        reader.beginObject()
        val xmlData = when (val name = reader.nextName()) {
            VaultItemJsonName.FIELD_COLLECTION ->
                readCollectionNodeContents(reader)
            VaultItemJsonName.FIELD_TYPE ->
                readObjectNodeContents(reader)
            else ->
                error("Unexpected property $name.")
        }
        reader.endObject()
        return xmlData
    }

    private fun readCollectionNodeContents(reader: JsonReader): XmlData.CollectionNode =
        XmlData.CollectionNode(readObjectNodeData(reader))

    private fun readObjectNodeContents(reader: JsonReader): XmlData.ObjectNode {
        val type = reader.nextString()
        val syncObjectType = SyncObjectType.forXmlNameOrNull(type)
        check(reader.nextName() == VaultItemJsonName.FIELD_OBJECT)
        return XmlData.ObjectNode(type, readObjectNodeData(reader, syncObjectType))
    }

    private fun readObjectNodeData(
        reader: JsonReader,
        syncObjectType: SyncObjectType? = null,
    ): Map<String, XmlData> {
        reader.beginObject()
        val data = mutableMapOf<String, XmlData>().also { map ->
            while (reader.peek() != JsonReader.Token.END_OBJECT) {
                val key = reader.nextName()
                map[key] = readXmlData(reader, key, syncObjectType)
            }
        }
        reader.endObject()
        return data
    }
}