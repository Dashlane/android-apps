package com.dashlane.wear.login.common.secondfactor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.authentication.AuthenticationOfflineException
import com.dashlane.authentication.login.AuthenticationSecondFactoryRepository
import com.dashlane.data.login.AuthenticationSecondFactor
import com.dashlane.data.login.SecurityFeature
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.State
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.wear.login.WearLoginRepository
import com.dashlane.wear.login.common.secondfactor.OtpState.SideEffect.Success
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WearSecondFactorViewModel @Inject constructor(
    private val loginRepository: WearLoginRepository,
    private val secondFactoryRepository: AuthenticationSecondFactoryRepository,
) : ViewModel() {
    private val _stateFlow = MutableViewStateFlow<OtpState.View, OtpState.SideEffect>(
        OtpState.View(
            token = "",
            isLoading = false,
        ),
    )
    val stateFlow: ViewStateFlow<OtpState.View, OtpState.SideEffect> = _stateFlow

    fun viewStarted() {
        viewModelScope.launch {
            _stateFlow.update { state ->
                state.copy(token = "", error = null)
            }
        }
    }

    fun addDigit(digitChar: Char) {
        if (_stateFlow.viewState.value.isLoading) return

        viewModelScope.launch {
            _stateFlow.update { state ->
                if (state.token.length.toUInt() >= TOKEN_MAX_LENGTH) {
                    
                    return@launch
                }
                state.copy(token = state.token + digitChar, error = null)
            }
        }
    }

    fun deleteLastDigit() {
        if (_stateFlow.viewState.value.isLoading) return

        viewModelScope.launch {
            _stateFlow.update { state ->
                if (state.token.isNotEmpty()) {
                    state.copy(token = state.token.dropLast(1), error = null)
                } else {
                    state.copy(error = null)
                }
            }
        }
    }

    fun validateToken() {
        if (_stateFlow.viewState.value.isLoading) return

        flow {
            val login = loginRepository.getUserLogin() ?: run {
                emit(
                    _stateFlow.value.copy(
                        isLoading = false,
                        error = LoginTokenError.InvalidLogin,
                    ),
                )
                return@flow
            }
            val result = secondFactoryRepository.validate(
                totp = AuthenticationSecondFactor.Totp(
                    login = login,
                    securityFeatures = setOf(SecurityFeature.TOTP),
                ),
                value = _stateFlow.viewState.value.token,
            )

            val authTicket = result.authTicket
            if (authTicket == null) {
                emit(
                    _stateFlow.value.copy(
                        isLoading = false,
                        error = LoginTokenError.InvalidToken,
                    ),
                )
            } else {
                loginRepository.updateRegisteredUserDevice(registeredUserDevice = result.registeredUserDevice)
                emit(Success(login))
            }
        }
            .catch { throwable ->
                val error = when (throwable) {
                    is AuthenticationOfflineException -> LoginTokenError.Offline
                    else -> LoginTokenError.GenericError
                }
                emit(
                    _stateFlow.value.copy(
                        isLoading = false,
                        error = error,
                    ),
                )
            }
            .onStart {
                emit(_stateFlow.value.copy(isLoading = true))
            }
            .onEach { state ->
                when (state) {
                    is OtpState.View -> _stateFlow.update { state }
                    is OtpState.SideEffect -> {
                        _stateFlow.update { it.copy(isLoading = false) }
                        _stateFlow.send(state)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    companion object {
        const val TOKEN_MAX_LENGTH = 9u
    }
}

sealed class OtpState : State {
    data class View(
        val token: String,
        val isLoading: Boolean = false,
        val error: LoginTokenError? = null,
    ) : OtpState(), State.View

    sealed class SideEffect : OtpState(), State.SideEffect {
        data class Success(val login: String) : SideEffect()
    }
}

sealed class LoginTokenError {
    data object InvalidLogin : LoginTokenError()

    data object InvalidToken : LoginTokenError()

    data object GenericError : LoginTokenError()

    data object Offline : LoginTokenError()
}