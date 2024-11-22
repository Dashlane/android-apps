package com.dashlane.login.pages.email.compose

import com.dashlane.authentication.AuthenticationSecondFactor
import com.dashlane.authentication.login.SsoInfo
import com.dashlane.mvvm.State

data class LoginEmailState(
    val email: String? = null,
    val isLoading: Boolean = false,
    val error: LoginEmailError? = null,
    val crashDeviceId: String? = null,
    val isSSOAdminDialogShown: Boolean = false,
    val showDebugConfirmationDialog: Boolean = false,
    val showDebugUploadingDialog: Boolean = false,
    val showDebugSuccessDialog: Boolean = false,
    val showDebugFailedDialog: Boolean = false,
) : State

sealed class LoginEmailNavigationState : State {
    data class GoToCreateAccount(val email: String?, val skipIfPrefilled: Boolean) : LoginEmailNavigationState()
    data class GoToSecretTransfer(val email: String?, val showQrCode: Boolean) :
        LoginEmailNavigationState()
    data class GoToToken(val secondFactor: AuthenticationSecondFactor.EmailToken) : LoginEmailNavigationState()
    data class GoToOTP(val secondFactor: AuthenticationSecondFactor.Totp) : LoginEmailNavigationState()
    data class GoToSSO(val email: String?, val ssoInfo: SsoInfo) : LoginEmailNavigationState()
    data object SSOSuccess : LoginEmailNavigationState()
    data object GoToPassword : LoginEmailNavigationState()
    data object EndOfLife : LoginEmailNavigationState()
}

sealed class LoginEmailError {
    data object Generic : LoginEmailError()
    data object InvalidEmail : LoginEmailError()
    data object NoAccount : LoginEmailError()
    data object Offline : LoginEmailError()
    data object Network : LoginEmailError()
    data object Team : LoginEmailError()
    data object SSO : LoginEmailError()
    data object UserDeactivated : LoginEmailError()
}