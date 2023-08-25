package com.dashlane.login.pages.secrettransfer

import com.dashlane.authentication.AuthenticationSecondFactor

sealed class LoginSecretTransferEvent {
    object Retry : LoginSecretTransferEvent()
    object Cancel : LoginSecretTransferEvent()
    object EmailConfirmed : LoginSecretTransferEvent()
    data class ChangeFromPushTo2FA(val secondFactor: AuthenticationSecondFactor.Totp) : LoginSecretTransferEvent()
    data class TotpCompleted(val otp: String) : LoginSecretTransferEvent()
}
