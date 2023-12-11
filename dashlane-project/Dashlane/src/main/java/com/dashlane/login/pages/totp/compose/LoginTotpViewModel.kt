package com.dashlane.login.pages.totp.compose

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.authentication.AuthenticationInvalidTokenException
import com.dashlane.authentication.AuthenticationLockedOutException
import com.dashlane.authentication.AuthenticationOfflineException
import com.dashlane.authentication.AuthenticationSecondFactor
import com.dashlane.authentication.SecurityFeature
import com.dashlane.authentication.login.AuthenticationSecondFactoryRepository
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

@HiltViewModel
class LoginTotpViewModel @Inject constructor(
    private val secondFactoryRepository: AuthenticationSecondFactoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val login = savedStateHandle.get<String>(LoginAccountRecoveryNavigation.LOGIN_KEY) ?: throw IllegalStateException("Email is empty")
    private val stateFlow = MutableStateFlow<LoginTotpState>(LoginTotpState.Initial(LoginTotpData(email = login)))
    val uiState = stateFlow.asStateFlow()

    fun hasNavigated() {
        viewModelScope.launch {
            stateFlow.emit(LoginTotpState.Initial(stateFlow.value.data))
        }
    }

    fun onTokenCompleted(token: String) {
        flow {
            val result = secondFactoryRepository.validate(AuthenticationSecondFactor.Totp(login, setOf(SecurityFeature.TOTP)), token)

            val authTicket = result.authTicket
            if (authTicket == null) {
                emit(LoginTotpState.Error(stateFlow.value.data, LoginTotpError.InvalidToken))
            } else {
                emit(LoginTotpState.Success(stateFlow.value.data, result.registeredUserDevice, authTicket))
            }
        }
            .onStart { emit(LoginTotpState.Loading(stateFlow.value.data.copy(token = token))) }
            .catch { throwable ->
                when (throwable) {
                    is AuthenticationInvalidTokenException,
                    is AuthenticationLockedOutException -> emit(LoginTotpState.Error(stateFlow.value.data, LoginTotpError.InvalidToken))
                    is AuthenticationOfflineException -> emit(LoginTotpState.Error(stateFlow.value.data, LoginTotpError.Offline))
                    else -> emit(LoginTotpState.Error(stateFlow.value.data, LoginTotpError.Network))
                }
            }
            .onEach { stateFlow.emit(it) }
            .launchIn(viewModelScope)
    }
}