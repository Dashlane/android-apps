package com.dashlane.createaccount

import com.dashlane.authentication.AuthenticationAccountAlreadyExistsException
import com.dashlane.authentication.AuthenticationEmptyEmailException
import com.dashlane.authentication.AuthenticationException
import com.dashlane.authentication.AuthenticationInvalidEmailException
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AccountCreationStatus
import com.dashlane.hermes.generated.events.user.CreateAccount
import com.dashlane.login.dagger.TrackingId
import com.dashlane.useractivity.log.install.InstallLogCode69
import com.dashlane.useractivity.log.install.InstallLogRepository
import com.dashlane.util.log.AttributionLogData
import javax.inject.Inject

class CreateAccountLogger @Inject constructor(
    @TrackingId private val trackingId: String,
    private val installLogRepository: InstallLogRepository,
    private val logRepository: LogRepository
) {
    fun logCreateAccountClick(termsState: AccountCreator.TermsState) {
        val checkedConditions = listOf(
            termsState.conditions to "conditions",
            termsState.offers to "offer"
        )
        val checkedString = checkedConditions
            .filter(Pair<Boolean, String>::first)
            .map(Pair<Boolean, String>::second)
            .joinToString(",")
        log("next", checkedString)
    }

    fun logError(e: AuthenticationException) {
        log("error")
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
        log("success")
        logRepository.queueEvent(
            CreateAccount(
                isMarketingOptIn = attributionLogData?.isMarketingOptIn ?: false,
                androidMarketing = attributionLogData?.androidAttribution,
                status = AccountCreationStatus.SUCCESS
            )
        )
    }

    private fun log(action: String, subAction: String? = null) {
        installLogRepository.enqueue(
            InstallLogCode69(
                type = InstallLogCode69.Type.CREATE_ACCOUNT,
                subType = "consentPage",
                action = action,
                loginSession = trackingId,
                subStep = "11.4",
                subAction = subAction
            )
        )
    }
}