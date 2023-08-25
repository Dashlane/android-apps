package com.dashlane.accountrecoverykey.setting

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
    val isDialogDisplayed: Boolean = false
)