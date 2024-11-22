package com.dashlane.login.monobucket

import com.dashlane.login.Device
import com.dashlane.mvvm.State

sealed class MonobucketState : State {
    data class View(
        val device: Device? = null,
        val showPreviousDeviceDialog: Boolean = false
    ) : MonobucketState(), State.View

    sealed class SideEffect : MonobucketState(), State.SideEffect {
        data object Premium : SideEffect()
        data object HasSync : SideEffect()
        data object ConfirmUnregisterDevice : SideEffect()
        data object UserLoggedOut : SideEffect()
    }
}