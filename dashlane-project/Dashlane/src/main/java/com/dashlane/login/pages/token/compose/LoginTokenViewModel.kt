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
import com.dashlane.debug.DaDaDa
import com.dashlane.hermes.generated.definitions.VerificationMode
import com.dashlane.login.LoginLogger
import com.dashlane.login.root.LoginDestination.LOGIN_KEY
import com.dashlane.mvvm.State
import com.dashlane.server.api.endpoints.authentication.AuthSendEmailTokenService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.annotations.VisibleForTesting

@HiltViewModel
class LoginTokenViewModel @Inject constructor(
    private val secondFactoryRepository: AuthenticationSecondFactoryRepository,
    private val sendEmailTokenService: AuthSendEmailTokenService,
    private val daDaDa: DaDaDa,
    private val loginLogger: LoginLogger,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val login = savedStateHandle.get<String>(LOGIN_KEY) ?: throw IllegalStateException("Email is empty")

    private val stateFlow = MutableStateFlow(LoginTokenState(email = login))
    private val navigationStateFlow = Channel<LoginTokenNavigationState>()

    val uiState = stateFlow.asStateFlow()
    val navigationState = navigationStateFlow.receiveAsFlow()

    fun viewStarted() {
        flow<LoginTokenState> {
            if (stateFlow.value.token == null) {
                debug(login)
                sendEmailTokenService.execute(AuthSendEmailTokenService.Request(login = login))
            }
        }
            .catch { throwable ->
                emit(stateFlow.value.copy(error = LoginTokenError.Network))
            }
            .onEach { state -> stateFlow.update { state } }
            .launchIn(viewModelScope)
    }

    fun onHelpClicked() {
        viewModelScope.launch {
            stateFlow.update { state -> state.copy(showHelpDialog = true) }
        }
    }

    fun onDialogConfirmed() {
        viewModelScope.launch {
            loginLogger.logResendToken()
            secondFactoryRepository.resendToken(AuthenticationSecondFactor.EmailToken(login))
            stateFlow.update { state -> state.copy(showHelpDialog = false) }
        }
    }

    fun onDialogDismissed() {
        viewModelScope.launch {
            stateFlow.update { state -> state.copy(showHelpDialog = false) }
        }
    }

    fun onTokenChange(token: String) {
        viewModelScope.launch {
            stateFlow.update { state -> state.copy(token = token, error = null) }
        }
    }

    fun onNext() {
        validateToken(stateFlow.value.token ?: "")
    }

    @VisibleForTesting
    fun validateToken(token: String) {
        flow<State> {
            val result = secondFactoryRepository.validate(AuthenticationSecondFactor.EmailToken(login), token)
            emit(LoginTokenNavigationState.Success(result.registeredUserDevice as RegisteredUserDevice.Remote, result.authTicket!!))
        }
            .onStart { emit(stateFlow.value.copy(token = token, isLoading = true)) }
            .catch { throwable ->
                when (throwable) {
                    is AuthenticationInvalidTokenException,
                    is AuthenticationLockedOutException -> {
                        loginLogger.logWrongOtp(VerificationMode.EMAIL_TOKEN)
                        emit(stateFlow.value.copy(isLoading = false, error = LoginTokenError.InvalidToken))
                    }
                    is AuthenticationOfflineException -> emit(stateFlow.value.copy(isLoading = false, error = LoginTokenError.Offline))
                    else -> emit(stateFlow.value.copy(isLoading = false, error = LoginTokenError.Network))
                }
            }
            .onEach { state ->
                when (state) {
                    is LoginTokenNavigationState -> {
                        navigationStateFlow.send(state)
                        stateFlow.update { it.copy(isLoading = false) }
                    }
                    is LoginTokenState -> stateFlow.update { state }
                }
            }
            .launchIn(viewModelScope)
    }

    fun debug(username: String) {
        daDaDa.getSecurityToken(username)
            .onEach { token ->
                stateFlow.update { state -> state.copy(token = token) }
                validateToken(token)
            }
            .launchIn(viewModelScope)
    }
}