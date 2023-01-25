package com.dashlane.login.pages.totp

import com.dashlane.account.UserSecuritySettings



interface LoginTotpLogger {
    

    fun logLand()

    

    fun logBack()

    

    fun logDuoClick()

    

    fun logDuoAppear()

    

    fun logDuoCancel()

    

    fun logDuoTimeout()

    

    fun logDuoNetworkError()

    

    fun logDuoDenied()

    

    fun logDuoSuccess()

    

    fun logInvalidTotp(autoSend: Boolean)

    

    fun logTotpNetworkError(autoSend: Boolean)

    

    fun logTotpSuccess(autoSend: Boolean)

    

    fun logU2fPopupClick()

    

    fun logU2fSuccess()

    fun logExecuteU2fAuthentication()
    fun logFailedSignU2FTag()
    fun logSuccessSignU2FTag()

    interface Factory {
        fun create(registeredDevice: Boolean, userSecuritySettings: UserSecuritySettings): LoginTotpLogger
    }
}