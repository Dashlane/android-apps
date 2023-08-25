package com.dashlane.login.accountrecoverykey

import com.dashlane.authentication.RegisteredUserDevice

sealed class LoginAccountRecoveryKeyState {
    abstract val data: LoginAccountRecoveryKeyData

    data class Initial(override val data: LoginAccountRecoveryKeyData) : LoginAccountRecoveryKeyState()
    data class Loading(override val data: LoginAccountRecoveryKeyData) : LoginAccountRecoveryKeyState()
    data class GoToToken(override val data: LoginAccountRecoveryKeyData) : LoginAccountRecoveryKeyState()
    data class GoToTOTP(override val data: LoginAccountRecoveryKeyData) : LoginAccountRecoveryKeyState()
    data class GoToARK(override val data: LoginAccountRecoveryKeyData, val authTicket: String) : LoginAccountRecoveryKeyState()
    data class FinishWithSuccess(override val data: LoginAccountRecoveryKeyData, val decryptedVaultKey: String) : LoginAccountRecoveryKeyState()
}

data class LoginAccountRecoveryKeyData(
    val registeredUserDevice: RegisteredUserDevice? = null,
)
