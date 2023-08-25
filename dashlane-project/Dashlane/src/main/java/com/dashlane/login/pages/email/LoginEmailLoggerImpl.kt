package com.dashlane.login.pages.email

import com.dashlane.hermes.LogRepository
import com.dashlane.login.LoginLogger
import javax.inject.Inject

class LoginEmailLoggerImpl @Inject constructor(
    logRepository: LogRepository
) : LoginEmailLogger {
    private val loginLogger: LoginLogger = LoginLogger(logRepository)

    override fun logEmptyEmail() {
        loginLogger.logWrongEmail()
    }

    override fun logInvalidEmail() {
        loginLogger.logWrongEmail()
    }

    override fun logRejectedEmail(invalid: Boolean) {
        loginLogger.logWrongEmail()
    }
}