package com.dashlane.accountrecoverykey.activation.generate

import com.dashlane.account.UserAccountInfo

sealed class AccountRecoveryKeyGenerateState {
    abstract val data: AccountRecoveryKeyGenerateData

    data class Initial(override val data: AccountRecoveryKeyGenerateData) : AccountRecoveryKeyGenerateState()
    data class Loading(override val data: AccountRecoveryKeyGenerateData) : AccountRecoveryKeyGenerateState()
    data class KeyGenerated(override val data: AccountRecoveryKeyGenerateData) : AccountRecoveryKeyGenerateState()
    data class Cancel(override val data: AccountRecoveryKeyGenerateData) : AccountRecoveryKeyGenerateState()
    data class GoToConfirm(override val data: AccountRecoveryKeyGenerateData) : AccountRecoveryKeyGenerateState()
    data class Error(override val data: AccountRecoveryKeyGenerateData) : AccountRecoveryKeyGenerateState()
}

data class AccountRecoveryKeyGenerateData(
    val accountType: UserAccountInfo.AccountType = UserAccountInfo.AccountType.MasterPassword,
    val accountRecoveryKey: String? = null,
    val cancelDialogShown: Boolean = false
)