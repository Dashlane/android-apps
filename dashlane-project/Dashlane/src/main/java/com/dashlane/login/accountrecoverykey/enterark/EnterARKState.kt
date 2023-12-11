package com.dashlane.login.accountrecoverykey.enterark

import com.dashlane.account.UserAccountInfo
import com.dashlane.cryptography.ObfuscatedByteArray

sealed class EnterARKState {
    abstract val data: EnterARKData

    data class Initial(override val data: EnterARKData) : EnterARKState()
    data class Loading(override val data: EnterARKData) : EnterARKState()
    data class KeyConfirmed(override val data: EnterARKData, val obfuscatedVaultKey: ObfuscatedByteArray) : EnterARKState()
    data class Error(override val data: EnterARKData) : EnterARKState()
}

data class EnterARKData(
    val accountType: UserAccountInfo.AccountType = UserAccountInfo.AccountType.MasterPassword,
    val accountRecoveryKey: String = ""
)
