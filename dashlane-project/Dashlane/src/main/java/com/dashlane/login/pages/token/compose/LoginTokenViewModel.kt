package com.dashlane.login.pages.token.compose

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.dashlane.authentication.AuthenticationInvalidTokenException
import com.dashlane.authentication.AuthenticationLockedOutException
import com.dashlane.authentication.AuthenticationOfflineException
import com.dashlane.authentication.AuthenticationSecondFactor
import com.dashlane.authentication.login.AuthenticationSecondFactoryRepository
import com.dashlane.debug.services.DaDaDaLogin
import com.dashlane.hermes.generated.definitions.VerificationMode
import com.dashlane.login.LoginLogger
import com.dashlane.login.root.LoginDestination.Token
import com.dashlane.login.root.LoginRepository
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.State
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.server.api.endpoints.authentication.AuthSendEmailTokenService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.annotations.VisibleForTesting
import javax.inject.Inject

@HiltViewModel
class LoginTokenViewModel @Inject constructor(
    private val secondFactoryRepository: AuthenticationSecondFactoryRepository,
    private val sendEmailTokenService: AuthSendEmailTokenService,
    private val dadadaLogin: DaDaDaLogin,
    private val loginLogger: LoginLogger,
    private val loginRepository: LoginRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val login = savedStateHandle.toRoute<Token>().login

    private val _stateFlow = MutableViewStateFlow<LoginTokenState.View, LoginTokenState.SideEffect>(LoginTokenState.View(login))
    val stateFlow: ViewStateFlow<LoginTokenState.View, LoginTokenState.SideEffect> = _stateFlow

    fun viewStarted() {
        flow<LoginTokenState.View> {
            if (_stateFlow.value.token == null) {
                debug(login)
                sendEmailTokenService.execute(AuthSendEmailTokenService.Request(login = login))
            }
        }
            .catch { throwable ->
                emit(_stateFlow.value.copy(error = LoginTokenError.Network))
            }
            .onEach { state -> _stateFlow.update { state } }
            .launchIn(viewModelScope)
    }

    fun onHelpClicked() {
        viewModelScope.launch {
            _stateFlow.update { state -> state.copy(showHelpDialog = true) }
        }
    }

    fun onDialogConfirmed() {
        viewModelScope.launch {
            loginLogger.logResendToken()
            secondFactoryRepository.resendToken(AuthenticationSecondFactor.EmailToken(login))
            _stateFlow.update { state -> state.copy(showHelpDialog = false) }
        }
    }

    fun onDialogDismissed() {
        viewModelScope.launch {
            _stateFlow.update { state -> state.copy(showHelpDialog = false) }
        }
    }

    fun onTokenChange(token: String) {
        viewModelScope.launch {
            _stateFlow.update { state -> state.copy(token = token, error = null) }
        }
    }

    fun onNext() {
        validateToken(_stateFlow.value.token ?: "")
    }

    @VisibleForTesting
    fun validateToken(token: String) {
        flow<State> {
            val result = secondFactoryRepository.validate(AuthenticationSecondFactor.EmailToken(login), token)
            val authTicket = result.authTicket ?: throw AuthenticationInvalidTokenException()

            loginRepository.updateRegisteredUserDevice(registeredUserDevice = result.registeredUserDevice)
            loginRepository.updateAuthTicket(authTicket)
            emit(LoginTokenState.SideEffect.Success)
        }
            .onStart { emit(_stateFlow.value.copy(token = token, isLoading = true)) }
            .catch { throwable ->
                when (throwable) {
                    is AuthenticationInvalidTokenException,
                    is AuthenticationLockedOutException -> {
                        loginLogger.logWrongOtp(VerificationMode.EMAIL_TOKEN)
                        emit(_stateFlow.value.copy(isLoading = false, error = LoginTokenError.InvalidToken))
                    }
                    is AuthenticationOfflineException -> emit(_stateFlow.value.copy(isLoading = false, error = LoginTokenError.Offline))
                    else -> emit(_stateFlow.value.copy(isLoading = false, error = LoginTokenError.Network))
                }
            }
            .onEach { state ->
                when (state) {
                    is LoginTokenState.SideEffect -> {
                        _stateFlow.send(state)
                        _stateFlow.update { it.copy(isLoading = false) }
                    }
                    is LoginTokenState.View -> _stateFlow.update { state }
                }
            }
            .launchIn(viewModelScope)
    }

    fun debug(username: String) {
        dadadaLogin.getSecurityToken(username)
            .onEach { token ->
                _stateFlow.update { state -> state.copy(token = token) }
                validateToken(token)
            }
            .launchIn(viewModelScope)
    }
}