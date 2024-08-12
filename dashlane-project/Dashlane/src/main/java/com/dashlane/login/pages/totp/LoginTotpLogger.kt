package com.dashlane.login.pages.totp

import com.dashlane.user.UserSecuritySettings

interface LoginTotpLogger {

    fun logInvalidTotp(autoSend: Boolean)

    interface Factory {
        fun create(registeredDevice: Boolean, userSecuritySettings: UserSecuritySettings): LoginTotpLogger
    }
}