package com.dashlane.cryptography

import okio.Closeable
import kotlin.experimental.xor
import kotlin.jvm.JvmSynthetic

class ObfuscatedByteArray : Closeable, Serializable {
    private val mask: ByteArray
    private val bytes: ByteArray

    constructor(bytes: ByteArray) {
        this.mask = secureRandom.nextBytes(bytes.size)
        this.bytes = ByteArray(bytes.size) { bytes[it] xor mask[it] }
    }

    constructor(other: ObfuscatedByteArray) {
        other.checkNotClosed()
        this.mask = secureRandom.nextBytes(other._size)
        this.bytes = ByteArray(other._size) { other.getByte(it) xor mask[it] }
    }

    private constructor(mask: ByteArray, bytes: ByteArray) {
        this.mask = mask
        this.bytes = bytes
    }

    private var closed = false

    private val _indices
        get() = bytes.indices
    val indices
        get() = checkNotClosed { _indices }

    private val _size
        get() = bytes.size
    val size: Int
        get() = checkNotClosed { _size }

    operator fun get(index: Int): Byte =
        checkNotClosed { getByte(index) }

    private fun getByte(index: Int) =
        bytes[index] xor mask[index]

    operator fun plus(other: ByteArray): ObfuscatedByteArray {
        checkNotClosed()
        val mask = secureRandom.nextBytes(_size + other.size)
        val bytes = ByteArray(_size + other.size)
        for (i in 0 until _size) {
            bytes[i] = getByte(i) xor mask[i]
        }
        for (i in 0 until other.size) {
            bytes[_size + i] = other[i] xor mask[_size + i]
        }
        return ObfuscatedByteArray(bytes, mask)
    }

    operator fun plus(other: ObfuscatedByteArray): ObfuscatedByteArray {
        checkNotClosed()
        other.checkNotClosed()
        val mask = secureRandom.nextBytes(_size + other._size)
        val bytes = ByteArray(_size + other._size)
        for (i in 0 until _size) {
            bytes[i] = getByte(i) xor mask[i]
        }
        for (i in 0 until other._size) {
            bytes[_size + i] = other.getByte(i) xor mask[_size + i]
        }
        return ObfuscatedByteArray(bytes, mask)
    }

    fun toByteArray(): ByteArray =
        checkNotClosed { ByteArray(_size) { getByte(it) } }

    fun toByteArray(offset: Int, byteCount: Int): ByteArray {
        require(offset >= 0) { "offset < 0" }
        require(byteCount >= 0) { "byteCount < 0" }
        require(size >= offset + byteCount) { "size < offset + byteCount" }
        checkNotClosed()
        return ByteArray(byteCount) { getByte(offset + it) }
    }

    fun decodeUtf8ToCharArray(): CharArray =
        toByteArray().use(ByteArray::decodeUtf8ToCharArray)

    fun decodeUtf8ToString(): String =
        toByteArray().use(ByteArray::decodeUtf8ToString)

    override fun equals(other: Any?): Boolean {
        checkNotClosed()
        if (this === other) return true
        if (other !is ObfuscatedByteArray) return false
        other.checkNotClosed()
        if (this._size != other._size) return false
        return _indices.all { getByte(it) == other.getByte(it) }
    }

    override fun hashCode(): Int =
        
        checkNotClosed { indices.fold(1) { acc, it -> 31 * acc + getByte(it) } }

    override fun toString(): String =
        "ObfuscatedByteArray(size=$_size, closed=$closed)"

    override fun close() {
        closed = true
        bytes.close()
        mask.close()
    }

    private inline fun <T> checkNotClosed(block: () -> T): T {
        checkNotClosed()
        return block()
    }

    private fun checkNotClosed() {
        check(!closed) { "Closed." }
    }
}

@JvmSynthetic
fun ByteArray.toObfuscated(): ObfuscatedByteArray =
    ObfuscatedByteArray(this)

@JvmSynthetic
fun ObfuscatedByteArray.clone(): ObfuscatedByteArray =
    ObfuscatedByteArray(this)

fun CharSequence.encodeUtf8ToObfuscated(): ObfuscatedByteArray =
    encodeUtf8ToByteArray().use(ByteArray::toObfuscated)

fun CharArray.encodeUtf8ToObfuscated(): ObfuscatedByteArray =
    encodeUtf8ToByteArray().use(ByteArray::toObfuscated)

val ObfuscatedByteArray.isEmpty
    get() = size == 0