package com.dashlane.accountrecoverykey

interface AccountRecoveryKeySettingStateRefresher {
    suspend fun refresh()
}