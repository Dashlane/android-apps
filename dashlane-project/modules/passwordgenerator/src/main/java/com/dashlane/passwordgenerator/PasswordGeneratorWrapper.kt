package com.dashlane.passwordgenerator

import com.dashlane.password.generator.PasswordGeneratorCriteria
import com.dashlane.passwordstrength.PasswordStrengthScore
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject



interface PasswordGeneratorWrapper {
    

    var passwordLength: Int

    

    var isUsingAmbiguousChar: Boolean

    

    var isUsingSymbols: Boolean

    

    var isUsingLetters: Boolean

    

    var isUsingDigits: Boolean

    

    suspend fun generatePassword(criteria: PasswordGeneratorCriteria?): Result

    

    suspend fun saveToPasswordHistory(
        password: String,
        authDomain: String,
        authId: String
    ): VaultItem<SyncObject.GeneratedPassword>

    data class Result(val password: String, val passwordStrength: PasswordStrengthScore?)
}

var PasswordGeneratorWrapper.criteria
    get() = PasswordGeneratorCriteria(
        length = passwordLength,
        digits = isUsingDigits,
        symbols = isUsingSymbols,
        letters = isUsingLetters,
        ambiguousChars = isUsingAmbiguousChar
    )
    set(value) {
        passwordLength = value.length
        isUsingDigits = value.digits
        isUsingLetters = value.letters
        isUsingSymbols = value.symbols
        isUsingAmbiguousChar = value.ambiguousChars
    }