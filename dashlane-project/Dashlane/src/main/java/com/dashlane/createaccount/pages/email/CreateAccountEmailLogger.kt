package com.dashlane.createaccount.pages.email

interface CreateAccountEmailLogger {

    fun logEmptyEmail()

    fun logInvalidEmailLocal()

    fun logInvalidEmailServer()

    fun logAccountExists()
}