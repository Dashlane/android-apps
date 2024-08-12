package com.dashlane.utils

import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import kotlin.random.Random

class PasswordScrambler @Inject constructor() {
    private val random = Random(Instant.now().toEpochMilli())

    suspend fun runScramble(password: String, block: (String) -> Unit) {
        val start = Instant.now()
        val length = password.length
        var scrambleSLength = SCRAMBLE_LENGTH
        val stepDelay: Long = (SCRAMBLE_DURATION_MILLIS / (length + SCRAMBLE_LENGTH)).toLong()

        
        while (Duration.between(start, Instant.now()) < Duration.ofMillis(100)) {
            block(generateRandomPassword(SCRAMBLE_LENGTH))
            delay(START_SCRAMBLE_DELAY_MILLIS)
        }

        
        if (length <= SCRAMBLE_LENGTH) {
            for (i in (length..SCRAMBLE_LENGTH).reversed()) {
                block(generateRandomPassword(i))
                delay(stepDelay)
            }
            scrambleSLength = length
        }

        
        password.forEachIndexed { index, _ ->
            val fakePassword = if (index < scrambleSLength) {
                generateRandomPassword(scrambleSLength - index)
            } else {
                generateRandomPassword(1)
            }
            block(password.subSequence(0, index).toString() + fakePassword)
            delay(stepDelay)
        }
        block(password)
    }

    private fun generateRandomPassword(size: Int? = null): String {
        val length = size ?: random.nextInt(SCRAMBLE_LENGTH)
        return (1..length)
            .map { Random.nextInt(0, charPool.size) }
            .map(charPool::get).joinToString("")
    }

    companion object {
        private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        private const val SCRAMBLE_LENGTH = 16
        const val START_SCRAMBLE_DELAY_MILLIS = 15L
        private const val SCRAMBLE_DURATION_MILLIS = 300
    }
}