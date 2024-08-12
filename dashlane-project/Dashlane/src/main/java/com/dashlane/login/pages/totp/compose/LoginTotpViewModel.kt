package com.dashlane.login.pages.totp.compose

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.authentication.AuthenticationInvalidTokenException
import com.dashlane.authentication.AuthenticationLockedOutException
import com.dashlane.authentication.AuthenticationOfflineException
import com.dashlane.authentication.AuthenticationSecondFactor
import com.dashlane.authentication.AuthenticationSecondFactorFailedException
import com.dashlane.authentication.SecurityFeature
import com.dashlane.authentication.login.AuthenticationSecondFactoryRepository
import com.dashlane.hermes.generated.definitions.VerificationMode
import com.dashlane.login.LoginLogger
import com.dashlane.login.root.LoginDestination.AUTHENTICATOR_ENABLED_KEY
import com.dashlane.login.root.LoginDestination.LOGIN_KEY
import com.dashlane.server.api.endpoints.authentication.RequestOtpRecoveryCodesByPhoneService
import com.dashlane.server.api.endpoints.authentication.exceptions.InvalidOtpAlreadyUsedException
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.annotations.VisibleForTesting

@HiltViewModel
class LoginTotpViewModel @Inject constructor(
    private val secondFactoryRepository: AuthenticationSecondFactoryRepository,
    private val requestOtpRecoveryCodesByPhoneService: RequestOtpRecoveryCodesByPhoneService,
    private val loginLogger: LoginLogger,
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val login = savedStateHandle.get<String>(LOGIN_KEY) ?: throw IllegalStateException("Email is empty")
    private val isAuthenticatorEnabled = savedStateHandle.get<Boolean>(AUTHENTICATOR_ENABLED_KEY) ?: false

    private val stateFlow = MutableStateFlow(
        LoginTotpState(
            email = login,
            isAuthenticatorEnabled = isAuthenticatorEnabled
        )
    )
    private val navigationStateFlow = Channel<LoginTotpNavigationState>()

    val uiState = stateFlow.asStateFlow()
    val navigationState = navigationStateFlow.receiveAsFlow()

    fun viewStarted(verificationMode: VerificationMode) {
        viewModelScope.launch {
            stateFlow.update { state -> state.copy(verificationMode = verificationMode) }
        }
    }

    fun pushClicked() {
        viewModelScope.launch {
            navigationStateFlow.send(LoginTotpNavigationState.GoToPush(stateFlow.value.email))
        }
    }

    fun helpClicked() {
        viewModelScope.launch {
            stateFlow.update { state -> state.copy(showHelpDialog = true) }
        }
    }

    fun recoveryCodeClicked() {
        viewModelScope.launch {
            stateFlow.update { state -> state.copy(showHelpDialog = false, showRecoveryCodeDialog = true) }
        }
    }

    fun textMessageClicked() {
        viewModelScope.launch {
            stateFlow.update { state -> state.copy(showHelpDialog = false, showRecoveryCodeDialog = false, showSendTextMessageDialog = true) }
        }
    }

    fun sendTextMessageClicked() {
        flow {
            val email = stateFlow.value.email
            val response = requestOtpRecoveryCodesByPhoneService.execute(RequestOtpRecoveryCodesByPhoneService.Request(email))
            emit(response)
        }
            .flowOn(ioDispatcher)
            .map { stateFlow.value.copy(showSendTextMessageDialog = false, showTextMessageDialog = true) }
            .catch { emit(stateFlow.value.copy(showSendTextMessageDialog = false, error = LoginTotpError.Network)) }
            .onStart { emit(stateFlow.value.copy(showSendTextMessageDialog = false)) }
            .onEach { state -> stateFlow.update { state } }
            .launchIn(viewModelScope)
    }

    fun recoveryCancelled() {
        viewModelScope.launch {
            stateFlow.update { state ->
                state.copy(
                    isRecoveryError = false,
                    showHelpDialog = false,
                    showSendTextMessageDialog = false,
                    showRecoveryCodeDialog = false,
                    showTextMessageDialog = false
                )
            }
        }
    }

    fun onOTPChange(otp: String) {
        viewModelScope.launch {
            stateFlow.update { state -> state.copy(otp = otp, error = null) }
        }
    }

    fun onNext() {
        validateToken(stateFlow.value.otp ?: "", isRecovery = false)
    }

    fun onRecoveryTokenComplete(token: String) {
        validateToken(token = token, isRecovery = true)
    }

    @VisibleForTesting
    fun validateToken(token: String, isRecovery: Boolean) {
        flow {
            val email = stateFlow.value.email
            val result = secondFactoryRepository.validate(AuthenticationSecondFactor.Totp(email, setOf(SecurityFeature.TOTP)), token)

            val authTicket = result.authTicket
            if (authTicket == null) {
                emit(stateFlow.value.copy(isLoading = false, error = LoginTotpError.InvalidToken))
            } else {
                emit(LoginTotpNavigationState.Success(result.registeredUserDevice, authTicket))
            }
        }
            .onStart {
                emit(stateFlow.value.copy(isLoading = true))
            }
            .catch { throwable ->
                if (isRecovery) {
                    emit(stateFlow.value.copy(isLoading = false, isRecoveryError = true))
                } else {
                    emit(stateFlow.value.copy(isLoading = false, error = handleErrors(throwable)))
                }
            }
            .onEach { state ->
                when (state) {
                    is LoginTotpState -> stateFlow.update { state }
                    is LoginTotpNavigationState -> {
                        navigationStateFlow.send(state)
                        stateFlow.update {
                            if (state is LoginTotpNavigationState.Success) {
                                it.copy(isLoading = false, otp = null, showRecoveryCodeDialog = false)
                            } else {
                                it.copy(isLoading = false)
                            }
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    @VisibleForTesting
    fun handleErrors(exception: Throwable): LoginTotpError {
        return when (exception) {
            is AuthenticationLockedOutException -> {
                uiState.value.verificationMode?.let { loginLogger.logWrongOtp(it) }
                LoginTotpError.InvalidTokenLockedOut
            }
            is AuthenticationInvalidTokenException,
            is AuthenticationSecondFactorFailedException -> {
                when (exception.cause) {
                    is InvalidOtpAlreadyUsedException -> LoginTotpError.AlreadyUsed
                    else -> {
                        uiState.value.verificationMode?.let { loginLogger.logWrongOtp(it) }
                        LoginTotpError.InvalidToken
                    }
                }
            }
            is AuthenticationOfflineException -> LoginTotpError.Offline
            else -> LoginTotpError.Network
        }
    }
}