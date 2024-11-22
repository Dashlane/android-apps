package com.dashlane.login.settings

import com.dashlane.mvvm.State

sealed class LoginSettingsState : State {
    data class View(
        val isLoading: Boolean = true,
        val biometricChecked: Boolean = false,
        val biometricRecoveryChecked: Boolean = false,
        val helpShown: Boolean = false,
        val snackBarShown: Boolean = false,
    ) : LoginSettingsState(), State.View

    sealed class SideEffect : LoginSettingsState(), State.SideEffect {
        data object Success : SideEffect()
    }
}
