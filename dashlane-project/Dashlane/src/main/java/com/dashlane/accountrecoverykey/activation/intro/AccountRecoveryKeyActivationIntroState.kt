package com.dashlane.accountrecoverykey.activation.intro

import com.dashlane.account.UserAccountInfo

sealed class AccountRecoveryKeyActivationIntroState {
    abstract val data: AccountRecoveryKeyActivationIntroData

    data class Default(override val data: AccountRecoveryKeyActivationIntroData) : AccountRecoveryKeyActivationIntroState()
    data class SkipAlertDialogVisible(override val data: AccountRecoveryKeyActivationIntroData) : AccountRecoveryKeyActivationIntroState()
}

data class AccountRecoveryKeyActivationIntroData(
    val accountType: UserAccountInfo.AccountType = UserAccountInfo.AccountType.MasterPassword
)