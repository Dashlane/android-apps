package com.dashlane.settings.help

import com.dashlane.mvvm.State
import com.dashlane.settings.SettingsError
import com.dashlane.settings.SettingsItemData
import com.dashlane.settings.main.misc.SettingsMainMiscState.SideEffect
import com.dashlane.settings.security.SettingsDialog

data class SettingsHelpData(
    val helpSettings: List<SettingsItemData> = emptyList(),
    val filteredSettings: List<SettingsItemData> = emptyList(),
    val searchQuery: String = "",
    val error: SettingsError? = null,
    val dialog: SettingsDialog? = null,
) : State.View

sealed class SettingsHelpState : State {
    sealed class View : SettingsHelpState(), State.View {
        abstract val data: SettingsHelpData

        data class Initial(override val data: SettingsHelpData) : View()

        data class Loading(override val data: SettingsHelpData) : View()

        data class DisplaySettings(override val data: SettingsHelpData) : View()

        data class UpdateUserSupportId(override val data: SettingsHelpData) : View()
    }

    sealed class SideEffect : SettingsHelpState(), State.SideEffect {
        data object GoToTroubleShooting : SideEffect()

        data object GoToPrivacyPolicy : SideEffect()

        data object GoToTermsOfService : SideEffect()

        data object GoToLicences : SideEffect()

        data object DisplayToast : SideEffect()
    }
}