package com.dashlane.settings.main.account

import com.dashlane.mvvm.State
import com.dashlane.settings.SettingsError
import com.dashlane.settings.SettingsItemData
import com.dashlane.settings.security.SettingsDialog

data class SettingsAccountData(
    val accountSettings: List<SettingsItemData> = emptyList(),
    val filteredSettings: List<SettingsItemData> = emptyList(),
    val searchQuery: String = "",
    val error: SettingsError? = null,
    val dialog: SettingsDialog? = null,
) : State.View

sealed class SettingsAccountState : State {
    sealed class View : SettingsAccountState(), State.View {
        abstract val data: SettingsAccountData

        data class Initial(override val data: SettingsAccountData) : View()

        data class Loading(override val data: SettingsAccountData) : View()

        data class DisplaySettings(override val data: SettingsAccountData) : View()

        data class Error(override val data: SettingsAccountData) : View()
    }

    sealed class SideEffect : SettingsAccountState(), State.SideEffect {
        data object GoToCurrentPlan : SideEffect()

        data object GoToAccountStatus : SideEffect()
    }
}