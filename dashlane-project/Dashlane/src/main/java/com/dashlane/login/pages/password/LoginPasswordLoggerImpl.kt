package com.dashlane.login.pages.password

import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.authentication.SecurityFeature
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.VerificationMode
import com.dashlane.login.LoginLogger
import com.dashlane.login.LoginMode
import javax.inject.Inject

class LoginPasswordLoggerImpl(
    private val otpRequired: Boolean,
    private val loginLogger: LoginLogger,
    private val verification: VerificationMode
) : LoginPasswordLogger {

    override fun logEmptyPassword() {
        loginLogger.logWrongPassword(verification)
    }

    override fun logPasswordInvalid() {
        loginLogger.logWrongPassword(verification)
    }

    override fun logPasswordInvalidWithRecovery() {
        loginLogger.logWrongPassword(verification)
    }

    override fun logNetworkError(@LoginPasswordLogger.NetworkError error: String) {
        loginLogger.logErrorUnknown(loginMode = LoginMode.MasterPassword(verification))
    }

    class Factory @Inject constructor(
        private val logRepository: LogRepository,
    ) : LoginPasswordLogger.Factory {
        override fun create(
            registeredUserDevice: RegisteredUserDevice,
            verification: VerificationMode
        ): LoginPasswordLoggerImpl {
            val isLocalDevice = registeredUserDevice is RegisteredUserDevice.Local
            val isServerKeyRequired = registeredUserDevice.isServerKeyRequired
            val securityFeatures = registeredUserDevice.securityFeatures
            val otpRequired =
                isServerKeyRequired || (!isLocalDevice && securityFeatures.contains(SecurityFeature.TOTP))
            return LoginPasswordLoggerImpl(
                otpRequired,
                LoginLogger(logRepository),
                verification = verification
            )
        }
    }
}
