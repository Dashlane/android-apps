package com.dashlane.login.pages.totp

import com.dashlane.account.UserSecuritySettings
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.VerificationMode
import com.dashlane.login.LoginLogger
import javax.inject.Inject

class LoginTotpLoggerImpl(
    private val loginLogger: LoginLogger,
    private val verificationMode: VerificationMode
) : LoginTotpLogger {

    override fun logInvalidTotp(autoSend: Boolean) {
        loginLogger.logWrongOtp(verificationMode)
    }

    class Factory @Inject constructor(
        private val logRepository: LogRepository
    ) : LoginTotpLogger.Factory {
        override fun create(
            registeredDevice: Boolean,
            userSecuritySettings: UserSecuritySettings
        ): LoginTotpLoggerImpl {
            return LoginTotpLoggerImpl(
                LoginLogger(logRepository),
                verificationMode = if (registeredDevice) VerificationMode.OTP2 else VerificationMode.OTP1
            )
        }
    }
}