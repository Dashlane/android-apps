package com.dashlane.createaccount.pages.confirmpassword

import com.dashlane.login.dagger.TrackingId
import com.dashlane.useractivity.log.install.InstallLogCode69
import com.dashlane.useractivity.log.install.InstallLogRepository
import javax.inject.Inject

class CreateAccountConfirmPasswordLoggerImpl @Inject constructor(
    @TrackingId private val trackingId: String,
    private val installLogRepository: InstallLogRepository
) : CreateAccountConfirmPasswordLogger {

    override var origin: String? = null

    override fun logLand(gdprApprovalRequired: Boolean) {
        if (gdprApprovalRequired) {
            log("9", "land", "nextConsentPage")
        } else {
            log("9", "land")
        }
    }

    override fun logBack() =
        log("11.1", "back")

    override fun logPasswordVisibilityToggle(passwordShown: Boolean) =
        log(if (passwordShown) "10.1" else "10.2", if (passwordShown) "show" else "hide")

    override fun logPasswordError() =
        log("11.2", "next", "incorrectPassword")

    override fun logNetworkError(step: String) =
        log("11.2", "next", "networkError$step")

    override fun logCreateAccountSuccess() =
        log("11.3", "next", "success")

    private fun log(step: String, action: String, subAction: String? = null) {
        installLogRepository.enqueue(
            InstallLogCode69(
                type = InstallLogCode69.Type.CREATE_ACCOUNT,
                subType = "enterAgainMP",
                action = if (origin != null) "${action}_$origin" else action,
                loginSession = trackingId,
                subStep = step,
                subAction = subAction
            )
        )
    }
}