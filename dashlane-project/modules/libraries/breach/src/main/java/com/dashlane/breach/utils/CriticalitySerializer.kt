package com.dashlane.breach.utils

import com.dashlane.breach.Breach.Companion.DEFAULT_CRITICALITY
import com.dashlane.util.tryOrNull
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class CriticalitySerializer : KSerializer<Int> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(
            serialName = "com.dashlane.passwordhealth.identitydashboard.breach.criticality",
            kind = PrimitiveKind.INT,
        )

    override fun serialize(encoder: Encoder, value: Int) {
        encoder.encodeInt(value)
    }

    override fun deserialize(decoder: Decoder): Int {
        return try {
            decoder.decodeInt()
        } catch (_: Exception) {
            DEFAULT_CRITICALITY
        }
    }
}