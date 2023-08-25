package com.dashlane.login.sso

import com.dashlane.hermes.LogRepository
import com.dashlane.login.LoginLogger
import com.dashlane.login.LoginMode
import dagger.Reusable
import javax.inject.Inject

@Reusable
class LoginSsoLoggerImpl @Inject constructor(
    logRepository: LogRepository
) : LoginSsoLogger {

    private val loginLogger = LoginLogger(logRepository)

    override fun logLoginStart() {
        loginLogger.logAskAuthentication(LoginMode.Sso)
    }

    override fun logInvalidSso() {
        loginLogger.logInvalidSso()
    }

    override fun logErrorUnknown() {
        loginLogger.logErrorUnknown(LoginMode.Sso)
    }
}