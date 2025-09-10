package com.dashlane.cryptography

import okio.Buffer
import okio.ByteString.Companion.toByteString
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ObfuscatedByteArrayBytesTest() {
    @ParameterizedTest
    @MethodSource("data")
    fun bytes(bytes: ByteArray) {
        val obfuscated = bytes.toObfuscated()
        val byteArray = obfuscated.toByteArray()

        assertEquals(bytes.toByteString(), byteArray.toByteString())
    }

    @ParameterizedTest
    @MethodSource("data")
    fun close(bytes: ByteArray) {
        val obfuscated = bytes.toObfuscated()
        obfuscated.close()

        assertFailsWith<IllegalStateException> { obfuscated.size }
        assertFailsWith<IllegalStateException> { obfuscated.toByteArray() }
        assertFailsWith<IllegalStateException> { obfuscated.toByteArray(0, bytes.size) }
        assertFailsWith<IllegalStateException> { obfuscated.clone() }
    }

    @ParameterizedTest
    @MethodSource("data")
    fun serialize(bytes: ByteArray) {
        val buffer = Buffer()
        ObjectOutputStream(buffer.outputStream()).use {
            it.writeObject(bytes.toObfuscated())
        }
        val obfuscatedBytes = ObjectInputStream(buffer.inputStream()).use {
            it.readObject() as ObfuscatedByteArray
        }

        assertEquals(bytes.toByteString(), obfuscatedBytes.toByteArray().toByteString())
    }

    companion object {
        private val random = Random(4704191036104612055)

        @JvmStatic
        fun data() = List(size = 16) { Arguments.of(random.nextBytes(size = it)) }
    }
}