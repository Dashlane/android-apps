package com.dashlane.accountrecoverykey.setting

import com.dashlane.accountrecoverykey.AccountRecoveryKeyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRecoveryKeySettingStateHolder @Inject constructor(
    private val accountRecoveryKeyRepository: AccountRecoveryKeyRepository,
) : AccountRecoveryKeySettingStateRefresher {

    private val stateFlow = MutableStateFlow<AccountRecoveryKeySettingState>(AccountRecoveryKeySettingState.Loading)
    val uiState = stateFlow.asStateFlow()

    override suspend fun refresh() {
        val status = accountRecoveryKeyRepository.getAccountRecoveryStatusAsync()
        stateFlow.update {
            if (status.visible) {
                AccountRecoveryKeySettingState.Loaded(
                    isEnabled = status.enabled
                )
            } else {
                AccountRecoveryKeySettingState.Hidden
            }
        }
    }
}
