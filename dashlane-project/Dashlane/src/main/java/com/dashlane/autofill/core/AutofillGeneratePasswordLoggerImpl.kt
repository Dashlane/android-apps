package com.dashlane.autofill.core

import com.dashlane.autofill.generatepassword.AutofillGeneratePasswordLogger
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.DomainType
import com.dashlane.hermes.generated.events.anonymous.AutofillChooseGeneratedPasswordAnonymous
import com.dashlane.password.generator.PasswordGeneratorCriteria
import javax.inject.Inject

class AutofillGeneratePasswordLoggerImpl @Inject constructor(
    private val hermesLogRepository: LogRepository
) : AutofillGeneratePasswordLogger {

    override fun logGeneratePassword(criteria: PasswordGeneratorCriteria, domainForLogs: Domain) {
        val isNativeApp = when (domainForLogs.type) {
            DomainType.APP -> true
            DomainType.WEB -> false
        }
        hermesLogRepository.queueEvent(
            AutofillChooseGeneratedPasswordAnonymous(
                hasSimilar = criteria.ambiguousChars,
                hasLetters = criteria.letters,
                domain = domainForLogs,
                length = criteria.length,
                hasSymbols = criteria.symbols,
                isNativeApp = isNativeApp,
                hasDigits = criteria.digits
            )
        )
    }
}