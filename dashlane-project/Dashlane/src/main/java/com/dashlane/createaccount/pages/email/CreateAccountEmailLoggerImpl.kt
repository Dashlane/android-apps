package com.dashlane.createaccount.pages.email

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AccountCreationStatus
import com.dashlane.hermes.generated.events.user.CreateAccount
import com.dashlane.login.dagger.TrackingId
import com.dashlane.useractivity.log.install.InstallLogCode69
import com.dashlane.useractivity.log.install.InstallLogRepository
import javax.inject.Inject

class CreateAccountEmailLoggerImpl @Inject constructor(
    @TrackingId private val trackingId: String,
    private val installLogRepository: InstallLogRepository,
    private val logRepository: LogRepository
) : CreateAccountEmailLogger {
    override fun logLand() =
        log("3", "land", "one_step_account_creation_control")

    override fun logBack() =
        log("4.1", "back")

    override fun logEmptyEmail() {
        logError("emptyField")
        logRepository.queueEvent(
            CreateAccount(
                isMarketingOptIn = false,
                status = AccountCreationStatus.ERROR_NOT_VALID_EMAIL
            )
        )
    }

    override fun logInvalidEmailLocal() {
        logError("invalidEmailLocal")
        logRepository.queueEvent(
            CreateAccount(
                isMarketingOptIn = false,
                status = AccountCreationStatus.ERROR_NOT_VALID_EMAIL
            )
        )
    }

    override fun logInvalidEmailServer() {
        logError("invalidEmailServer")
        logRepository.queueEvent(
            CreateAccount(
                isMarketingOptIn = false,
                status = AccountCreationStatus.ERROR_NOT_VALID_EMAIL
            )
        )
    }

    override fun logUnlikelyEmail() =
        log("4.4", "next", "unlikelyEmail")

    override fun logAccountExists() {
        logError("accountExists")
        logRepository.queueEvent(
            CreateAccount(
                isMarketingOptIn = false,
                status = AccountCreationStatus.ERROR_ACCOUNT_ALREADY_EXISTS
            )
        )
    }

    override fun logNetworkError() =
        logError("networkError")

    private fun logError(value: String) {
        log("4.3", "next", value)
    }

    override fun logValidEmail() =
        log("4.2", "next", "success")

    override fun logShowConfirmEmail() =
        logPopup("5", "show")

    override fun logCancelConfirmEmail() =
        logPopup("5.1", "cancel")

    override fun logConfirmEmail() =
        logPopup("5.2", "continue")

    private fun log(step: String, action: String, subAction: String? = null) {
        log("enterEmail", action, subAction, step)
    }

    private fun logPopup(step: String, action: String, subAction: String? = null) {
        log("unlikelyEmail", action, subAction, step)
    }

    private fun log(subType: String, action: String, subAction: String? = null, step: String) {
        installLogRepository.enqueue(
            InstallLogCode69(
                type = InstallLogCode69.Type.CREATE_ACCOUNT,
                subType = subType,
                action = action,
                loginSession = trackingId,
                subAction = subAction,
                subStep = step
            )
        )
    }
}