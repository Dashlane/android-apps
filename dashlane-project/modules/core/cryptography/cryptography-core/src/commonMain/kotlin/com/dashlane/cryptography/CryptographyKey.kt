package com.dashlane.cryptography

import okio.Closeable
import kotlin.jvm.JvmStatic

sealed class CryptographyKey(internal val bytes: ObfuscatedByteArray) : Closeable {
    abstract val type: Type

    fun toObfuscatedByteArray() =
        bytes.clone()

    fun toByteArray() =
        bytes.toByteArray()

    fun toByteArray(offset: Int, byteCount: Int): ByteArray =
        bytes.toByteArray(offset, byteCount)

    abstract fun clone(): CryptographyKey

    override fun close() {
        bytes.close()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CryptographyKey) return false
        if (type != other.type) return false
        if (bytes != other.bytes) return false
        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + bytes.hashCode()
        return result
    }

    override fun toString(): String =
        "CryptographyKey(type=$type, bytes=$bytes)"

    class Password internal constructor(bytes: ObfuscatedByteArray) : CryptographyKey(bytes) {
        override val type: Type
            get() = Type.PASSWORD

        override fun clone() =
            Password(bytes.clone())
    }

    class Raw32 internal constructor(bytes: ObfuscatedByteArray) : CryptographyKey(bytes) {
        override val type: Type
            get() = Type.RAW_32

        override fun clone() =
            Raw32(bytes.clone())
    }

    class Raw64 internal constructor(bytes: ObfuscatedByteArray) : CryptographyKey(bytes) {
        override val type: Type
            get() = Type.RAW_64

        override fun clone() =
            Raw64(bytes.clone())
    }

    companion object {
        const val KEY_SIZE_16 = 16
        const val KEY_SIZE_32 = 32
        const val KEY_SIZE_64 = 64

        @JvmStatic
        fun ofBytes(bytes: ByteArray, type: Type) = when (type) {
            Type.PASSWORD -> ofPasswordUtf8Bytes(bytes)
            Type.RAW_32 -> ofBytes32(bytes)
            Type.RAW_64 -> ofBytes64(bytes)
        }

        @JvmStatic
        fun ofBytes(bytes: ObfuscatedByteArray, type: Type) = when (type) {
            Type.PASSWORD -> ofPasswordUtf8Bytes(bytes)
            Type.RAW_32 -> ofBytes32(bytes)
            Type.RAW_64 -> ofBytes64(bytes)
        }

        @JvmStatic
        fun ofPassword(password: String): Password =
            Password(password.encodeUtf8ToObfuscated())

        @JvmStatic
        fun ofPasswordUtf8Bytes(bytes: ByteArray): Password =
            Password(bytes.toObfuscated())

        @JvmStatic
        fun ofPasswordUtf8Bytes(bytes: ObfuscatedByteArray): Password =
            Password(bytes.clone())

        @JvmStatic
        fun ofBytes32(bytes: ByteArray): Raw32 {
            val size = bytes.size
            require(
                size == KEY_SIZE_32 || size == KEY_SIZE_16 || size == KEY_SIZE_64,
            ) { "Expected key size 32 (or 16 or 64), but was $size" }
            return Raw32(bytes.toObfuscated())
        }

        @JvmStatic
        fun ofBytes32(bytes: ObfuscatedByteArray): Raw32 {
            val size = bytes.size
            require(
                size == KEY_SIZE_32 || size == KEY_SIZE_16 || size == KEY_SIZE_64,
            ) { "Expected key size 32 (or 16 or 64), but was $size" }
            return Raw32(bytes.clone())
        }

        @JvmStatic
        fun ofBytes64(bytes: ByteArray): Raw64 {
            val size = bytes.size
            require(size == KEY_SIZE_64) { "Expected key size 64, but was $size" }
            return Raw64(bytes.toObfuscated())
        }

        @JvmStatic
        fun ofBytes64(bytes: ObfuscatedByteArray): Raw64 {
            val size = bytes.size
            require(size == KEY_SIZE_64) { "Expected key size 64, but was $size" }
            return Raw64(bytes.clone())
        }
    }

    enum class Type {
        PASSWORD,

        RAW_32,

        RAW_64,
    }
}