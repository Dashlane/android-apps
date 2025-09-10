package com.dashlane.breach.utils

import com.dashlane.breach.Breach.Companion.EPOCH_TIMESTAMP
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField

class BreachDateSerializer : KSerializer<Long> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(
            serialName = "com.dashlane.passwordhealth.identitydashboard.breach.date",
            kind = PrimitiveKind.LONG,
        )

    override fun serialize(encoder: Encoder, value: Long) {
        encoder.encodeLong(value)
    }

    override fun deserialize(decoder: Decoder): Long {
        return try {
            decoder.decodeLong()
        } catch (_: Exception) {
            
            parseDate(decoder.decodeString())
                ?.epochSecond
                ?: run {
                    EPOCH_TIMESTAMP
                }
        }
    }

    companion object {
        fun parseDate(date: String?, clock: Clock = Clock.systemUTC()): Instant? {
            date ?: return null
            
            date.toLongOrNull()
                ?.takeIf { it > 10_000 } 
                ?.let {
                    return if (it < 100_000_000_000L) {
                        Instant.ofEpochSecond(it) 
                    } else {
                        Instant.ofEpochMilli(it) 
                    }
                }
            
            DATES_FORMAT.forEach { (pattern, timeToAdd) ->
                val formatter = DateTimeFormatter.ofPattern(pattern)
                val accessor = try {
                    formatter.parse(date)
                } catch (_: Exception) {
                    
                    return@forEach
                }
                val localDate = LocalDate.of(
                    accessor[ChronoField.YEAR],
                    if (accessor.isSupported(ChronoField.MONTH_OF_YEAR)) {
                        accessor[ChronoField.MONTH_OF_YEAR]
                    } else {
                        1
                    },
                    if (accessor.isSupported(ChronoField.DAY_OF_MONTH)) {
                        accessor[ChronoField.DAY_OF_MONTH]
                    } else {
                        1
                    },
                )
                return ZonedDateTime.of(localDate, LocalTime.MIDNIGHT, clock.zone).toInstant() + timeToAdd
            }
            return null
        }

        val DATES_FORMAT = listOf(
            
            "yyyy-MM-dd" to Duration.ZERO,
            
            "yyyy-MM" to Duration.ofDays(30),
            
            "yyyy" to Duration.ofDays(364),
        )
    }
}