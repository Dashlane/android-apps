package com.dashlane.login.pages.secrettransfer

import com.dashlane.account.UserAccountInfo
import com.dashlane.authentication.RegisteredUserDevice

sealed class LoginSecretTransferState {
    abstract val data: LoginSecretTransferData

    data class Initial(override val data: LoginSecretTransferData) : LoginSecretTransferState()
    data class ConfirmEmail(override val data: LoginSecretTransferData) : LoginSecretTransferState()
    data class LoadingLogin(override val data: LoginSecretTransferData) : LoginSecretTransferState()
    data class Success(override val data: LoginSecretTransferData, val accountType: UserAccountInfo.AccountType) : LoginSecretTransferState()
    data class Cancelled(override val data: LoginSecretTransferData) : LoginSecretTransferState()
    data class Error(override val data: LoginSecretTransferData) : LoginSecretTransferState()
}

data class LoginSecretTransferData(
    val secretTransferPayload: SecretTransferPayload? = null,
    val registeredUserDevice: RegisteredUserDevice.Remote? = null
)