package com.dashlane.login

import com.dashlane.login.sso.LoginSsoLogger

interface LoginSsoLoggerConfigProvider {
    val ssoLoggerConfig: LoginSsoLogger.Config
}