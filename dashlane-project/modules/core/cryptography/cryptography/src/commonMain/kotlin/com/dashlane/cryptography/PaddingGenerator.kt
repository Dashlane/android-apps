package com.dashlane.cryptography

import kotlin.random.Random

fun interface PaddingGenerator {
    fun generateRandomPadding(size: Int): ByteArray
}

@Suppress("FunctionName")
fun PaddingGenerator(
    random: Random,
): PaddingGenerator = PaddingGeneratorImpl(
    random,
)

private class PaddingGeneratorImpl(private val random: Random) : PaddingGenerator {
    override fun generateRandomPadding(size: Int) =
        random.nextBytes(size)
}