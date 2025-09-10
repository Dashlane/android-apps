package com.dashlane.settings.general.autofill

import com.dashlane.mvvm.State
import com.dashlane.settings.SettingsError
import com.dashlane.settings.SettingsItemData
import com.dashlane.settings.security.SettingsDialog

data class SettingsAutofillData(
    val autofillSettings: List<SettingsItemData> = emptyList(),
    val filteredSettings: List<SettingsItemData> = emptyList(),
    val searchQuery: String = "",
    val error: SettingsError? = null,
    val dialog: SettingsDialog? = null,
) : State.View

sealed class SettingsAutofillState : State {
    sealed class View : SettingsAutofillState(), State.View {
        abstract val data: SettingsAutofillData

        data class Initial(override val data: SettingsAutofillData) : View()

        data class Loading(override val data: SettingsAutofillData) : View()

        data class DisplaySettings(override val data: SettingsAutofillData) : View()

        data class RefreshSettings(override val data: SettingsAutofillData) : View()

        data class UpdateAutofill(override val data: SettingsAutofillData, val checked: Boolean) : View()

        data class UpdateChromeAutofill(override val data: SettingsAutofillData, val checked: Boolean) : View()

        data class UpdateKeyboardAutofill(override val data: SettingsAutofillData, val checked: Boolean) : View()

        data class UpdatePhishingAlert(override val data: SettingsAutofillData, val checked: Boolean) : View()
    }

    sealed class SideEffect : SettingsAutofillState(), State.SideEffect {
        data object GoToPaywall : SideEffect()

        data object GoToInAppLogin : SideEffect()

        data object GoToChromeInAppLogin : SideEffect()

        data object GoToAutofillSettings : SideEffect()

        data class GoToChromeAutofillSettings(val packageName: String) : SideEffect()

        data object GoToPausedAutofill : SideEffect()
    }
}