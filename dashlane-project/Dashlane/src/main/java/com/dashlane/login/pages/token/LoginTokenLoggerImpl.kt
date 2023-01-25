package com.dashlane.login.pages.token

import com.dashlane.account.UserSecuritySettings
import com.dashlane.login.dagger.TrackingId
import com.dashlane.useractivity.log.install.InstallLogRepository
import com.dashlane.useractivity.log.install.InstallLogCode17
import com.dashlane.useractivity.log.install.InstallLogCode69
import javax.inject.Inject

class LoginTokenLoggerImpl @Inject constructor(
    @TrackingId private val trackingId: String,
    private val installLogRepository: InstallLogRepository
) : LoginTokenLogger {

    override fun logLand() =
        logToken("5", "land")

    override fun logBack() =
        logToken("6.1", "back")

    override fun logWhereIsMyCodeClick() =
        logToken("6.2", "whereIsMyCode")

    override fun logWhereIsMyCodeAppear() =
        logTokenPopup("6.3", "show")

    override fun logCloseWhereIsMyCode() =
        logTokenPopup("6.4", "close")

    override fun logDismissWhereIsMyCode() =
        logTokenPopup("6.5", "back")

    override fun logResendCode() =
        logTokenPopup("6.6", "resend")

    override fun logInvalidToken(autoSend: Boolean) =
        logToken(getSendErrorStep(autoSend), getSendAction(autoSend), subAction = "invalidToken")

    override fun logNetworkError(autoSend: Boolean) =
        logToken(getSendErrorStep(autoSend), getSendAction(autoSend), subAction = "networkError")

    override fun logTokenSuccess(autoSend: Boolean) =
        logToken(getSendSuccessStep(autoSend), getSendAction(autoSend), subAction = "success")

    private fun getSendSuccessStep(autoSend: Boolean) = if (autoSend) "7.2.2" else "7.2.1"
    private fun getSendErrorStep(autoSend: Boolean) = if (autoSend) "7.1.2" else "7.1.1"
    private fun getSendAction(autoSend: Boolean) = if (autoSend) "nextAutomatic" else "next"

    private fun logToken(step: String, action: String, subAction: String? = null) {
        log(step, "token", action, subAction)
    }

    override fun logTokenViaDeepLink() {
        installLogRepository.enqueue(InstallLogCode17(subStep = "4.1"))
    }

    private fun logTokenPopup(step: String, action: String, subAction: String? = null) {
        log(step, "tokenPopup", action, subAction)
    }

    private fun log(step: String, value: String, action: String, subAction: String? = null) {
        installLogRepository.enqueue(
            InstallLogCode69(
                loginSession = trackingId,
                type = InstallLogCode69.Type.LOGIN,
                subType = value,
                action = action,
                deviceStatus = InstallLogCode69.DeviceStatus.NEW,
                accountType = UserSecuritySettings(isToken = true).asString(),
                subAction = subAction,
                subStep = step
            )
        )
    }
}