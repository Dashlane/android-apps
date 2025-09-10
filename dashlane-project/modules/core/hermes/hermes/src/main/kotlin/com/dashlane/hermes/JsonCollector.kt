package com.dashlane.hermes

import com.google.gson.stream.JsonWriter
import java.io.StringWriter
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

internal class JsonCollector(private val jsonWriter: JsonWriter) : TrackingLog.Collector {
    override fun set(
        key: String,
        value: Boolean?,
    ) {
        if (value != null) {
            jsonWriter.name(key)
            jsonWriter.value(value)
        }
    }

    override fun set(
        key: String,
        value: Int?,
    ) {
        if (value != null) {
            jsonWriter.name(key)
            jsonWriter.value(value)
        }
    }

    override fun set(
        key: String,
        value: Float?,
    ) {
        if (value != null) {
            jsonWriter.name(key)
            jsonWriter.value(value)
        }
    }

    override fun set(
        key: String,
        value: Double?,
    ) {
        if (value != null) {
            jsonWriter.name(key)
            jsonWriter.value(value)
        }
    }

    override fun set(
        key: String,
        value: String?,
    ) {
        if (value != null) {
            jsonWriter.name(key)
            jsonWriter.value(value)
        }
    }

    override fun set(
        key: String,
        value: UUID?,
    ) {
        if (value != null) {
            jsonWriter.name(key)
            jsonWriter.value(value.toString())
        }
    }

    override fun set(
        key: String,
        value: TrackingLog.Enum?,
    ) {
        if (value != null) {
            jsonWriter.name(key)
            jsonWriter.value(value.code)
        }
    }

    override fun set(
        key: String,
        value: TrackingLog.PageEnum?,
    ) {
        if (value != null) {
            jsonWriter.name(key)
            jsonWriter.value(value.code)
        }
    }

    override fun set(
        key: String,
        value: Long?,
    ) {
        if (value != null) {
            jsonWriter.name(key)
            jsonWriter.value(value)
        }
    }

    override fun set(
        key: String,
        value: LocalDate?,
    ) {
        if (value != null) {
            jsonWriter.name(key)
            jsonWriter.value(value.format(DateTimeFormatter.ISO_LOCAL_DATE))
        }
    }

    override fun set(
        key: String,
        value: ZonedDateTime?,
    ) {
        if (value != null) {
            jsonWriter.name(key)
            jsonWriter.value(value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
        }
    }

    override fun set(
        key: String,
        value: Sha256Hash?,
    ) {
        if (value != null) {
            jsonWriter.name(key)
            jsonWriter.value(value.hex)
        }
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("setSha256HashList")
    override operator fun set(
        key: String,
        value: List<Sha256Hash?>?,
    ) {
        if (value != null) {
            jsonWriter.name(key)
            jsonWriter.beginArray()
            value.forEach { hash ->
                hash?.let { jsonWriter.value(it.hex) }
            }
            jsonWriter.endArray()
        }
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("setEnumList")
    override fun set(
        key: String,
        value: List<TrackingLog.Enum>?,
    ) {
        if (value != null) {
            jsonWriter.name(key)
            jsonWriter.beginArray()
            value.forEach {
                jsonWriter.value(it.code)
            }
            jsonWriter.endArray()
        }
    }

    override fun set(
        key: String,
        value: TrackingLog.Object?,
    ) {
        if (value != null) {
            jsonWriter.name(key)
            jsonWriter.beginObject()
            value.collect(this)
            jsonWriter.endObject()
        }
    }

    override fun set(
        key: String,
        value: TrackingLog.DataObject?,
    ) {
        
        set(key, value?.code)
    }
}

internal inline fun collectToJsonString(block: (JsonWriter, JsonCollector) -> Unit = { _, _ -> }): String =
    writeJsonString { writer ->
        writer.beginObject()
        block(writer, JsonCollector(writer))
        writer.endObject()
    }

internal inline fun writeJsonString(block: (JsonWriter) -> Unit): String = writeString { JsonWriter(it).use(block) }

internal inline fun writeString(block: (StringWriter) -> Unit): String = StringWriter().also(block).toString()