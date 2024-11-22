package com.dashlane.login.progress

import com.dashlane.login.Device
import com.dashlane.mvvm.State

sealed class LoginSyncProgressState : State {
    data class View(
        val devicesToUnregister: List<Device> = emptyList(),
        val hasFinishedLoading: Boolean = false,
        val progress: Int? = null,
        val message: Int? = null,
        val error: LoginSyncProgressError? = null
    ) : LoginSyncProgressState(), State.View

    sealed class SideEffect : LoginSyncProgressState(), State.SideEffect {
        data object Success : SideEffect()
        data object SyncError : SideEffect()
        data object Cancel : SideEffect()
    }
}

sealed class LoginSyncProgressError {
    data class Generic(val errorMessage: String? = null) : LoginSyncProgressError()
    data object Unregister : LoginSyncProgressError()
}