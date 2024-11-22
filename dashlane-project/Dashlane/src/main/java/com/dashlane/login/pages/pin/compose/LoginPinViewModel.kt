package com.dashlane.login.pages.pin.compose

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.dashlane.hardwaresecurity.SecurityHelper
import com.dashlane.lock.LockEvent
import com.dashlane.lock.LockManager
import com.dashlane.lock.LockPass
import com.dashlane.lock.LockSetting
import com.dashlane.lock.LockType
import com.dashlane.login.LoginLogger
import com.dashlane.login.LoginMode
import com.dashlane.login.root.LocalLoginDestination.Pin
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.user.UserAccountInfo
import com.dashlane.user.Username
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginPinViewModel @Inject constructor(
    private val securityHelper: SecurityHelper,
    private val preferencesManager: PreferencesManager,
    private val sessionManager: SessionManager,
    private val lockManager: LockManager,
    private val loginLogger: LoginLogger,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val email: String = savedStateHandle.toRoute<Pin>().login

    private val stateFlow: MutableStateFlow<LoginPinState> = MutableStateFlow(LoginPinState(email))
    private val navigationStateFlow = MutableSharedFlow<LoginPinNavigationState>()

    val uiState: StateFlow<LoginPinState> = stateFlow.asStateFlow()
    val navigationState = navigationStateFlow.asSharedFlow()

    fun viewStarted(userAccountInfo: UserAccountInfo, lockSetting: LockSetting) {
        viewModelScope.launch {
            if (stateFlow.value.pinCode?.isNotEmpty() == true) return@launch

            val isSystemLockSetup = securityHelper.isDeviceSecured(Username.ofEmailOrNull(userAccountInfo.username))
            val isMPLess = userAccountInfo.accountType is UserAccountInfo.AccountType.InvisibleMasterPassword
            val pinLength = preferencesManager[userAccountInfo.username].pinCodeLength
            val fallback = when {
                userAccountInfo.sso -> LoginPinFallback.SSO
                isMPLess -> LoginPinFallback.MPLess
                else -> LoginPinFallback.MP
            }
            stateFlow.update { state ->
                state.copy(
                    email = userAccountInfo.username,
                    pinLength = pinLength,
                    isSystemLockSetup = isSystemLockSetup,
                    lockSetting = lockSetting,
                    fallback = fallback,
                    isMPLess = isMPLess
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
            navigationStateFlow.emit(LoginPinNavigationState.GoToSecretTransfer(stateFlow.value.email))
        }
    }

    fun onClickRecovery() {
        viewModelScope.launch {
            stateFlow.update { state -> state.copy(helpDialogShown = false) }
            navigationStateFlow.emit(LoginPinNavigationState.GoToRecoveryHelp(stateFlow.value.email))
        }
    }

    fun onPinUpdated(pinCode: String) {
        viewModelScope.launch {
            stateFlow.update { state -> state.copy(pinCode = pinCode, error = null) }
            delay(100) 
            if (pinCode.length == stateFlow.value.pinLength) {
                validatePin(pinCode)
            }
        }
    }

    @org.jetbrains.annotations.VisibleForTesting
    @Suppress("kotlin:S6313") 
    suspend fun validatePin(pinCode: String) {
        val session = sessionManager.session ?: throw IllegalStateException("session null in validatePin")
        if (lockManager.unlock(session = session, pass = LockPass.ofPin(pinCode))) {
            loginLogger.logSuccess(loginMode = LoginMode.Pin)
            val reason = uiState.value.lockSetting?.unlockReason ?: LockEvent.Unlock.Reason.AppAccess
            runCatching { lockManager.sendUnlockEvent(LockEvent.Unlock(reason = reason, lockType = LockType.PinCode)) }
            navigationStateFlow.emit(LoginPinNavigationState.UnlockSuccess)
        } else {
            loginLogger.logWrongPin()
            lockManager.addFailUnlockAttempt()
            if (lockManager.hasFailedUnlockTooManyTimes()) {
                navigationStateFlow.emit(
                    LoginPinNavigationState.Logout(
                        email = stateFlow.value.email,
                        isMPLess = stateFlow.value.isMPLess,
                        errorMessage = null
                    )
                )
            } else {
                val attempt = lockManager.getFailUnlockAttemptCount()
                stateFlow.update { state -> state.copy(pinCode = "", error = LoginPinError.WrongPin(attempt)) }
            }
        }
    }
}