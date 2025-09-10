package com.dashlane.cryptography

import okio.ByteString.Companion.toByteString
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.random.Random
import kotlin.test.assertEquals

class ObfuscatedByteArrayPlusTest() {
    @ParameterizedTest()
    @MethodSource("data")
    fun plusBytes(x: ByteArray, y: ByteArray) {
        val obfuscatedX = x.toObfuscated()

        val obfuscatedZ = obfuscatedX + y
        obfuscatedX.close()
        val z = obfuscatedZ.toByteArray()
        obfuscatedX.close()

        val expectedZ = x + y
        assertEquals(expectedZ.toByteString(), z.toByteString())
    }

    @ParameterizedTest()
    @MethodSource("data")
    fun plusObfuscated(x: ByteArray, y: ByteArray) {
        val obfuscatedX = x.toObfuscated()
        val obfuscatedY = y.toObfuscated()

        val obfuscatedZ = obfuscatedX + obfuscatedY
        obfuscatedX.close()
        obfuscatedY.close()
        val z = obfuscatedZ.toByteArray()
        obfuscatedZ.close()

        val expectedZ = x + y
        assertEquals(expectedZ.toByteString(), z.toByteString())
    }

    companion object {
        val random = Random(5709385206457190844)

        @JvmStatic
        fun data() = byteArrayPairs.map { (x, y) -> Arguments.of(x, y) }

        @Suppress("ktlint:standard:max-line-length")
        private val byteArrayPairs
            get() = listOf(
                ByteArray(0) to ByteArray(0),
                ByteArray(0) to random.nextBytes(4),
                random.nextBytes(3) to ByteArray(0),
                ByteArray(8) to ByteArray(8),
                "kEL4nsgdxa2fVta4DnAiew==".decodeBase64ToByteArray() to "3rMrWr7soirzuosKyawNJU2RgeNbeGCQbyEiREBnJrcP3RXK".decodeBase64ToByteArray(),
                random.nextBytes(525) to random.nextBytes(850),
            )
    }
}