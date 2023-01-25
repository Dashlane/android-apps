package com.dashlane.autofill.api.common

import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.password.generator.PasswordGeneratorCriteria

interface AutofillGeneratePasswordLogger {

    

    fun logRefreshPassword()

    

    fun logChangeLength(length: Int)

    

    fun logChangeDigit(state: Boolean)

    

    fun logChangeLetters(state: Boolean)

    

    fun logChangeSymbols(state: Boolean)

    

    fun logChangeAmbiguousChar(state: Boolean)

    

    fun logGeneratePassword(criteria: PasswordGeneratorCriteria, domainForLogs: Domain)
}