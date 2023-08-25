package com.dashlane.login.accountrecoverykey.enterark

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryKeyRepository
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryNavigation
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
import org.jetbrains.annotations.VisibleForTesting

@HiltViewModel
class EnterARKViewModel @Inject constructor(
    private val loginAccountRecoveryKeyRepository: LoginAccountRecoveryKeyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val login = savedStateHandle.get<String>(LoginAccountRecoveryNavigation.LOGIN_KEY) ?: throw IllegalStateException("Email is empty")
    private val stateFlow = MutableStateFlow<EnterARKState>(EnterARKState.Initial(EnterARKData()))
    val uiState = stateFlow.asStateFlow()

    fun onNextClicked(accountRecoveryKey: String, authTicket: String?) {
        flow {
            val formattedAccountRecoveryKey = accountRecoveryKey.replace("-", "")
            if (authTicket == null || !checkAccountRecoveryKeyFormat(formattedAccountRecoveryKey)) {
                emit(EnterARKState.Error(stateFlow.value.data.copy(accountRecoveryKey = accountRecoveryKey)))
                return@flow
            }
            val result = loginAccountRecoveryKeyRepository.verifyAccountRecoveryKey(login, formattedAccountRecoveryKey, authTicket)
            when {
                result.isSuccess -> emit(EnterARKState.KeyConfirmed(stateFlow.value.data, result.getOrThrow()))
                result.isFailure -> emit(EnterARKState.Error(stateFlow.value.data))
            }
        }
            .onStart { emit(EnterARKState.Loading(stateFlow.value.data)) }
            .catch { throwable ->
                emit(EnterARKState.Error(stateFlow.value.data))
            }
            .onEach { state -> stateFlow.emit(state) }
            .launchIn(viewModelScope)
    }

    fun animationEnded(decryptedVaultKey: String) {
        viewModelScope.launch { stateFlow.emit(EnterARKState.GoToNext(stateFlow.value.data, decryptedVaultKey)) }
    }

    fun hasNavigated() {
        viewModelScope.launch { stateFlow.emit(EnterARKState.Initial(stateFlow.value.data)) }
    }

    @VisibleForTesting
    fun checkAccountRecoveryKeyFormat(accountRecoveryKey: String): Boolean {
        if (accountRecoveryKey.length != 28) return false
        accountRecoveryKey.forEach {
            if (it !in 'A'..'Z' && it !in '0'..'9') {
                return false
            }
        }
        return true
    }
}
