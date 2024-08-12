package com.dashlane.accountrecoverykey.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.user.UserAccountInfo
import com.dashlane.accountrecoverykey.AccountRecoveryKeyRepository
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.hermes.generated.definitions.DeleteKeyReason
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.sync.DataSync
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountRecoveryKeyDetailSettingViewModel @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager,
    private val accountRecoveryKeyRepository: AccountRecoveryKeyRepository,
    private val logRepository: LogRepository,
    private val dataSync: DataSync,
    private val accountRecoveryKeySettingStateRefresher: AccountRecoveryKeySettingStateRefresher
) : ViewModel() {

    private val stateFlow =
        MutableStateFlow<AccountRecoveryKeyDetailSettingState>(AccountRecoveryKeyDetailSettingState.Initial(AccountRecoveryKeyDetailSettingData()))
    val uiState = stateFlow.asStateFlow()

    init {
        logRepository.queuePageView(BrowseComponent.MAIN_APP, AnyPage.SETTINGS_SECURITY_RECOVERY_KEY)
    }

    fun viewStarted() {
        viewModelScope.launch {
            val accountType = UserAccountInfo.AccountType.fromString(userPreferencesManager.accountType)
            stateFlow.emit(AccountRecoveryKeyDetailSettingState.Loading(stateFlow.value.data.copy(accountType = accountType)))
            val arkStatus = accountRecoveryKeyRepository.getAccountRecoveryStatusAsync()
            stateFlow.emit(AccountRecoveryKeyDetailSettingState.DetailedSettings(stateFlow.value.data.copy(enabled = arkStatus.enabled)))
        }
    }

    fun toggleClicked(checked: Boolean) {
        viewModelScope.launch {
            if (checked) {
                stateFlow.emit(AccountRecoveryKeyDetailSettingState.GoToIntro(stateFlow.value.data))
            } else {
                logRepository.queuePageView(BrowseComponent.MAIN_APP, AnyPage.SETTINGS_SECURITY_RECOVERY_KEY_DISABLE)
                stateFlow.emit(AccountRecoveryKeyDetailSettingState.ConfirmationDisableDialog(stateFlow.value.data.copy(isDialogDisplayed = true)))
            }
        }
    }

    fun confirmDisable() {
        flow<AccountRecoveryKeyDetailSettingState> {
            accountRecoveryKeyRepository.disableRecoveryKey(DeleteKeyReason.SETTING_DISABLED)
            dataSync.awaitSync()
            val arkStatus = accountRecoveryKeyRepository.getAccountRecoveryStatusAsync()
            emit(AccountRecoveryKeyDetailSettingState.DetailedSettings(stateFlow.value.data.copy(enabled = arkStatus.enabled)))
            accountRecoveryKeySettingStateRefresher.refresh()
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
