package com.dashlane.login.pages.token



interface LoginTokenLogger {
    

    fun logLand()

    

    fun logBack()

    

    fun logWhereIsMyCodeClick()

    

    fun logWhereIsMyCodeAppear()

    

    fun logCloseWhereIsMyCode()

    

    fun logDismissWhereIsMyCode()

    

    fun logResendCode()

    

    fun logInvalidToken(autoSend: Boolean)

    

    fun logNetworkError(autoSend: Boolean)

    

    fun logTokenSuccess(autoSend: Boolean)

    

    fun logTokenViaDeepLink()
}