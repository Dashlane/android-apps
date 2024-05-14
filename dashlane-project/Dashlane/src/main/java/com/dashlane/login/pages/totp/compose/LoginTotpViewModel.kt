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
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryNavigation
import com.dashlane.server.api.Response
import com.dashlane.server.api.endpoints.authentication.RequestOtpRecoveryCodesByPhoneService
import com.dashlane.server.api.endpoints.authentication.exceptions.InvalidOtpAlreadyUsedException
import com.dashlane.util.inject.qualifiers.IoCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.jetbrains.annotations.VisibleForTesting

@HiltViewModel
class LoginTotpViewModel @Inject constructor(
    private val secondFactoryRepository: AuthenticationSecondFactoryRepository,
    private val requestOtpRecoveryCodesByPhoneService: RequestOtpRecoveryCodesByPhoneService,
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher,
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

    fun helpClicked() {
        viewModelScope.launch {
            stateFlow.emit(LoginTotpState.Initial(stateFlow.value.data.copy(showHelpDialog = true)))
        }
    }

    fun recoveryCodeClicked() {
        viewModelScope.launch {
            stateFlow.emit(LoginTotpState.Initial(stateFlow.value.data.copy(showHelpDialog = false, showRecoveryCodeDialog = true)))
        }
    }

    fun textMessageClicked() {
        viewModelScope.launch {
            stateFlow.emit(LoginTotpState.Initial(stateFlow.value.data.copy(showHelpDialog = false, showSendTextMessageDialog = true)))
        }
    }

    fun sendTextMessageClicked() {
        flow {
            val response = requestOtpRecoveryCodesByPhoneService.execute(RequestOtpRecoveryCodesByPhoneService.Request(login))
            emit(response)
        }
            .flowOn(ioDispatcher)
            .map<Response<Unit>, LoginTotpState> {
                LoginTotpState.Initial(stateFlow.value.data.copy(showSendTextMessageDialog = false, showTextMessageDialog = true))
            }
            .catch { emit(LoginTotpState.Error(stateFlow.value.data.copy(showSendTextMessageDialog = false), LoginTotpError.Network)) }
            .onStart { emit(LoginTotpState.Loading(stateFlow.value.data.copy(showSendTextMessageDialog = false))) }
            .onEach { state -> stateFlow.emit(state) }
            .launchIn(viewModelScope)
    }

    fun recoveryCancelled() {
        viewModelScope.launch {
            stateFlow.emit(
                LoginTotpState.Initial(
                    stateFlow.value.data.copy(
                        showHelpDialog = false,
                        showSendTextMessageDialog = false,
                        showRecoveryCodeDialog = false,
                        showTextMessageDialog = false
                    )
                )
            )
        }
    }

    fun onOTPChange(otp: String) {
        viewModelScope.launch {
            stateFlow.emit(LoginTotpState.Initial(stateFlow.value.data.copy(otp = otp)))
        }
    }

    fun onNext() {
        validateToken(stateFlow.value.data.otp ?: "")
    }

    fun onRecoveryTokenComplete(token: String) {
        validateToken(token)
    }

    @VisibleForTesting
    fun validateToken(token: String) {
        flow {
            val result = secondFactoryRepository.validate(AuthenticationSecondFactor.Totp(login, setOf(SecurityFeature.TOTP)), token)

            val authTicket = result.authTicket
            if (authTicket == null) {
                emit(LoginTotpState.Error(stateFlow.value.data, LoginTotpError.InvalidToken))
            } else {
                emit(LoginTotpState.Success(stateFlow.value.data, result.registeredUserDevice, authTicket))
            }
        }
            .onStart {
                emit(
                    LoginTotpState.Loading(
                        stateFlow.value.data.copy(
                            recoveryToken = token,
                            showRecoveryCodeDialog = false,
                            showTextMessageDialog = false
                        )
                    )
                )
            }
            .catch { throwable ->
                val error = when (throwable) {
                    is AuthenticationInvalidTokenException,
                    is AuthenticationLockedOutException -> LoginTotpError.InvalidToken
                    is AuthenticationSecondFactorFailedException -> {
                        when (throwable.cause) {
                            is InvalidOtpAlreadyUsedException -> LoginTotpError.AlreadyUsed
                            else -> LoginTotpError.InvalidToken
                        }
                    }
                    is AuthenticationOfflineException -> LoginTotpError.Offline
                    else -> LoginTotpError.Network
                }
                emit(LoginTotpState.Error(stateFlow.value.data, error))
            }
            .onEach { stateFlow.emit(it) }
            .launchIn(viewModelScope)
    }
}