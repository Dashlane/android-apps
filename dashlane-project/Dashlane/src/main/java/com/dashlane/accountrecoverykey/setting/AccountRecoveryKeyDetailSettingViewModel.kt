package com.dashlane.accountrecoverykey.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.accountrecoverykey.AccountRecoveryKeyRepository
import com.dashlane.core.DataSync
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@HiltViewModel
class AccountRecoveryKeyDetailSettingViewModel @Inject constructor(
    private val accountRecoveryKeyRepository: AccountRecoveryKeyRepository,
    private val dataSync: DataSync,
) : ViewModel() {

    private val stateFlow =
        MutableStateFlow<AccountRecoveryKeyDetailSettingState>(AccountRecoveryKeyDetailSettingState.Initial(AccountRecoveryKeyDetailSettingData()))
    val uiState = stateFlow.asStateFlow()

    fun viewStarted() {
        viewModelScope.launch {
            stateFlow.emit(AccountRecoveryKeyDetailSettingState.Loading(stateFlow.value.data))
            accountRecoveryKeyRepository.getAccountRecoveryStatusAsync()
                .await()
                .onSuccess { stateFlow.emit(AccountRecoveryKeyDetailSettingState.DetailedSettings(stateFlow.value.data.copy(enabled = it.enabled))) }
        }
    }

    fun toggleClicked(checked: Boolean) {
        viewModelScope.launch {
            if (checked) {
                stateFlow.emit(AccountRecoveryKeyDetailSettingState.GoToIntro(stateFlow.value.data))
            } else {
                stateFlow.emit(AccountRecoveryKeyDetailSettingState.ConfirmationDisableDialog(stateFlow.value.data.copy(isDialogDisplayed = true)))
            }
        }
    }

    fun confirmDisable() {
        flow<AccountRecoveryKeyDetailSettingState> {
            accountRecoveryKeyRepository.disableRecoveryKey()
            dataSync.awaitSync()
            accountRecoveryKeyRepository.getAccountRecoveryStatusAsync().await()
                .onSuccess {
                    emit(AccountRecoveryKeyDetailSettingState.DetailedSettings(stateFlow.value.data.copy(enabled = it.enabled)))
                }
                .onFailure {
                    emit(AccountRecoveryKeyDetailSettingState.DetailedSettings(stateFlow.value.data.copy(enabled = false)))
                }
        }
            .catch {
                emit(AccountRecoveryKeyDetailSettingState.DetailedSettings(stateFlow.value.data.copy(enabled = false)))
            }
            .onStart { emit(AccountRecoveryKeyDetailSettingState.Loading(stateFlow.value.data.copy(isDialogDisplayed = false))) }
            .onEach { stateFlow.emit(it) }
            .launchIn(viewModelScope)
    }

    fun cancelDisable() {
        viewModelScope.launch { stateFlow.emit(AccountRecoveryKeyDetailSettingState.Initial(stateFlow.value.data.copy(isDialogDisplayed = false))) }
    }

    fun hasNavigated() {
        viewModelScope.launch { stateFlow.emit(AccountRecoveryKeyDetailSettingState.Initial(stateFlow.value.data)) }
    }
}
