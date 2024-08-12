package com.dashlane.login.pages.pin.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.user.UserAccountInfo
import com.dashlane.login.LoginLogger
import com.dashlane.login.LoginMode
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockPass
import com.dashlane.login.lock.LockSetting
import com.dashlane.pin.setup.PinSetupViewModel
import com.dashlane.security.SecurityHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginPinViewModel @Inject constructor(
    private val securityHelper: SecurityHelper,
    private val lockManager: LockManager,
    private val loginLogger: LoginLogger
) : ViewModel() {

    private val stateFlow: MutableStateFlow<LoginPinState> = MutableStateFlow(LoginPinState())
    private val navigationStateFlow = MutableSharedFlow<LoginPinNavigationState>()

    val uiState: StateFlow<LoginPinState> = stateFlow.asStateFlow()
    val navigationState = navigationStateFlow.asSharedFlow()

    fun viewStarted(userAccountInfo: UserAccountInfo, lockSetting: LockSetting) {
        viewModelScope.launch {
            val isSystemLockSetup = securityHelper.isDeviceSecured()
            val fallback = when {
                lockSetting.isLockCancelable -> LoginPinFallback.Cancellable
                userAccountInfo.sso -> LoginPinFallback.SSO
                userAccountInfo.accountType is UserAccountInfo.AccountType.InvisibleMasterPassword -> LoginPinFallback.MPLess
                else -> LoginPinFallback.MP
            }
            stateFlow.update { state ->
                state.copy(
                    email = userAccountInfo.username,
                    isSystemLockSetup = isSystemLockSetup,
                    lockSetting = lockSetting,
                    fallback = fallback,
                )
            }
        }
    }

    fun onGoToSystemLockSetting() {
        viewModelScope.launch {
            securityHelper.intentHelper.findEnableDeviceLockIntent()?.let { intent ->
                navigationStateFlow.emit(LoginPinNavigationState.GoToSystemLockSetting(intent))
            }
        }
    }

    fun onClickForgot() {
        viewModelScope.launch {
            when (val fallback = stateFlow.value.fallback) {
                LoginPinFallback.MPLess -> stateFlow.update { state -> state.copy(helpDialogShown = true) }
                else -> navigationStateFlow.emit(LoginPinNavigationState.Cancel(fallback))
            }
        }
    }

    fun bottomSheetDismissed() {
        viewModelScope.launch {
            stateFlow.update { state -> state.copy(helpDialogShown = false) }
        }
    }

    fun onClickD2D() {
        viewModelScope.launch {
            stateFlow.update { state -> state.copy(helpDialogShown = false) }
            val email = stateFlow.value.email ?: throw IllegalStateException("email cannot be null")
            navigationStateFlow.emit(LoginPinNavigationState.GoToSecretTransfer(email))
        }
    }

    fun onClickRecovery() {
        viewModelScope.launch {
            stateFlow.update { state -> state.copy(helpDialogShown = false) }
            val email = stateFlow.value.email ?: throw IllegalStateException("email cannot be null")
            navigationStateFlow.emit(LoginPinNavigationState.GoToRecoveryHelp(email))
        }
    }

    fun onPinUpdated(pinCode: String) {
        viewModelScope.launch {
            stateFlow.update { state -> state.copy(pinCode = pinCode, error = null) }
            delay(100) 
            if (pinCode.length == PinSetupViewModel.PIN_LENGTH) {
                validatePin(pinCode)
            }
        }
    }

    @org.jetbrains.annotations.VisibleForTesting
    @Suppress("kotlin:S6313") 
    suspend fun validatePin(pinCode: String) {
        if (lockManager.unlock(LockPass.ofPin(pinCode))) {
            loginLogger.logSuccess(loginMode = LoginMode.Pin)
            uiState.value.lockSetting?.unlockReason?.let { reason -> runCatching { lockManager.sendUnLock(reason, true) } }
            navigationStateFlow.emit(LoginPinNavigationState.UnlockSuccess)
        } else {
            loginLogger.logWrongPin()
            lockManager.addFailUnlockAttempt()
            if (lockManager.hasFailedUnlockTooManyTimes()) {
                navigationStateFlow.emit(LoginPinNavigationState.Logout(email = stateFlow.value.email, errorMessage = null))
            } else {
                val attempt = lockManager.getFailUnlockAttemptCount()
                stateFlow.update { state -> state.copy(pinCode = "", error = LoginPinError.WrongPin(attempt)) }
            }
        }
    }
}