package com.dashlane.accountrecoverykey.activation.generate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.accountrecoverykey.AccountRecoveryKeyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AccountRecoveryKeyGenerateViewModel @Inject constructor(
    private val accountRecoveryKeyRepository: AccountRecoveryKeyRepository
) : ViewModel() {

    private val stateFlow =
        MutableStateFlow<AccountRecoveryKeyGenerateState>(AccountRecoveryKeyGenerateState.Initial(AccountRecoveryKeyGenerateData()))
    val uiState = stateFlow.asStateFlow()

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

    fun continueClicked() {
        viewModelScope.launch { stateFlow.emit(AccountRecoveryKeyGenerateState.GoToConfirm(stateFlow.value.data)) }
    }

    fun hasNavigated() {
        viewModelScope.launch { stateFlow.emit(AccountRecoveryKeyGenerateState.Initial(stateFlow.value.data)) }
    }
}