package com.dashlane.createaccount

import com.dashlane.authentication.AuthenticationAccountAlreadyExistsException
import com.dashlane.authentication.AuthenticationEmptyEmailException
import com.dashlane.authentication.AuthenticationException
import com.dashlane.authentication.AuthenticationInvalidEmailException
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AccountCreationStatus
import com.dashlane.hermes.generated.events.user.CreateAccount
import com.dashlane.util.log.AttributionLogData
import javax.inject.Inject

class CreateAccountLogger @Inject constructor(
    private val logRepository: LogRepository
) {
    fun logError(e: AuthenticationException) {
        val accountCreationStatus = when (e) {
            is AuthenticationInvalidEmailException, is AuthenticationEmptyEmailException -> {
                AccountCreationStatus.ERROR_NOT_VALID_EMAIL
            }
            is AuthenticationAccountAlreadyExistsException -> {
                AccountCreationStatus.ERROR_ACCOUNT_ALREADY_EXISTS
            }
            else -> null
        }
        if (accountCreationStatus != null) {
            logRepository.queueEvent(
                CreateAccount(
                    isMarketingOptIn = false,
                    status = accountCreationStatus
                )
            )
        }
    }

    fun logSuccess(attributionLogData: AttributionLogData?) {
        logRepository.queueEvent(
            CreateAccount(
                isMarketingOptIn = attributionLogData?.isMarketingOptIn ?: false,
                androidMarketing = attributionLogData?.androidAttribution,
                status = AccountCreationStatus.SUCCESS
            )
        )
    }
}