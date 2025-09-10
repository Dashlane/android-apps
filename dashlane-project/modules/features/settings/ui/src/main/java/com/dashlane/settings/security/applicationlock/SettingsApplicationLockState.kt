package com.dashlane.settings.security.applicationlock

import com.dashlane.mvvm.State
import com.dashlane.settings.SettingsError
import com.dashlane.settings.SettingsItemData
import com.dashlane.settings.security.SettingsDialog

data class SettingsApplicationLockData(
    val securitySettings: List<SettingsItemData> = emptyList(),
    val filteredSettings: List<SettingsItemData> = emptyList(),
    val searchQuery: String = "",
    val error: SettingsError? = null,
    val dialog: SettingsDialog? = null,
) : State.View

sealed class SettingsApplicationLockState : State {
    sealed class View : SettingsApplicationLockState(), State.View {
        abstract val data: SettingsApplicationLockData

        data class Initial(override val data: SettingsApplicationLockData) : View()

        data class Loading(override val data: SettingsApplicationLockData) : View()

        data class DisplaySettings(override val data: SettingsApplicationLockData) : View()

        data class WaitingForLock(override val data: SettingsApplicationLockData, val sideEffect: SideEffect) : View()

        data class UpdatePinLock(override val data: SettingsApplicationLockData, val checked: Boolean) : View()

        data class UpdateBiometricLock(override val data: SettingsApplicationLockData, val checked: Boolean) : View()

        data class Update2FA(override val data: SettingsApplicationLockData, val checked: Boolean) : View()

        data class UpdateBiometricRecovery(override val data: SettingsApplicationLockData, val checked: Boolean) : View()

        data class UpdateDisableLocal2FA(override val data: SettingsApplicationLockData, val checked: Boolean) : View()

        data class UpdateAutoLockOnExit(override val data: SettingsApplicationLockData, val checked: Boolean) : View()

        data class UpdateAutoLockTime(override val data: SettingsApplicationLockData) : View()

        data class DialogConfirmed(override val data: SettingsApplicationLockData) : View()

        data class AutoLockTimeDialogConfirmed(override val data: SettingsApplicationLockData, val selected: Int) : View()

        data class Error(override val data: SettingsApplicationLockData) : View()

        companion object
    }

    sealed class SideEffect : SettingsApplicationLockState(), State.SideEffect {
        data class DisplayLock(val forceMasterPassword: Boolean) : SideEffect()

        data object GoToPinLock : SideEffect()

        data object GoToARK : SideEffect()

        data object GoToBiometricLock : SideEffect()

        data object GoToBiometricRecovery : SideEffect()

        data object GoToDisable2FA : SideEffect()

        data object GoToDisable2FAEnforced : SideEffect()
    }
}