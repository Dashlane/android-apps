package com.dashlane.login.pages.authenticator.compose

import com.dashlane.authentication.RegisteredUserDevice

sealed class LoginDashlaneAuthenticatorState {

    object Initial : LoginDashlaneAuthenticatorState()
    object Loading : LoginDashlaneAuthenticatorState()

    object Canceled : LoginDashlaneAuthenticatorState()
    data class Success(
        val registeredUserDevice: RegisteredUserDevice,
        val authTicket: String
    ) : LoginDashlaneAuthenticatorState()

    data class Error(val error: LoginDashlaneAuthenticatorError) :
        LoginDashlaneAuthenticatorState()
}

sealed class LoginDashlaneAuthenticatorError : Exception() {
    object Network : LoginDashlaneAuthenticatorError()
    object Offline : LoginDashlaneAuthenticatorError()
}