package com.dashlane.login.accountrecoverykey.enterark

sealed class EnterARKState {
    abstract val data: EnterARKData

    data class Initial(override val data: EnterARKData) : EnterARKState()
    data class Loading(override val data: EnterARKData) : EnterARKState()
    data class KeyConfirmed(override val data: EnterARKData, val decryptedVaultKey: String) : EnterARKState()
    data class GoToNext(override val data: EnterARKData, val decryptedVaultKey: String) : EnterARKState()
    data class Error(override val data: EnterARKData) : EnterARKState()
}

data class EnterARKData(
    val accountRecoveryKey: String = ""
)
