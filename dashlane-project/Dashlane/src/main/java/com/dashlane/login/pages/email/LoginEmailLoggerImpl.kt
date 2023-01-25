package com.dashlane.login.pages.email

import com.dashlane.account.UserSecuritySettings
import com.dashlane.hermes.LogRepository
import com.dashlane.login.LoginLogger
import com.dashlane.login.dagger.TrackingId
import com.dashlane.useractivity.log.install.InstallLogCode69
import com.dashlane.useractivity.log.install.InstallLogRepository
import javax.inject.Inject

class LoginEmailLoggerImpl @Inject constructor(
    @TrackingId private val trackingId: String,
    private val installLogRepository: InstallLogRepository,
    logRepository: LogRepository
) : LoginEmailLogger {
    private val loginLogger: LoginLogger = LoginLogger(logRepository)

    override fun logLand(@LoginEmailLogger.LandState state: String, securitySettings: UserSecuritySettings?) =
        log(
            "3", "land",
            subAction = state,
            securitySettings = securitySettings
        )

    override fun logBack() =
        log("4.0", "back")

    override fun logAutoFill(securitySettings: UserSecuritySettings?) =
        log(
            "4.1.2", "clickSuggestion",
            subAction = "1",
            deviceStatus = InstallLogCode69.DeviceStatus.REGISTERED,
            securitySettings = securitySettings
        )

    override fun logEmptyEmail() {
        loginLogger.logWrongEmail()
        log("5.1.2", "nextManual", subAction = "emptyField")
    }

    override fun logInvalidEmail() {
        loginLogger.logWrongEmail()
        log("5.1.2", "nextManual", subAction = "invalidFormat")
    }

    override fun logRejectedEmail(invalid: Boolean) {
        loginLogger.logWrongEmail()
        log("5.1.2", "nextManual", subAction = if (invalid) "nonExistingInvalid" else "nonExisting")
    }

    override fun logNetworkError(
        @LoginEmailLogger.InputType inputType: String,
        @LoginEmailLogger.NetworkError error: String
    ) = log("5.1.2", inputType, subAction = "networkError$error")

    override fun logValidatedEmail(
        registeredDevice: Boolean,
        @LoginEmailLogger.InputType inputType: String,
        securitySettings: UserSecuritySettings?
    ) =
        log(
            "5.1.1", inputType,
            subAction = "success",
            deviceStatus = if (registeredDevice) {
                InstallLogCode69.DeviceStatus.REGISTERED
            } else {
                InstallLogCode69.DeviceStatus.NEW
            },
            securitySettings = securitySettings
        )

    override fun logClearedEmail(securitySettings: UserSecuritySettings?) =
        log(
            "5.2", "deletePreFilled",
            deviceStatus = InstallLogCode69.DeviceStatus.REGISTERED,
            securitySettings = securitySettings
        )

    override fun logCreateAccountClick() =
        log("5.3", "createAccount")

    private fun log(
        step: String,
        action: String,
        subAction: String? = null,
        deviceStatus: InstallLogCode69.DeviceStatus? = null,
        securitySettings: UserSecuritySettings? = null
    ) {
        installLogRepository.enqueue(
            InstallLogCode69(
                loginSession = trackingId,
                type = InstallLogCode69.Type.LOGIN,
                subType = "enterEmail",
                action = action,
                subStep = step,
                subAction = subAction,
                deviceStatus = deviceStatus,
                accountType = securitySettings?.asString()
            )
        )
    }
}