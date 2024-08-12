package com.dashlane.login.pages.authenticator.compose

import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.mvvm.State

data class LoginDashlaneAuthenticatorState(
    val email: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: LoginDashlaneAuthenticatorError? = null
) : State

sealed class LoginDashlaneAuthenticatorNavigationState : State {
    data object Canceled : LoginDashlaneAuthenticatorNavigationState()
    data class Success(val registeredUserDevice: RegisteredUserDevice, val authTicket: String) : LoginDashlaneAuthenticatorNavigationState()
}

sealed class LoginDashlaneAuthenticatorError {
    data object ExpiredVersion : LoginDashlaneAuthenticatorError()
    data object Timeout : LoginDashlaneAuthenticatorError()
    data object Generic : LoginDashlaneAuthenticatorError()
}