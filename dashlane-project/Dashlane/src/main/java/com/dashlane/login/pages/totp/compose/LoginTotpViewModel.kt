package com.dashlane.login.pages.totp.compose

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.dashlane.authentication.AuthenticationInvalidTokenException
import com.dashlane.authentication.AuthenticationLockedOutException
import com.dashlane.authentication.AuthenticationOfflineException
import com.dashlane.authentication.AuthenticationSecondFactor
import com.dashlane.authentication.AuthenticationSecondFactorFailedException
import com.dashlane.authentication.SecurityFeature
import com.dashlane.authentication.login.AuthenticationSecondFactoryRepository
import com.dashlane.hermes.generated.definitions.VerificationMode
import com.dashlane.lock.LockTypeManager
import com.dashlane.login.LoginLogger
import com.dashlane.login.pages.biometric.recovery.BiometricRecoveryDestination.Otp
import com.dashlane.login.root.LoginRepository
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.server.api.endpoints.authentication.RequestOtpRecoveryCodesByPhoneService
import com.dashlane.server.api.endpoints.authentication.exceptions.InvalidOtpAlreadyUsedException
import com.dashlane.session.SessionRestorer
import com.dashlane.user.Username
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.annotations.VisibleForTesting
import javax.inject.Inject

@HiltViewModel
class LoginTotpViewModel @Inject constructor(
    private val secondFactoryRepository: AuthenticationSecondFactoryRepository,
    private val requestOtpRecoveryCodesByPhoneService: RequestOtpRecoveryCodesByPhoneService,
    private val loginRepository: LoginRepository,
    private val loginLogger: LoginLogger,
    private val sessionRestorer: SessionRestorer,
    private val lockTypeManager: LockTypeManager,
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val login = savedStateHandle.toRoute<Otp>().login
    private val restoreSession = savedStateHandle.toRoute<Otp>().restoreSession

    private val _stateFlow = MutableViewStateFlow<LoginTotpState.View, LoginTotpState.SideEffect>(
        LoginTotpState.View(email = login, restoreSession = restoreSession)
    )
    val stateFlow: ViewStateFlow<LoginTotpState.View, LoginTotpState.SideEffect> = _stateFlow

    fun viewStarted(verificationMode: VerificationMode) {
        viewModelScope.launch {
            _stateFlow.update { state -> state.copy(verificationMode = verificationMode) }
        }
    }

    fun helpClicked() {
        viewModelScope.launch {
            _stateFlow.update { state -> state.copy(showHelpDialog = true) }
        }
    }

    fun recoveryCodeClicked() {
        viewModelScope.launch {
            _stateFlow.update { state -> state.copy(showHelpDialog = false, showRecoveryCodeDialog = true) }
        }
    }

    fun textMessageClicked() {
        viewModelScope.launch {
            _stateFlow.update { state -> state.copy(showHelpDialog = false, showRecoveryCodeDialog = false, showSendTextMessageDialog = true) }
        }
    }

    fun sendTextMessageClicked() {
        flow {
            val email = _stateFlow.value.email
            val response = requestOtpRecoveryCodesByPhoneService.execute(RequestOtpRecoveryCodesByPhoneService.Request(email))
            emit(response)
        }
            .flowOn(ioDispatcher)
            .map { _stateFlow.value.copy(showSendTextMessageDialog = false, showTextMessageDialog = true) }
            .catch { emit(_stateFlow.value.copy(showSendTextMessageDialog = false, error = LoginTotpError.Network)) }
            .onStart { emit(_stateFlow.value.copy(showSendTextMessageDialog = false)) }
            .onEach { state -> _stateFlow.update { state } }
            .launchIn(viewModelScope)
    }

    fun recoveryCancelled() {
        viewModelScope.launch {
            _stateFlow.update { state ->
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
            _stateFlow.update { state -> state.copy(otp = otp, error = null) }
        }
    }

    fun onNext() {
        validateToken(_stateFlow.value.otp ?: "", isRecovery = false)
    }

    fun onRecoveryTokenComplete(token: String) {
        validateToken(token = token, isRecovery = true)
    }

    @VisibleForTesting
    fun validateToken(token: String, isRecovery: Boolean) {
        flow {
            val email = _stateFlow.value.email
            val result = secondFactoryRepository.validate(AuthenticationSecondFactor.Totp(email, setOf(SecurityFeature.TOTP)), token)

            val authTicket = result.authTicket
            if (authTicket == null) {
                emit(_stateFlow.value.copy(isLoading = false, error = LoginTotpError.InvalidToken))
            } else {
                loginRepository.updateRegisteredUserDevice(registeredUserDevice = result.registeredUserDevice)
                loginRepository.updateAuthTicket(authTicket = authTicket)

                if (restoreSession) {
                    val username = Username.ofEmail(email)
                    sessionRestorer.restoreSession(username, result.registeredUserDevice.serverKey)
                    val locks = lockTypeManager.getLocks(username)
                    emit(LoginTotpState.SideEffect.Success(locks))
                } else {
                    emit(LoginTotpState.SideEffect.Success(emptyList()))
                }
            }
        }
            .onStart {
                emit(_stateFlow.value.copy(isLoading = true))
            }
            .catch { throwable ->
                if (isRecovery) {
                    emit(_stateFlow.value.copy(isLoading = false, isRecoveryError = true))
                } else {
                    emit(_stateFlow.value.copy(isLoading = false, error = handleErrors(throwable)))
                }
            }
            .onEach { state ->
                when (state) {
                    is LoginTotpState.View -> _stateFlow.update { state }
                    is LoginTotpState.SideEffect -> {
                        _stateFlow.send(state)
                        _stateFlow.update {
                            if (state is LoginTotpState.SideEffect.Success) {
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
                _stateFlow.value.verificationMode?.let { loginLogger.logWrongOtp(it) }
                LoginTotpError.InvalidTokenLockedOut
            }
            is AuthenticationInvalidTokenException,
            is AuthenticationSecondFactorFailedException -> {
                when (exception.cause) {
                    is InvalidOtpAlreadyUsedException -> LoginTotpError.AlreadyUsed
                    else -> {
                        _stateFlow.value.verificationMode?.let { loginLogger.logWrongOtp(it) }
                        LoginTotpError.InvalidToken
                    }
                }
            }
            is AuthenticationOfflineException -> LoginTotpError.Offline
            else -> LoginTotpError.Network
        }
    }
}