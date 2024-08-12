package com.dashlane.login.pages.password.compose

import androidx.compose.ui.text.input.TextFieldValue
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.authentication.login.SsoInfo
import com.dashlane.lock.UnlockEvent
import com.dashlane.login.LoginStrategy
import com.dashlane.login.lock.LockSetting
import com.dashlane.mvvm.State

sealed class LoginPasswordState : State {
    data class View(
        val email: String? = null,
        val loginHistory: List<String> = emptyList(),
        
        val password: TextFieldValue = TextFieldValue(""),
        val unlockReason: UnlockEvent.Reason? = null,
        val isLoading: Boolean = false,
        val lockSetting: LockSetting? = null,
        val error: LoginPasswordError? = null,
        val isBiometricRecoveryEnabled: Boolean? = null,
        val isARKEnabled: Boolean? = null,
        val helpDialogShown: Boolean = false,
        val recoveryDialogShown: Boolean = false
    ) : LoginPasswordState(), State.View

    sealed class SideEffect : LoginPasswordState(), State.SideEffect {
        data class LoginSuccess(val strategy: LoginStrategy.Strategy, val ssoInfo: SsoInfo?) : SideEffect()
        data object Cancel : SideEffect()
        data object Fallback : SideEffect()
        data class ChangeAccount(val email: String) : SideEffect()
        data class Logout(val email: String?, val error: LoginPasswordError?) : SideEffect()
        data object GoToBiometricRecovery : SideEffect()
        data class GoToARK(val registeredUserDevice: RegisteredUserDevice, val authTicket: String?) : SideEffect()
        data object GoToCannotLoginHelp : SideEffect()
        data object GoToForgotMPHelp : SideEffect()
    }
}

sealed class LoginPasswordError {
    data object Generic : LoginPasswordError()
    data object EmptyPassword : LoginPasswordError()
    data object InvalidPassword : LoginPasswordError()
    data object InvalidCredentials : LoginPasswordError()
    data object TooManyInvalidPassword : LoginPasswordError()
}
