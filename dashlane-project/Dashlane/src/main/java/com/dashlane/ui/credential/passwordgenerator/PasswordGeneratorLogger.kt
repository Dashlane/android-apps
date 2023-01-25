package com.dashlane.ui.credential.passwordgenerator

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.events.user.GeneratePassword
import com.dashlane.password.generator.PasswordGeneratorCriteria
import com.dashlane.useractivity.log.usage.UsageLogCode75
import com.dashlane.useractivity.log.usage.UsageLogConstant
import com.dashlane.useractivity.log.usage.UsageLogRepository

class PasswordGeneratorLogger(
    private val usageLogRepository: UsageLogRepository?,
    private val trackingLogRepository: LogRepository
) {

    fun log(subType: String?, action: String?, subAction: String? = null) {
        usageLogRepository?.enqueue(
            UsageLogCode75(
                type = UsageLogConstant.ViewType.passwordGenerator,
                subtype = subType,
                action = action,
                subaction = subAction
            )
        )
    }

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