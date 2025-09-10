package com.dashlane.login.devicelimit

import com.dashlane.data.login.Device
import com.dashlane.mvvm.State

sealed class DeviceLimitState : State {
    data class View(
        val maxDevices: Int = 0,
        val devices: List<Device> = emptyList(),
        val showDevicesDialog: Boolean = false,
        val unlinkEnabled: Boolean = false,
    ) : DeviceLimitState(), State.View

    sealed class SideEffect : DeviceLimitState(), State.SideEffect {
        data object Premium : SideEffect()

        data object HasSync : SideEffect()

        data class ConfirmUnregisterDevice(val devices: List<Device>) : SideEffect()

        data object UserLoggedOut : SideEffect()
    }
}