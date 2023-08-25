package com.dashlane.createaccount.pages.email

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AccountCreationStatus
import com.dashlane.hermes.generated.events.user.CreateAccount
import javax.inject.Inject

class CreateAccountEmailLoggerImpl @Inject constructor(
    private val logRepository: LogRepository
) : CreateAccountEmailLogger {

    override fun logEmptyEmail() {
        logRepository.queueEvent(
            CreateAccount(
                isMarketingOptIn = false,
                status = AccountCreationStatus.ERROR_NOT_VALID_EMAIL
            )
        )
    }

    override fun logInvalidEmailLocal() {
        logRepository.queueEvent(
            CreateAccount(
                isMarketingOptIn = false,
                status = AccountCreationStatus.ERROR_NOT_VALID_EMAIL
            )
        )
    }

    override fun logInvalidEmailServer() {
        logRepository.queueEvent(
            CreateAccount(
                isMarketingOptIn = false,
                status = AccountCreationStatus.ERROR_NOT_VALID_EMAIL
            )
        )
    }

    override fun logAccountExists() {
        logRepository.queueEvent(
            CreateAccount(
                isMarketingOptIn = false,
                status = AccountCreationStatus.ERROR_ACCOUNT_ALREADY_EXISTS
            )
        )
    }
}
