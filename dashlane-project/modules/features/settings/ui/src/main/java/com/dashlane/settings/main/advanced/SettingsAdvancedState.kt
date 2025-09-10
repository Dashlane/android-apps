package com.dashlane.settings.main.advanced

import com.dashlane.mvvm.State
import com.dashlane.settings.SettingsError
import com.dashlane.settings.SettingsItemData
import com.dashlane.settings.security.SettingsDialog

data class SettingsAdvancedData(
    val advancedSettings: List<SettingsItemData> = emptyList(),
    val filteredSettings: List<SettingsItemData> = emptyList(),
    val searchQuery: String = "",
    val error: SettingsError? = null,
    val dialog: SettingsDialog? = null,
) : State.View

sealed class SettingsAdvancedState : State {
    sealed class View : SettingsAdvancedState(), State.View {
        abstract val data: SettingsAdvancedData

        data class Initial(override val data: SettingsAdvancedData) : View()

        data class Loading(override val data: SettingsAdvancedData) : View()

        data class DisplaySettings(override val data: SettingsAdvancedData) : View()

        data class WaitingForLock(override val data: SettingsAdvancedData, val sideEffect: SideEffect) : View()

        data class Error(override val data: SettingsAdvancedData) : View()
    }

    sealed class SideEffect : SettingsAdvancedState(), State.SideEffect {
        data class DisplayLock(val forceMasterPassword: Boolean) : SideEffect()

        data object GoToSecurity : SideEffect()

        data object GoToGeneral : SideEffect()

        data object GoToHelp : SideEffect()

        data object GoToSecretTransfer : SideEffect()
    }
}