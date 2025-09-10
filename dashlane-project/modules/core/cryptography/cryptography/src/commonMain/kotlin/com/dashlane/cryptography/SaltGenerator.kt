package com.dashlane.cryptography

import kotlin.random.Random

fun interface SaltGenerator {
    fun generateRandomSalt(size: Int): ByteArray
}

fun SaltGenerator.generateFixedSalt(marker: CryptographyMarker): CryptographyFixedSalt? =
    if (marker.isFixedSalt) {
        CryptographyFixedSalt(generateRandomSalt(marker.saltLength))
    } else {
        null
    }

@Suppress("FunctionName")
fun SaltGenerator(
    random: Random,
): SaltGenerator = SaltGeneratorImpl(
    random,
)

private class SaltGeneratorImpl(private val random: Random) : SaltGenerator {
    override fun generateRandomSalt(size: Int) =
        random.nextBytes(size)
}