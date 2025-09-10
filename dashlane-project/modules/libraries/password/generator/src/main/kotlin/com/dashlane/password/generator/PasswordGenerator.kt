package com.dashlane.password.generator

import java.security.SecureRandom
import kotlin.random.Random
import kotlin.random.asKotlinRandom

interface PasswordGenerator {
    fun generate(
        length: Int = DEFAULT_PASSWORD_LENGTH,
        digits: Boolean = true,
        letters: Boolean = true,
        symbols: Boolean = true,
        ambiguousChars: Boolean = true,
    ): String

    companion object {
        const val DEFAULT_PASSWORD_LENGTH = 16

        const val ALPHABET_NUMERALS = "1234567890"

        const val ALPHABET_NUMERALS_DISTINGUISHABLE = "3456789"

        const val ALPHABET_LOWER_ALPHA = "abcdefghijklmnopqrstuvwxyz"

        const val ALPHABET_UPPER_ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

        const val ALPHABET_LOWER_ALPHA_DISTINGUISHABLE = "abcdefghijkmnopqrstxyz"

        const val ALPHABET_UPPER_ALPHA_DISTINGUISHABLE = "ABCDEFGHJKLMNPQRSTXY"

        const val ALPHABET_SYMBOLS = "&@\$!#?"
    }
}

fun PasswordGenerator.generate(criteria: PasswordGeneratorCriteria) = generate(
    criteria.length,
    criteria.digits,
    criteria.letters,
    criteria.symbols,
    criteria.ambiguousChars,
)

@Suppress("unused")
fun PasswordGenerator(random: Random = SecureRandom().asKotlinRandom()): PasswordGenerator =
    PasswordGeneratorImpl(random)

private const val MAX_RETRIES = 1000

private class PasswordGeneratorImpl(private val random: Random) : PasswordGenerator {
    override fun generate(
        length: Int,
        digits: Boolean,
        letters: Boolean,
        symbols: Boolean,
        ambiguousChars: Boolean,
    ): String {
        require(length >= 4) { "The length of the password cannot be below 4 characters." }
        require(digits || letters || symbols) { "You need to have at least one kind of character selected." }

        val alphabet =
            getAlphabet(digits, symbols, letters, ambiguousChars)

        val password = CharArray(length)
        repeat(MAX_RETRIES) {
            
            password.fill { alphabet.randomChar(random) }
            if (password.containsAtLeast(digits = digits, letters = letters, symbols = symbols)) {
                
                return String(password)
            }
        }

        throw PasswordGeneratorException("Unable to generate a password with the given criteria.")
    }
}

@Suppress("ComplexCondition")
private fun CharArray.containsAtLeast(
    digits: Boolean,
    letters: Boolean,
    symbols: Boolean,
): Boolean {
    
    var expectingDigit = digits
    var expectingSymbol = symbols
    var expectingLowerCase = letters
    var expectingUpperCase = letters

    this.forEach { char ->
        
        when {
            expectingDigit && char in PasswordGenerator.ALPHABET_NUMERALS -> expectingDigit = false
            expectingSymbol && char in PasswordGenerator.ALPHABET_SYMBOLS -> expectingSymbol = false
            expectingLowerCase && char in PasswordGenerator.ALPHABET_LOWER_ALPHA -> expectingLowerCase = false
            expectingUpperCase && char in PasswordGenerator.ALPHABET_UPPER_ALPHA -> expectingUpperCase = false
        }
        if (!expectingDigit && !expectingSymbol && !expectingLowerCase && !expectingUpperCase) {
            
            return true
        }
    }
    return false 
}

private fun getAlphabet(
    digits: Boolean,
    symbols: Boolean,
    letters: Boolean,
    ambiguousChars: Boolean,
): String = buildString {
    if (digits) {
        append(getNumeralsAlphabet(ambiguousChars))
    }
    if (symbols) {
        append(PasswordGenerator.ALPHABET_SYMBOLS)
    }
    if (letters) {
        append(getAlphaLowerAlphabet(ambiguousChars))
        append(getAlphaUpperAlphabet(ambiguousChars))
    }
}

private fun getNumeralsAlphabet(ambiguousChars: Boolean): String =
    if (ambiguousChars) {
        PasswordGenerator.ALPHABET_NUMERALS
    } else {
        PasswordGenerator.ALPHABET_NUMERALS_DISTINGUISHABLE
    }

private fun getAlphaLowerAlphabet(ambiguousChars: Boolean): String =
    if (ambiguousChars) {
        PasswordGenerator.ALPHABET_LOWER_ALPHA
    } else {
        PasswordGenerator.ALPHABET_LOWER_ALPHA_DISTINGUISHABLE
    }

private fun getAlphaUpperAlphabet(ambiguousChars: Boolean): String =
    if (ambiguousChars) {
        PasswordGenerator.ALPHABET_UPPER_ALPHA
    } else {
        PasswordGenerator.ALPHABET_UPPER_ALPHA_DISTINGUISHABLE
    }

private inline fun CharArray.fill(block: (Int) -> Char) {
    for (i in indices) {
        this[i] = block(i)
    }
}

private fun String.randomChar(random: Random): Char =
    this[random.nextInt(length)]