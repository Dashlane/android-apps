package com.dashlane.accountrecoverykey.activation.generate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.accountrecoverykey.AccountRecoveryKeyRepository
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.hermes.generated.definitions.CreateKeyErrorName
import com.dashlane.hermes.generated.definitions.FlowStep
import com.dashlane.hermes.generated.events.user.CreateAccountRecoveryKey
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.user.UserAccountInfo
import com.dashlane.util.clipboard.ClipboardCopy
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AccountRecoveryKeyGenerateViewModel @Inject constructor(
    sessionManager: SessionManager,
    preferencesManager: PreferencesManager,
    private val accountRecoveryKeyRepository: AccountRecoveryKeyRepository,
    private val clipboardCopy: ClipboardCopy,
    private val logRepository: LogRepository
) : ViewModel() {

    private val _stateFlow: MutableViewStateFlow<AccountRecoveryKeyGenerateState.View, AccountRecoveryKeyGenerateState.SideEffect>
    val stateFlow: ViewStateFlow<AccountRecoveryKeyGenerateState.View, AccountRecoveryKeyGenerateState.SideEffect>

    init {
        logRepository.queuePageView(BrowseComponent.MAIN_APP, AnyPage.SETTINGS_SECURITY_RECOVERY_KEY_STORE)

        val session = sessionManager.session ?: throw IllegalStateException("session cannot be null")
        val accountType = preferencesManager[session.username].accountType
            ?.let { UserAccountInfo.AccountType.fromString(it) }
            ?: throw IllegalStateException("accountType cannot be null")

        _stateFlow = MutableViewStateFlow(AccountRecoveryKeyGenerateState.View(accountType = accountType))
        stateFlow = _stateFlow
    }

    fun viewStarted(userCanExitFlow: Boolean) {
        viewModelScope.launch {
            _stateFlow.update { state -> state.copy(userCanExitFlow = userCanExitFlow) }
            if (_stateFlow.value.accountRecoveryKey.isNullOrEmpty()) {
                _stateFlow.update { state -> state.copy(isLoading = true) }
                accountRecoveryKeyRepository.requestActivation()
                    .onSuccess { accountRecoveryKey ->
                        val formattedAccountRecoveryKey = accountRecoveryKey.chunked(4).joinToString("-")
                        _stateFlow.update { state -> state.copy(accountRecoveryKey = formattedAccountRecoveryKey, isLoading = false) }
                    }
                    .onFailure {
                        logRepository.queueEvent(CreateAccountRecoveryKey(flowStep = FlowStep.ERROR, createKeyErrorName = CreateKeyErrorName.UNKNOWN))
                        _stateFlow.update { state -> state.copy(error = AccountRecoveryKeyGenerateError.Generic, isLoading = false) }
                    }
            }
        }
    }

    fun retryClicked() {
        viewStarted(_stateFlow.value.userCanExitFlow)
    }

    fun copy(text: String) {
        clipboardCopy.copyToClipboard(data = text, sensitiveData = true)
    }

    fun continueClicked() {
        viewModelScope.launch { _stateFlow.send(AccountRecoveryKeyGenerateState.SideEffect.GoToConfirm) }
    }

    fun onBackPressed() {
        viewModelScope.launch {
            if (_stateFlow.value.userCanExitFlow) {
                _stateFlow.update { state -> state.copy(cancelDialogShown = true) }
            } else {
                _stateFlow.send(AccountRecoveryKeyGenerateState.SideEffect.Cancel)
            }
        }
    }

    fun cancelDialogDismissed() {
        viewModelScope.launch { _stateFlow.update { state -> state.copy(cancelDialogShown = false) } }
    }

    fun cancelConfirmed() {
        viewModelScope.launch {
            _stateFlow.update { state -> state.copy(cancelDialogShown = false) }
            _stateFlow.send(AccountRecoveryKeyGenerateState.SideEffect.Cancel)
        }
    }
}