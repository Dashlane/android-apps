package com.dashlane.accountrecoverykey.activation.confirm

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
class AccountRecoveryKeyConfirmViewModel @Inject constructor(
    private val accountRecoveryKeyRepository: AccountRecoveryKeyRepository,
    private val dataSync: DataSync
) : ViewModel() {

    private val stateFlow = MutableStateFlow<AccountRecoveryKeyConfirmState>(AccountRecoveryKeyConfirmState.Initial(AccountRecoveryKeyConfirmData()))
    val uiState = stateFlow.asStateFlow()

    fun confirmClicked(accountRecoveryKey: String) {
        flow<AccountRecoveryKeyConfirmState> {
            val keyConfirmed = accountRecoveryKeyRepository.confirmRecoveryKey(accountRecoveryKey.replace("-", ""))

            if (!keyConfirmed) throw InvalidKeyException

            val syncSuccess = dataSync.awaitSync()

            if (!syncSuccess) throw IllegalStateException("")

            accountRecoveryKeyRepository.confirmActivation()
                .onSuccess { emit(AccountRecoveryKeyConfirmState.KeyConfirmed(stateFlow.value.data)) }
                .onFailure { throw it }
        }
            .catch { throwable ->
                val errorState = when (throwable) {
                    is InvalidKeyException -> AccountRecoveryKeyConfirmState.KeyError(stateFlow.value.data.copy(accountRecoveryKey = accountRecoveryKey))
                    else -> AccountRecoveryKeyConfirmState.SyncError(stateFlow.value.data.copy(accountRecoveryKey = accountRecoveryKey))
                }
                emit(errorState)
            }
            .onStart {
                emit(AccountRecoveryKeyConfirmState.Loading(stateFlow.value.data.copy(accountRecoveryKey = accountRecoveryKey)))
            }
            .onEach { stateFlow.emit(it) }
            .launchIn(viewModelScope)
    }

    fun retryClicked() {
        viewModelScope.launch {
            when (val state = stateFlow.value) {
                is AccountRecoveryKeyConfirmState.SyncError -> {
                    state.data.accountRecoveryKey?.let { confirmClicked(it) }
                        ?: run { stateFlow.emit(AccountRecoveryKeyConfirmState.Initial(state.data)) }
                }

                else -> stateFlow.emit(AccountRecoveryKeyConfirmState.Initial(state.data))
            }
        }
    }

    fun doneClicked() {
        viewModelScope.launch { stateFlow.emit(AccountRecoveryKeyConfirmState.Done(stateFlow.value.data)) }
    }

    fun hasNavigated() {
        viewModelScope.launch { stateFlow.emit(AccountRecoveryKeyConfirmState.KeyConfirmed(stateFlow.value.data)) }
    }

    object InvalidKeyException : Exception()
}