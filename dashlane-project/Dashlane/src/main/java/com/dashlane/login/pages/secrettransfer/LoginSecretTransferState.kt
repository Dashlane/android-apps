package com.dashlane.login.pages.secrettransfer

import com.dashlane.authentication.AuthenticationSecondFactor

sealed class LoginSecretTransferState {
    abstract val data: LoginSecretTransferData

    data class LoadingQR(override val data: LoginSecretTransferData) : LoginSecretTransferState()
    data class QrCodeUriGenerated(override val data: LoginSecretTransferData) : LoginSecretTransferState()
    data class ConfirmEmail(override val data: LoginSecretTransferData, val email: String) : LoginSecretTransferState()
    data class LoadingLogin(override val data: LoginSecretTransferData) : LoginSecretTransferState()
    data class AskForTOTP(override val data: LoginSecretTransferData, val secondFactor: AuthenticationSecondFactor.Totp) : LoginSecretTransferState()
    data class WaitForPush(override val data: LoginSecretTransferData, val secondFactor: AuthenticationSecondFactor.Totp) : LoginSecretTransferState()
    data class LoginSuccess(override val data: LoginSecretTransferData) : LoginSecretTransferState()
    data class Cancelled(override val data: LoginSecretTransferData) : LoginSecretTransferState()
    data class Error(override val data: LoginSecretTransferData, val error: LoginSecretTransferError) : LoginSecretTransferState()
}

data class LoginSecretTransferData(
    val qrCodeUri: String? = null
)

sealed class LoginSecretTransferError {
    object StartTransferError : LoginSecretTransferError()
    object LoginError : LoginSecretTransferError()
    object QrCodeGeneration : LoginSecretTransferError()
}
