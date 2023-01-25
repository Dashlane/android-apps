package com.dashlane.createaccount.pages.choosepassword

import com.dashlane.login.dagger.TrackingId
import com.dashlane.useractivity.log.install.InstallLogRepository
import com.dashlane.useractivity.log.install.InstallLogCode69
import javax.inject.Inject

class CreateAccountChoosePasswordLoggerImpl @Inject constructor(
    @TrackingId private val trackingId: String,
    private val installLogRepository: InstallLogRepository
) : CreateAccountChoosePasswordLogger {

    override fun logLand() =
        log("6", "land")

    override fun logBack() =
        log("8.1", "back")

    override fun logPasswordVisibilityToggle(passwordShown: Boolean) =
        log(if (passwordShown) "7.1" else "7.2", if (passwordShown) "show" else "hide")

    override fun logEmptyPassword() =
        log("8.2", "next", "emptyField")

    override fun logInsufficientPassword() =
        log("8.2", "next", "missingCriteria")

    override fun logPasswordChosen() =
        log("8.3", "next", "success")

    override fun logShowPasswordTips() =
        log("7.3", "mp_tips")

    private fun log(step: String, action: String, subAction: String? = null) {
        installLogRepository.enqueue(
            InstallLogCode69(
                type = InstallLogCode69.Type.CREATE_ACCOUNT,
                subType = "enterMP",
                action = action,
                loginSession = trackingId,
                subAction = subAction,
                subStep = step
            )
        )
    }
}