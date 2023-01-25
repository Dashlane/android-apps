package com.dashlane.login.pages.totp

import com.dashlane.account.UserSecuritySettings
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.VerificationMode
import com.dashlane.login.LoginLogger
import com.dashlane.login.dagger.TrackingId
import com.dashlane.useractivity.log.install.InstallLog
import com.dashlane.useractivity.log.install.InstallLogCode17
import com.dashlane.useractivity.log.install.InstallLogCode69
import com.dashlane.useractivity.log.install.InstallLogRepository
import javax.inject.Inject

class LoginTotpLoggerImpl(
    private val installLogRepository: InstallLogRepository,
    private val trackingId: String,
    private val deviceStatus: InstallLogCode69.DeviceStatus,
    private val accountTypeFlags: String,
    private val loginLogger: LoginLogger,
    private val verificationMode: VerificationMode
) : LoginTotpLogger {

    override fun logLand() =
        logTotp("5", "land")

    override fun logBack() =
        logTotp("6.1.1", "back")

    override fun logDuoClick() =
        logDuo("7", "useDuo")

    override fun logDuoAppear() =
        logDuo("8", "show")

    override fun logDuoCancel() =
        logDuo("9.2", "cancel")

    override fun logDuoTimeout() =
        logDuo("9.4", "requestExpired")

    override fun logDuoNetworkError() =
        logDuo("9.6", "networkError")

    override fun logDuoDenied() =
        logDuo("9.5", "requestDenied")

    override fun logDuoSuccess() =
        logDuo("9.3", "success")

    override fun logU2fPopupClick() =
        logTotp("6.1.4", "show")

    override fun logU2fSuccess() =
        logTotp("6.1.5", "success")

    override fun logExecuteU2fAuthentication() {
        log(InstallLogCode17(subStep = "3.21"))
    }

    override fun logFailedSignU2FTag() {
        log(InstallLogCode17(subStep = "3.21.4"))
    }

    override fun logSuccessSignU2FTag() {
        log(InstallLogCode17(subStep = "3.21.3"))
    }

    override fun logInvalidTotp(autoSend: Boolean) {
        loginLogger.logWrongOtp(verificationMode)
        logTotp(getSendErrorStep(autoSend), getSendAction(autoSend), subAction = "incorrectCode")
    }

    override fun logTotpNetworkError(autoSend: Boolean) =
        logTotp(getSendErrorStep(autoSend), getSendAction(autoSend), subAction = "networkError")

    override fun logTotpSuccess(autoSend: Boolean) =
        logTotp(getSendSuccessStep(autoSend), getSendAction(autoSend), subAction = "success")

    private fun getSendSuccessStep(autoSend: Boolean) = if (autoSend) "6.1.3" else "6.1.2"
    private fun getSendErrorStep(autoSend: Boolean) = if (autoSend) "6.2.2" else "6.2.1"
    private fun getSendAction(autoSend: Boolean) = if (autoSend) "nextAutomatic" else "next"

    private fun logTotp(step: String, action: String, subAction: String? = null) {
        logInstallLog69("totp", action, subAction, step)
    }

    private fun logDuo(step: String, action: String, subAction: String? = null) {
        logInstallLog69("duo", action, subAction, step)
    }

    private fun logInstallLog69(
        subType: String,
        action: String,
        subAction: String?,
        step: String
    ) {
        log(
            InstallLogCode69(
                loginSession = trackingId,
                type = InstallLogCode69.Type.LOGIN,
                subType = subType,
                action = action,
                deviceStatus = deviceStatus,
                accountType = accountTypeFlags,
                subAction = subAction,
                subStep = step
            )
        )
    }

    private fun log(log: InstallLog) {
        installLogRepository.enqueue(log)
    }

    class Factory @Inject constructor(
        @TrackingId private val trackingId: String,
        private val installLogRepository: InstallLogRepository,
        private val logRepository: LogRepository
    ) : LoginTotpLogger.Factory {
        override fun create(
            registeredDevice: Boolean,
            userSecuritySettings: UserSecuritySettings
        ): LoginTotpLoggerImpl {
            val deviceStatus = if (registeredDevice) InstallLogCode69.DeviceStatus.REGISTERED else InstallLogCode69.DeviceStatus.NEW
            return LoginTotpLoggerImpl(
                installLogRepository,
                trackingId,
                deviceStatus,
                userSecuritySettings.asString(),
                LoginLogger(logRepository),
                verificationMode = if (registeredDevice) VerificationMode.OTP2 else VerificationMode.OTP1
            )
        }
    }
}