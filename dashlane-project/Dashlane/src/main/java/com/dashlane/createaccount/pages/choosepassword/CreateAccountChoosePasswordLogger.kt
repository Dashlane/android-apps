package com.dashlane.createaccount.pages.choosepassword



interface CreateAccountChoosePasswordLogger {
    

    fun logLand()

    

    fun logBack()

    

    fun logPasswordVisibilityToggle(passwordShown: Boolean)

    

    fun logEmptyPassword()

    

    fun logInsufficientPassword()

    

    fun logPasswordChosen()

    

    fun logShowPasswordTips()
}