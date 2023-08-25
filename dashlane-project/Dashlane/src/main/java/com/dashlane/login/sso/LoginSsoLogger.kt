package com.dashlane.login.sso

interface LoginSsoLogger {
    fun logLoginStart()

    fun logInvalidSso()

    fun logErrorUnknown()
}