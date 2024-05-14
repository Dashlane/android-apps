package com.dashlane.accountrecoverykey.activation.generate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.account.UserAccountInfo
import com.dashlane.accountrecoverykey.AccountRecoveryKeyRepository
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.hermes.generated.definitions.CreateKeyErrorName
import com.dashlane.hermes.generated.definitions.FlowStep
import com.dashlane.hermes.generated.events.user.CreateAccountRecoveryKey
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.util.clipboard.ClipboardCopy
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AccountRecoveryKeyGenerateViewModel @Inject constructor(
    userPreferencesManager: UserPreferencesManager,
    private val accountRecoveryKeyRepository: AccountRecoveryKeyRepository,
    private val clipboardCopy: ClipboardCopy,
    private val logRepository: LogRepository
) : ViewModel() {

    private val stateFlow: MutableStateFlow<AccountRecoveryKeyGenerateState>
    val uiState: StateFlow<AccountRecoveryKeyGenerateState>

    init {
        logRepository.queuePageView(BrowseComponent.MAIN_APP, AnyPage.SETTINGS_SECURITY_RECOVERY_KEY_STORE)

        val accountType = UserAccountInfo.AccountType.fromString(userPreferencesManager.accountType)
        stateFlow = MutableStateFlow(AccountRecoveryKeyGenerateState.Initial(AccountRecoveryKeyGenerateData(accountType = accountType)))
        uiState = stateFlow.asStateFlow()
    }

    fun viewStarted() {
        viewModelScope.launch {
            if (stateFlow.value.data.accountRecoveryKey.isNullOrEmpty()) {
                stateFlow.emit(AccountRecoveryKeyGenerateState.Loading(stateFlow.value.data))
                accountRecoveryKeyRepository.requestActivation()
                    .onSuccess { accountRecoveryKey ->
                        val formattedAccountRecoveryKey = accountRecoveryKey.chunked(4).joinToString("-")
                        val newData = stateFlow.value.data.copy(accountRecoveryKey = formattedAccountRecoveryKey)
                        stateFlow.emit(AccountRecoveryKeyGenerateState.KeyGenerated(newData))
                    }
                    .onFailure {
                        logRepository.queueEvent(CreateAccountRecoveryKey(flowStep = FlowStep.ERROR, createKeyErrorName = CreateKeyErrorName.UNKNOWN))
                        stateFlow.emit(AccountRecoveryKeyGenerateState.Error(stateFlow.value.data))
                    }
            } else {
                stateFlow.emit(AccountRecoveryKeyGenerateState.KeyGenerated(stateFlow.value.data))
            }
        }
    }

    fun retryClicked() {
        viewStarted()
    }

    fun copy(text: String) {
        clipboardCopy.copyToClipboard(data = text, sensitiveData = true)
    }

    fun continueClicked() {
        viewModelScope.launch { stateFlow.emit(AccountRecoveryKeyGenerateState.GoToConfirm(stateFlow.value.data)) }
    }

    fun hasNavigated() {
        viewModelScope.launch { stateFlow.emit(AccountRecoveryKeyGenerateState.Initial(stateFlow.value.data)) }
    }

    fun onBackPressed() {
        viewModelScope.launch { stateFlow.emit(AccountRecoveryKeyGenerateState.KeyGenerated(stateFlow.value.data.copy(cancelDialogShown = true))) }
    }

    fun cancelDialogDismissed() {
        viewModelScope.launch { stateFlow.emit(AccountRecoveryKeyGenerateState.KeyGenerated(stateFlow.value.data.copy(cancelDialogShown = false))) }
    }
    fun cancelConfirmed() {
        viewModelScope.launch { stateFlow.emit(AccountRecoveryKeyGenerateState.Cancel(stateFlow.value.data.copy(cancelDialogShown = false))) }
    }
}