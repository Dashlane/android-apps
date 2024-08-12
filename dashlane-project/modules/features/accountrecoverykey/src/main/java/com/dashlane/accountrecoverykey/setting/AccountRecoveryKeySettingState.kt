package com.dashlane.accountrecoverykey.setting

sealed class AccountRecoveryKeySettingState {
    data object Loading : AccountRecoveryKeySettingState()
    data object Hidden : AccountRecoveryKeySettingState()
    data class Loaded(
        val isEnabled: Boolean,
    ) : AccountRecoveryKeySettingState()
}