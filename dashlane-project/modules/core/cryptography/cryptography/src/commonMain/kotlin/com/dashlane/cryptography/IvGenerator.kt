package com.dashlane.cryptography

import kotlin.random.Random

fun interface IvGenerator {
    fun generateRandomIv(size: Int): ByteArray
}

@Suppress("FunctionName")
fun IvGenerator(
    random: Random,
): IvGenerator = IvGeneratorImpl(
    random,
)

private class IvGeneratorImpl(private val random: Random) : IvGenerator {
    override fun generateRandomIv(size: Int) =
        random.nextBytes(size)
}