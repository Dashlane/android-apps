package com.dashlane.autofill.generatepassword

import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.password.generator.PasswordGeneratorCriteria

interface AutofillGeneratePasswordLogger {

    fun logGeneratePassword(criteria: PasswordGeneratorCriteria, domainForLogs: Domain)
}