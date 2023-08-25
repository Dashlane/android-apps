package com.dashlane.login.pages.token.compose

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.authentication.AuthenticationInvalidTokenException
import com.dashlane.authentication.AuthenticationLockedOutException
import com.dashlane.authentication.AuthenticationOfflineException
import com.dashlane.authentication.AuthenticationSecondFactor
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.authentication.login.AuthenticationSecondFactoryRepository
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryNavigation
import com.dashlane.server.api.endpoints.authentication.AuthSendEmailTokenService
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
class LoginTokenViewModel @Inject constructor(
    private val secondFactoryRepository: AuthenticationSecondFactoryRepository,
    private val sendEmailTokenService: AuthSendEmailTokenService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val login = savedStateHandle.get<String>(LoginAccountRecoveryNavigation.LOGIN_KEY) ?: throw IllegalStateException("Email is empty")
    private val stateFlow = MutableStateFlow<LoginTokenState>(LoginTokenState.Initial(LoginTokenData(email = login)))
    val uiState = stateFlow.asStateFlow()

    init {
        flow<LoginTokenState> {
            sendEmailTokenService.execute(AuthSendEmailTokenService.Request(login = login))
        }
            .catch { throwable ->
                emit(LoginTokenState.Error(stateFlow.value.data, LoginTokenError.Network))
            }
            .launchIn(viewModelScope)
    }

    fun hasNavigated() {
        viewModelScope.launch {
            stateFlow.emit(LoginTokenState.Initial(stateFlow.value.data))
        }
    }

    fun onHelpClicked() {
        viewModelScope.launch {
            secondFactoryRepository.resendToken(AuthenticationSecondFactor.EmailToken(login))
        }
    }

    fun onTokenCompleted(token: String) {
        flow<LoginTokenState> {
            val result = secondFactoryRepository.validate(AuthenticationSecondFactor.EmailToken(login), token)
            emit(LoginTokenState.Success(stateFlow.value.data, result.registeredUserDevice as RegisteredUserDevice.Remote, result.authTicket!!))
        }
            .onStart { emit(LoginTokenState.Loading(stateFlow.value.data.copy(token = token))) }
            .catch { throwable ->
                when (throwable) {
                    is AuthenticationInvalidTokenException,
                    is AuthenticationLockedOutException -> emit(LoginTokenState.Error(stateFlow.value.data, LoginTokenError.InvalidToken))
                    is AuthenticationOfflineException -> emit(LoginTokenState.Error(stateFlow.value.data, LoginTokenError.Offline))
                    else -> emit(LoginTokenState.Error(stateFlow.value.data, LoginTokenError.Network))
                }
            }
            .onEach { stateFlow.emit(it) }
            .launchIn(viewModelScope)
    }
}