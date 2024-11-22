package com.dashlane.accountrecoverykey.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.accountrecoverykey.AccountRecoveryKeyRepository
import com.dashlane.accountrecoverykey.AccountRecoveryKeySettingStateRefresher
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.hermes.generated.definitions.DeleteKeyReason
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.sync.DataSync
import com.dashlane.user.UserAccountInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AccountRecoveryKeyDetailSettingViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val preferencesManager: PreferencesManager,
    private val accountRecoveryKeyRepository: AccountRecoveryKeyRepository,
    private val logRepository: LogRepository,
    private val dataSync: DataSync,
    private val accountRecoveryKeySettingStateRefresher: AccountRecoveryKeySettingStateRefresher
) : ViewModel() {

    private val _stateFlow = MutableViewStateFlow<AccountRecoveryKeyDetailSettingState.View, AccountRecoveryKeyDetailSettingState.SideEffect>(
        AccountRecoveryKeyDetailSettingState.View()
    )
    val stateFlow: ViewStateFlow<AccountRecoveryKeyDetailSettingState.View, AccountRecoveryKeyDetailSettingState.SideEffect> = _stateFlow

    init {
        logRepository.queuePageView(BrowseComponent.MAIN_APP, AnyPage.SETTINGS_SECURITY_RECOVERY_KEY)
    }

    fun viewStarted() {
        viewModelScope.launch {
            val session = sessionManager.session ?: throw IllegalStateException("session cannot be null")
            val accountType = preferencesManager[session.username].accountType
                ?.let { UserAccountInfo.AccountType.fromString(it) }
                ?: throw IllegalStateException("accountType cannot be null")

            _stateFlow.update { state -> state.copy(isLoading = true, accountType = accountType) }
            val arkStatus = accountRecoveryKeyRepository.getAccountRecoveryStatusAsync()
            _stateFlow.update { state -> state.copy(isLoading = false, enabled = arkStatus.enabled) }
        }
    }

    fun toggleClicked(checked: Boolean) {
        viewModelScope.launch {
            if (checked) {
                _stateFlow.send(AccountRecoveryKeyDetailSettingState.SideEffect.GoToIntro)
            } else {
                logRepository.queuePageView(BrowseComponent.MAIN_APP, AnyPage.SETTINGS_SECURITY_RECOVERY_KEY_DISABLE)
                _stateFlow.update { state -> state.copy(isDialogDisplayed = true) }
            }
        }
    }

    fun confirmDisable() {
        flow<AccountRecoveryKeyDetailSettingState.View> {
            accountRecoveryKeyRepository.disableRecoveryKey(DeleteKeyReason.SETTING_DISABLED)
            dataSync.awaitSync()
            val arkStatus = accountRecoveryKeyRepository.getAccountRecoveryStatusAsync()
            emit(_stateFlow.value.copy(enabled = arkStatus.enabled, isLoading = false))
            accountRecoveryKeySettingStateRefresher.refresh()
        }
            .catch {
                emit(_stateFlow.value.copy(enabled = false, isLoading = false))
            }
            .onStart { emit(_stateFlow.value.copy(isLoading = true, isDialogDisplayed = false)) }
            .onEach { state -> _stateFlow.update { state } }
            .launchIn(viewModelScope)
    }

    fun cancelDisable() {
        _stateFlow.update { state -> state.copy(isDialogDisplayed = false) }
    }
}
