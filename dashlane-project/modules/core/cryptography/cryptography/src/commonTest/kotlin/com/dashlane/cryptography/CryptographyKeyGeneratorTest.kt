package com.dashlane.cryptography

import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertEquals

class CryptographyKeyGeneratorTest {
    private val random = Random(11248)

    @Test
    fun cryptographyKeyGenerator_32() {
        val keyGenerator = CryptographyKeyGenerator(random)
        val key = keyGenerator.generateRaw32()

        assertEquals(32, key.toByteArray().size)
        assertEquals(CryptographyKey.Type.RAW_32, key.type)
    }

    @Test
    fun cryptographyKeyGenerator_64() {
        val keyGenerator = CryptographyKeyGenerator(random)
        val key = keyGenerator.generateRaw64()

        assertEquals(64, key.toByteArray().size)
        assertEquals(CryptographyKey.Type.RAW_64, key.type)
    }
}