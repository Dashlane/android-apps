package com.dashlane.accountrecoverykey.setting

import com.dashlane.account.UserAccountInfo

sealed class AccountRecoveryKeyDetailSettingState {
    abstract val data: AccountRecoveryKeyDetailSettingData

    data class Initial(override val data: AccountRecoveryKeyDetailSettingData) : AccountRecoveryKeyDetailSettingState()
    data class Loading(override val data: AccountRecoveryKeyDetailSettingData) : AccountRecoveryKeyDetailSettingState()
    data class DetailedSettings(override val data: AccountRecoveryKeyDetailSettingData) : AccountRecoveryKeyDetailSettingState()
    data class ConfirmationDisableDialog(override val data: AccountRecoveryKeyDetailSettingData) : AccountRecoveryKeyDetailSettingState()
    data class GoToIntro(override val data: AccountRecoveryKeyDetailSettingData) : AccountRecoveryKeyDetailSettingState()
}

data class AccountRecoveryKeyDetailSettingData(
    val enabled: Boolean = false,
    val accountType: UserAccountInfo.AccountType = UserAccountInfo.AccountType.MasterPassword,
    val isDialogDisplayed: Boolean = false
)