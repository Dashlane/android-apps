package com.dashlane.autofill.core

import com.dashlane.autofill.api.common.AutofillGeneratePasswordLogger
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.DomainType
import com.dashlane.hermes.generated.events.anonymous.AutofillChooseGeneratedPasswordAnonymous
import com.dashlane.password.generator.PasswordGeneratorCriteria
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLogCode75
import com.dashlane.useractivity.log.usage.UsageLogConstant
import com.dashlane.useractivity.log.usage.UsageLogRepository
import javax.inject.Inject

class AutofillGeneratePasswordLoggerImpl @Inject constructor(
    sessionManager: SessionManager,
    bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val hermesLogRepository: LogRepository
) : AutofillGeneratePasswordLogger,
    AutofillLegacyLogger(sessionManager, bySessionUsageLogRepository) {

    override fun logRefreshPassword() {
        createAndSendPasswordGeneratorLog(UsageLogConstant.PasswordGeneratorAction.refresh)
    }

    override fun logChangeLength(length: Int) {
        createAndSendPasswordGeneratorLog(
            UsageLogConstant.PasswordGeneratorAction.changeLength,
            length.toString()
        )
    }

    override fun logChangeDigit(state: Boolean) {
        val action = if (state) {
            UsageLogConstant.PasswordGeneratorAction.digitsON
        } else {
            UsageLogConstant.PasswordGeneratorAction.digitsOFF
        }
        createAndSendPasswordGeneratorLog(action)
    }

    override fun logChangeLetters(state: Boolean) {
        val action = if (state) {
            UsageLogConstant.PasswordGeneratorAction.lettersON
        } else {
            UsageLogConstant.PasswordGeneratorAction.lettersOFF
        }
        createAndSendPasswordGeneratorLog(action)
    }

    override fun logChangeSymbols(state: Boolean) {
        val action = if (state) {
            UsageLogConstant.PasswordGeneratorAction.symbolsON
        } else {
            UsageLogConstant.PasswordGeneratorAction.symbolsOFF
        }
        createAndSendPasswordGeneratorLog(action)
    }

    override fun logChangeAmbiguousChar(state: Boolean) {
        val action = if (state) {
            UsageLogConstant.PasswordGeneratorAction.ambiguousCharON
        } else {
            UsageLogConstant.PasswordGeneratorAction.ambiguousCharOFF
        }
        createAndSendPasswordGeneratorLog(action)
    }

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

    private fun createAndSendPasswordGeneratorLog(action: String?, subAction: String? = null) {
        log(
            UsageLogCode75(
                type = UsageLogConstant.ViewType.passwordGenerator,
                origin = UsageLogCode75.Origin.AUTOFILL_CREATE_ACCOUNT,
                action = action,
                subaction = subAction
            )
        )
    }
}