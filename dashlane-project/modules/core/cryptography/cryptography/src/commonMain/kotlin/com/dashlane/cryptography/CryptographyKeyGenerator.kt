package com.dashlane.cryptography

import kotlin.random.Random

interface CryptographyKeyGenerator {
    fun generateRaw32(): CryptographyKey.Raw32

    fun generateRaw64(): CryptographyKey.Raw64
}

@Suppress("FunctionName")
fun CryptographyKeyGenerator(
    random: Random,
): CryptographyKeyGenerator = CryptographyKeyGeneratorImpl(
    random,
)

private class CryptographyKeyGeneratorImpl(private val random: Random) : CryptographyKeyGenerator {
    override fun generateRaw32() =
        CryptographyKey.ofBytes32(random.nextBytes(32))

    override fun generateRaw64() =
        CryptographyKey.ofBytes64(random.nextBytes(64))
}