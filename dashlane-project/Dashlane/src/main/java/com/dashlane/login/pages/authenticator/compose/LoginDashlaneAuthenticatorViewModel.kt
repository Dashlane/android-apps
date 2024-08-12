package com.dashlane.login.pages.authenticator.compose

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.authentication.AuthenticationExpiredVersionException
import com.dashlane.authentication.AuthenticationSecondFactor
import com.dashlane.authentication.AuthenticationTimeoutException
import com.dashlane.authentication.SecurityFeature
import com.dashlane.authentication.login.AuthenticationSecondFactoryRepository
import com.dashlane.login.root.LoginDestination.LOGIN_KEY
import com.dashlane.mvvm.State
import com.dashlane.util.extension.takeUntil
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
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

@HiltViewModel
class LoginDashlaneAuthenticatorViewModel @Inject constructor(
    private val secondFactoryRepository: AuthenticationSecondFactoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val login = savedStateHandle.get<String>(LOGIN_KEY) ?: throw IllegalStateException("Email is empty")

    private val stateFlow = MutableStateFlow(LoginDashlaneAuthenticatorState())
    private val navigationStateFlow = Channel<LoginDashlaneAuthenticatorNavigationState>()

    private val cancelFlow = MutableSharedFlow<Unit>()

    val uiState = stateFlow.asStateFlow()
    val navigationState = navigationStateFlow.receiveAsFlow()

    fun viewStarted() {
        loginAuthenticatorPush()
    }

    fun retry() {
        loginAuthenticatorPush()
    }

    fun useTOTP() {
        viewModelScope.launch {
            cancelFlow.emit(Unit)
            navigationStateFlow.send(LoginDashlaneAuthenticatorNavigationState.Canceled)
        }
    }

    @VisibleForTesting
    fun loginAuthenticatorPush() {
        flow {
            val dashlaneAuthenticator = AuthenticationSecondFactor.Authenticator(login, setOf(SecurityFeature.AUTHENTICATOR))
            val result = secondFactoryRepository.validate(dashlaneAuthenticator)

            val authTicket = result.authTicket
            if (authTicket == null) {
                emit(stateFlow.value.copy(isLoading = false, error = LoginDashlaneAuthenticatorError.Generic))
            } else {
                emit(LoginDashlaneAuthenticatorNavigationState.Success(result.registeredUserDevice, authTicket))
            }
        }
            
            .takeUntil(cancelFlow)
            .catch {
                val state = handleErrors(it)
                emit(state)
            }
            .onStart { emit(stateFlow.value.copy(email = login, isLoading = true, error = null)) }
            .onEach { state ->
                when (state) {
                    is LoginDashlaneAuthenticatorState -> stateFlow.update { state }
                    is LoginDashlaneAuthenticatorNavigationState -> {
                        if (state is LoginDashlaneAuthenticatorNavigationState.Success) {
                            stateFlow.update { it.copy(isLoading = false, isSuccess = true) }
                        } else {
                            stateFlow.update { it.copy(isLoading = false) }
                        }
                        delay(500) 
                        navigationStateFlow.send(state)
                        stateFlow.update { it.copy(isSuccess = false) }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun handleErrors(e: Throwable): State {
        val state = stateFlow.value
        return when (e) {
            is AuthenticationTimeoutException -> state.copy(isLoading = false, error = LoginDashlaneAuthenticatorError.Timeout)
            is AuthenticationExpiredVersionException -> state.copy(isLoading = false, error = LoginDashlaneAuthenticatorError.ExpiredVersion)
            else -> state.copy(isLoading = false, error = LoginDashlaneAuthenticatorError.Generic)
        }
    }
}