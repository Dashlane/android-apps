package com.dashlane.login.pages.sso.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.user.UserAccountInfo
import com.dashlane.authentication.AuthenticationInvalidSsoException
import com.dashlane.authentication.AuthenticationNetworkException
import com.dashlane.authentication.AuthenticationOfflineException
import com.dashlane.authentication.login.AuthenticationSsoRepository
import com.dashlane.authentication.sso.GetSsoInfoResult
import com.dashlane.login.LoginLogger
import com.dashlane.login.LoginMode
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockPass
import com.dashlane.login.lock.LockSetting
import com.dashlane.login.pages.sso.SsoLockContract
import com.dashlane.mvvm.State
import com.dashlane.preference.GlobalPreferencesManager
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
class LoginSsoViewModel @Inject constructor(
    private val ssoRepository: AuthenticationSsoRepository,
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val lockManager: LockManager,
    private val loginLogger: LoginLogger,
) : ViewModel() {

    private val stateFlow = MutableStateFlow(LoginSsoState())
    private val navigationStateFlow = Channel<LoginSsoNavigationState>()

    val uiState = stateFlow.asStateFlow()
    val navigationState = navigationStateFlow.receiveAsFlow()

    fun viewStarted(userAccountInfo: UserAccountInfo, lockSetting: LockSetting) {
        viewModelScope.launch {
            val email = userAccountInfo.username
            val loginHistory = listOf(email) + (globalPreferencesManager.getUserListHistory() - email).filter { it != "" }

            stateFlow.update { state ->
                state.copy(
                    userAccountInfo = userAccountInfo,
                    loginHistory = loginHistory,
                    lockSetting = lockSetting,
                )
            }
        }
    }

    fun changeAccount(email: String?) {
        viewModelScope.launch {
            navigationStateFlow.send(LoginSsoNavigationState.ChangeAccount(email = email ?: ""))
        }
    }

    fun onNextClicked() {
        viewModelScope.launch {
            stateFlow.update { state -> state.copy(isLoading = true, error = null) }
            val userAccountInfo = stateFlow.value.userAccountInfo ?: throw IllegalStateException("userAccountInfo cannot be null")
            val ssoInfo = ssoRepository.getSsoInfo(userAccountInfo.username, userAccountInfo.accessKey)
            navigationStateFlow.send(LoginSsoNavigationState.GoToSso(ssoInfo))
        }
    }

    fun onCancelClicked() {
        viewModelScope.launch {
            navigationStateFlow.send(LoginSsoNavigationState.Cancel)
        }
    }

    fun ssoComplete(result: GetSsoInfoResult) {
        viewModelScope.launch {
            when (result) {
                is GetSsoInfoResult.Error -> {
                    val error = handleSSOError(result)
                    stateFlow.update { state -> state.copy(isLoading = false, error = error) }
                }
                is GetSsoInfoResult.Success -> validateSso(result)
            }
        }
    }

    @VisibleForTesting
    fun validateSso(result: GetSsoInfoResult.Success) {
        flow<State> {
            val userAccountInfo = stateFlow.value.userAccountInfo ?: throw IllegalStateException("userAccountInfo cannot be null")
            val userSsoInfo = result.userSsoInfo
            if (userSsoInfo.login != userAccountInfo.username) throw SsoLockContract.NoSessionLoadedException()

            val validateResult = ssoRepository.validate(
                login = userAccountInfo.username,
                ssoToken = userSsoInfo.ssoToken,
                serviceProviderKey = userSsoInfo.key,
                accessKey = userAccountInfo.accessKey
            )

            check(validateResult is AuthenticationSsoRepository.ValidateResult.Local)

            lockManager.unlock(LockPass.ofPassword(validateResult.ssoKey))

            loginLogger.logSuccess(loginMode = LoginMode.Sso)
            stateFlow.value.lockSetting?.unlockReason?.let { reason -> runCatching { lockManager.sendUnLock(reason, true) } }

            emit(LoginSsoNavigationState.UnlockSuccess)
        }
            .catch { e ->
                emit(handleErrors(exception = e))
            }
            .onStart { emit(stateFlow.value.copy(isLoading = true)) }
            .onEach { state ->
                when (state) {
                    is LoginSsoState -> stateFlow.update { state }
                    is LoginSsoNavigationState -> {
                        stateFlow.update { it.copy(isLoading = false) }
                        navigationStateFlow.send(state)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    @VisibleForTesting
    fun handleSSOError(error: GetSsoInfoResult.Error): LoginSsoError {
        return when (error) {
            GetSsoInfoResult.Error.CannotOpenServiceProvider,
            GetSsoInfoResult.Error.SamlResponseNotFound,
            GetSsoInfoResult.Error.UnauthorizedNavigation,
            GetSsoInfoResult.Error.Unknown -> {
                loginLogger.logErrorUnknown(loginMode = LoginMode.Sso)
                LoginSsoError.Generic
            }
        }
    }

    @VisibleForTesting
    fun handleErrors(exception: Throwable): LoginSsoState {
        val error = when (exception) {
            is AuthenticationOfflineException -> LoginSsoError.Offline
            is AuthenticationNetworkException -> LoginSsoError.Network
            is AuthenticationInvalidSsoException -> {
                loginLogger.logInvalidSso()
                LoginSsoError.InvalidSso
            }
            else -> {
                loginLogger.logErrorUnknown(loginMode = LoginMode.Sso)
                LoginSsoError.Generic
            }
        }
        return stateFlow.value.copy(error = error)
    }
}