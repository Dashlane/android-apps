package com.dashlane.accountrecoverykey.activation.confirm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.accountrecoverykey.AccountRecoveryKeyRepository
import com.dashlane.accountrecoverykey.AccountRecoveryKeySettingStateRefresher
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.hermes.generated.definitions.CreateKeyErrorName
import com.dashlane.hermes.generated.definitions.FlowStep
import com.dashlane.hermes.generated.events.user.CreateAccountRecoveryKey
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.sync.DataSync
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
class AccountRecoveryKeyConfirmViewModel @Inject constructor(
    private val accountRecoveryKeyRepository: AccountRecoveryKeyRepository,
    private val logRepository: LogRepository,
    private val dataSync: DataSync,
    private val accountRecoveryKeySettingStateRefresher: AccountRecoveryKeySettingStateRefresher,
) : ViewModel() {

    private val _stateFlow =
        MutableViewStateFlow<AccountRecoveryKeyConfirmState.View, AccountRecoveryKeyConfirmState.SideEffect>(AccountRecoveryKeyConfirmState.View())
    val stateFlow: ViewStateFlow<AccountRecoveryKeyConfirmState.View, AccountRecoveryKeyConfirmState.SideEffect> = _stateFlow

    init {
        logRepository.queuePageView(BrowseComponent.MAIN_APP, AnyPage.SETTINGS_SECURITY_RECOVERY_KEY_CONFIRM)
    }

    fun keyChanged(accountRecoveryKey: String) {
        viewModelScope.launch {
            _stateFlow.update { state -> state.copy(accountRecoveryKey = accountRecoveryKey, error = null) }
        }
    }

    fun confirmClicked() {
        flow<AccountRecoveryKeyConfirmState> {
            val accountRecoveryKey = _stateFlow.value.accountRecoveryKey ?: throw InvalidKeyException
            val keyConfirmed = accountRecoveryKeyRepository.confirmRecoveryKey(accountRecoveryKey.replace("-", ""))

            if (!keyConfirmed) {
                throw InvalidKeyException
            }

            val syncSuccess = dataSync.awaitSync()

            check(syncSuccess) { "Sync error when confirming ARK" }

            accountRecoveryKeyRepository.confirmActivation()
                .onSuccess {
                    logRepository.queuePageView(BrowseComponent.MAIN_APP, AnyPage.SETTINGS_SECURITY_RECOVERY_KEY_SUCCESS)
                    emit(AccountRecoveryKeyConfirmState.SideEffect.KeyConfirmed)
                    accountRecoveryKeySettingStateRefresher.refresh()
                }
                .onFailure { throw it }
        }
            .catch { throwable ->
                logRepository.queueEvent(CreateAccountRecoveryKey(flowStep = FlowStep.ERROR, createKeyErrorName = CreateKeyErrorName.UNKNOWN))
                val errorState = when (throwable) {
                    is InvalidKeyException -> _stateFlow.value.copy(error = AccountRecoveryKeyConfirmError.KeyError, isLoading = false)
                    else -> _stateFlow.value.copy(error = AccountRecoveryKeyConfirmError.SyncError, isLoading = false)
                }
                emit(errorState)
            }
            .onStart { emit(_stateFlow.value.copy(isLoading = true)) }
            .onEach { state ->
                when (state) {
                    is AccountRecoveryKeyConfirmState.View -> _stateFlow.update { state }
                    is AccountRecoveryKeyConfirmState.SideEffect -> {
                        _stateFlow.update { it.copy(isLoading = false) }
                        _stateFlow.send(state)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun retryClicked() {
        viewModelScope.launch {
            when (_stateFlow.value.error) {
                is AccountRecoveryKeyConfirmError.SyncError -> {
                    _stateFlow.update { state -> state.copy(error = null) }
                    _stateFlow.value.accountRecoveryKey?.let { confirmClicked() }
                }
                else -> _stateFlow.update { state -> state.copy(error = null) }
            }
        }
    }

    fun cancelClicked() {
        viewModelScope.launch {
            logRepository.queueEvent(CreateAccountRecoveryKey(flowStep = FlowStep.CANCEL))
            _stateFlow.send(AccountRecoveryKeyConfirmState.SideEffect.Cancel)
        }
    }

    fun onBackPressed() {
        viewModelScope.launch {
            _stateFlow.send(AccountRecoveryKeyConfirmState.SideEffect.Back)
        }
    }

    object InvalidKeyException : Exception()
}