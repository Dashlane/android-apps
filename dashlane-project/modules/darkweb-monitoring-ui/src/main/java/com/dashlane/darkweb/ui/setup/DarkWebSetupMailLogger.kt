package com.dashlane.darkweb.ui.setup



internal interface DarkWebSetupMailLogger {
    

    fun logShow()

    

    fun logCancel()

    

    fun logNext()

    

    fun logInvalidMailLocal()

    

    fun logEmptyMail()

    

    fun logInvalidMailServer()

    

    fun logAlreadyValidatedMail()

    

    fun logLimitReached()
}