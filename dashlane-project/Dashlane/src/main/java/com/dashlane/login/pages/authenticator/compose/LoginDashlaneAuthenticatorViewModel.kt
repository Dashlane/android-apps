package com.dashlane.login.pages.authenticator.compose

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.authentication.AuthenticationSecondFactor
import com.dashlane.authentication.SecurityFeature
import com.dashlane.authentication.login.AuthenticationSecondFactoryRepository
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryNavigation
import com.dashlane.util.extension.takeUntil
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@HiltViewModel
class LoginDashlaneAuthenticatorViewModel @Inject constructor(
    private val secondFactoryRepository: AuthenticationSecondFactoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val login = savedStateHandle.get<String>(LoginAccountRecoveryNavigation.LOGIN_KEY) ?: throw IllegalStateException("Email is empty")

    private val stateFlow = MutableStateFlow<LoginDashlaneAuthenticatorState>(LoginDashlaneAuthenticatorState.Initial)
    private val cancelFlow = MutableSharedFlow<Unit>()
    val uiState = stateFlow.asStateFlow()

    fun viewStarted() {
        loginAuthenticatorPush()
    }

    fun hasNavigated() {
        viewModelScope.launch {
            stateFlow.emit(LoginDashlaneAuthenticatorState.Initial)
        }
    }

    fun useTOTP() {
        viewModelScope.launch {
            cancelFlow.emit(Unit)
            stateFlow.emit(LoginDashlaneAuthenticatorState.Canceled)
        }
    }

    @VisibleForTesting
    fun loginAuthenticatorPush() {
        flow {
            val dashlaneAuthenticator = AuthenticationSecondFactor.Authenticator(login, setOf(SecurityFeature.AUTHENTICATOR))
            val result = secondFactoryRepository.validate(dashlaneAuthenticator)

            val authTicket = result.authTicket
            if (authTicket == null) {
                emit(LoginDashlaneAuthenticatorState.Error(LoginDashlaneAuthenticatorError.Network))
            } else {
                emit(LoginDashlaneAuthenticatorState.Success(result.registeredUserDevice, authTicket))
            }
        }
            
            .takeUntil(cancelFlow)
            .catch {
                emit(LoginDashlaneAuthenticatorState.Error(error = LoginDashlaneAuthenticatorError.Network))
            }
            .onStart { emit(LoginDashlaneAuthenticatorState.Loading) }
            .onEach { stateFlow.emit(it) }
            .launchIn(viewModelScope)
    }
}