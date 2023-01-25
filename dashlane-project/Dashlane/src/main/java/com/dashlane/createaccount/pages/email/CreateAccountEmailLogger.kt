package com.dashlane.createaccount.pages.email



interface CreateAccountEmailLogger {
    

    fun logLand()

    

    fun logBack()

    

    fun logEmptyEmail()

    

    fun logInvalidEmailLocal()

    

    fun logInvalidEmailServer()

    

    fun logUnlikelyEmail()

    

    fun logAccountExists()

    

    fun logNetworkError()

    

    fun logValidEmail()

    

    fun logShowConfirmEmail()

    

    fun logCancelConfirmEmail()

    

    fun logConfirmEmail()
}