package com.dashlane.ui.credential.passwordgenerator

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.events.user.GeneratePassword
import com.dashlane.password.generator.PasswordGeneratorCriteria

class PasswordGeneratorLogger(
    private val trackingLogRepository: LogRepository
) {

    fun logPasswordGenerate(criteria: PasswordGeneratorCriteria) {
        trackingLogRepository.queueEvent(
            GeneratePassword(
                hasSimilar = criteria.ambiguousChars,
                hasLetters = criteria.letters,
                length = criteria.length,
                hasSymbols = criteria.symbols,
                hasDigits = criteria.digits
            )
        )
    }
}