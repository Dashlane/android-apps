package com.dashlane.accountrecoverykey.setting

interface AccountRecoveryKeySettingStateRefresher {
    suspend fun refresh()
}