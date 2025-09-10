package com.dashlane.xml

import java.io.Closeable
import java.io.Serializable
import java.security.SecureRandom

@Suppress("SerialVersionUIDInSerializableClass")
class XmlObfuscatedValue : CharSequence, Closeable, Serializable {
    private val obfuscationKey: IntArray
    private val obfuscatedContents: IntArray
    private var closed = false

    constructor(charArray: CharArray) {
        this.obfuscationKey = randomIntArray(charArray.size)
        this.obfuscatedContents = IntArray(charArray.size) { charArray[it].code xor obfuscationKey[it] }
    }

    constructor(charSequence: CharSequence) {
        this.obfuscationKey = randomIntArray(charSequence.length)
        this.obfuscatedContents = IntArray(charSequence.length) { charSequence[it].code xor obfuscationKey[it] }
    }

    override val length: Int
        get() = checkClosed { obfuscatedContents.size }

    override operator fun get(index: Int): Char = checkClosed {
        getAt(index).toChar()
    }

    private fun getAt(index: Int): Int =
        obfuscatedContents[index] xor obfuscationKey[index]

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = checkClosed {
        require(startIndex >= 0) { "{startIndex $startIndex} < 0" }
        require(startIndex <= endIndex) { "{startIndex $startIndex} > {endIndex $endIndex}" }
        require(endIndex <= length) { "{endIndex $endIndex} > {length $length}" }
        SubSequence(startIndex, endIndex)
    }

    override fun close() {
        obfuscationKey.fill(0)
        obfuscatedContents.fill(0)
        closed = true
    }

    fun equalsString(other: String?): Boolean {
        other ?: return false
        if (length != other.length) return false
        return indices.all { get(it) == other[it] }
    }

    override fun hashCode(): Int = checkClosed {
        var result = 1
        for (i in indices) result = 31 * result + getAt(i)
        return result
    }

    override fun equals(other: Any?): Boolean = checkClosed {
        if (this === other) return true
        if (other !is XmlObfuscatedValue) return false

        val length = this.length
        if (length != other.length) return false
        for (i in 0 until length) {
            if (getAt(i) != other.getAt(i)) return false
        }
        return true
    }

    override fun toString(): String =
        toCharArray().use { String(it) }

    fun toCharArray(): CharArray = checkClosed {
        CharArray(length) { get(it) }
    }

    private inline fun <T> checkClosed(block: () -> T): T {
        checkClosed()
        return block()
    }

    private fun checkClosed() = check(!closed) { "closed" }

    private inner class SubSequence(private val startIndex: Int, val endIndex: Int) : CharSequence {
        override val length: Int
            get() = checkClosed { endIndex - startIndex }

        override fun get(index: Int): Char {
            require(index >= 0) { "{index $index} < 0" }
            require(index < length) { "{index $index} > {length $length}" }
            return this@XmlObfuscatedValue[startIndex + index]
        }

        override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = checkClosed {
            require(startIndex >= 0) { "{startIndex $startIndex} < 0" }
            require(startIndex <= endIndex) { "{startIndex $startIndex} > {endIndex $endIndex}" }
            require(endIndex <= length) { "{endIndex $endIndex} > {length $length}" }
            return SubSequence(this.startIndex + startIndex, this.startIndex + endIndex)
        }

        override fun toString(): String =
            toCharArray().use { String(it) }

        private fun toCharArray(): CharArray = checkClosed {
            CharArray(length) { this@XmlObfuscatedValue[startIndex + it] }
        }
    }
}

private val random = SecureRandom()

private fun randomIntArray(size: Int): IntArray =
    IntArray(size) { random.nextInt() }

private inline fun <T> CharArray.use(block: (CharArray) -> T) =
    try {
        block(this)
    } finally {
        fill('\u0000')
    }