package com.dashlane.cryptography

import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SaltGeneratorTest {
    private val random = Random(60186739)

    @Test
    fun generateFixedSaltArgon2d() {
        val marker = CryptographyMarker.Flexible.Defaults.argon2d

        val saltGenerator = SaltGenerator(random)
        val fixedSalt = saltGenerator.generateFixedSalt(marker)

        assertNotNull(fixedSalt)
        assertEquals(16, fixedSalt.data.size)
    }

    @Test
    fun generateFixedSaltNoDerivation() {
        val marker = CryptographyMarker.Flexible.Defaults.noDerivation

        val saltGenerator = SaltGenerator(random)
        val fixedSalt = saltGenerator.generateFixedSalt(marker)

        assertNull(fixedSalt)
    }

    @Test
    fun generateFixedSaltKwc3() {
        val marker = CryptographyMarker.Kwc3

        val saltGenerator = SaltGenerator(random)
        val fixedSalt = saltGenerator.generateFixedSalt(marker)

        assertNull(fixedSalt)
    }
}