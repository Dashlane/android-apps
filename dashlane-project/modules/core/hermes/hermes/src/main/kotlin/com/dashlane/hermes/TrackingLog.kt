package com.dashlane.hermes

import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID

interface TrackingLog {
    val name: String
    val category: Category
    val priority: Boolean

    fun collect(collector: Collector)

    interface Collector {
        operator fun set(
            key: String,
            value: Boolean?,
        )

        operator fun set(
            key: String,
            value: Int?,
        )

        operator fun set(
            key: String,
            value: Float?,
        )

        operator fun set(
            key: String,
            value: Double?,
        )

        operator fun set(
            key: String,
            value: String?,
        )

        operator fun set(
            key: String,
            value: UUID?,
        )

        operator fun set(
            key: String,
            value: Enum?,
        )

        operator fun set(
            key: String,
            value: PageEnum?,
        )

        operator fun set(
            key: String,
            value: Long?,
        )

        operator fun set(
            key: String,
            value: LocalDate?,
        )

        operator fun set(
            key: String,
            value: ZonedDateTime?,
        )

        operator fun set(
            key: String,
            value: Sha256Hash?,
        )

        operator fun set(
            key: String,
            value: Object?,
        )

        @Suppress("INAPPLICABLE_JVM_NAME")
        @JvmName("setSha256HashList")
        operator fun set(
            key: String,
            value: List<Sha256Hash?>?,
        )

        @Suppress("INAPPLICABLE_JVM_NAME")
        @JvmName("setEnumList")
        operator fun set(
            key: String,
            value: List<Enum>?,
        )

        operator fun set(
            key: String,
            value: DataObject?,
        )
    }

    interface Enum {
        val code: String
    }

    interface PageEnum {
        val code: String
        val priority: Boolean
    }

    interface Object {
        fun collect(collector: Collector)
    }

    interface DataObject {
        val code: String
    }

    enum class Category(val code: String) {
        USER("user"),
        ANONYMOUS("anonymous"),
        ACTIVITY_LOG("activity_log"),
    }
}