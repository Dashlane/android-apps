package com.dashlane.login.accountrecoverykey.enterark

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.account.UserAccountInfo
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.hermes.generated.definitions.FlowStep
import com.dashlane.hermes.generated.definitions.UseKeyErrorName
import com.dashlane.hermes.generated.events.user.UseAccountRecoveryKey
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryKeyRepository
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryNavigation
import com.dashlane.util.inject.qualifiers.IoCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.jetbrains.annotations.VisibleForTesting

@HiltViewModel
class EnterARKViewModel @Inject constructor(
    private val loginAccountRecoveryKeyRepository: LoginAccountRecoveryKeyRepository,
    private val logRepository: LogRepository,
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val login = savedStateHandle.get<String>(LoginAccountRecoveryNavigation.LOGIN_KEY) ?: throw IllegalStateException("Email is empty")

    private val stateFlow: MutableStateFlow<EnterARKState>
    val uiState: StateFlow<EnterARKState>

    init {
        val accountType = loginAccountRecoveryKeyRepository.state.value.accountType ?: UserAccountInfo.AccountType.MasterPassword
        stateFlow = MutableStateFlow(EnterARKState.Initial(EnterARKData(accountType = accountType)))
        uiState = stateFlow.asStateFlow()
        logRepository.queuePageView(BrowseComponent.MAIN_APP, AnyPage.LOGIN_MASTER_PASSWORD_ACCOUNT_RECOVERY_ENTER_RECOVERY_KEY)
    }

    fun onNextClicked(accountRecoveryKey: String) {
        flow {
            val formattedAccountRecoveryKey = accountRecoveryKey.replace("-", "")
            val authTicket = loginAccountRecoveryKeyRepository.state.value.authTicket
            if (authTicket == null || !checkAccountRecoveryKeyFormat(formattedAccountRecoveryKey)) {
                logRepository.queueEvent(UseAccountRecoveryKey(flowStep = FlowStep.ERROR, useKeyErrorName = UseKeyErrorName.WRONG_KEY_ENTERED))
                emit(EnterARKState.Error(stateFlow.value.data.copy(accountRecoveryKey = accountRecoveryKey)))
                return@flow
            }
            loginAccountRecoveryKeyRepository.verifyAccountRecoveryKey(login, formattedAccountRecoveryKey, authTicket)
                .onSuccess { obfuscatedVaultKey ->
                    emit(EnterARKState.KeyConfirmed(stateFlow.value.data, obfuscatedVaultKey))
                }
                .onFailure {
                    logRepository.queueEvent(UseAccountRecoveryKey(flowStep = FlowStep.ERROR, useKeyErrorName = UseKeyErrorName.WRONG_KEY_ENTERED))
                    emit(EnterARKState.Error(stateFlow.value.data.copy(accountRecoveryKey = accountRecoveryKey)))
                }
        }
            .flowOn(ioDispatcher)
            .catch { throwable ->
                logRepository.queueEvent(UseAccountRecoveryKey(flowStep = FlowStep.ERROR, useKeyErrorName = UseKeyErrorName.UNKNOWN))
                emit(EnterARKState.Error(stateFlow.value.data.copy(accountRecoveryKey = accountRecoveryKey)))
            }
            .onStart { emit(EnterARKState.Loading(stateFlow.value.data)) }
            .onEach { state -> stateFlow.emit(state) }
            .launchIn(viewModelScope)
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
