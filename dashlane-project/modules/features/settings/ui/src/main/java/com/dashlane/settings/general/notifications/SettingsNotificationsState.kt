package com.dashlane.settings.general.notifications

import com.dashlane.mvvm.State
import com.dashlane.settings.SettingsError
import com.dashlane.settings.SettingsItemData
import com.dashlane.settings.general.autofill.SettingsAutofillState.SideEffect
import com.dashlane.settings.general.autofill.SettingsAutofillState.View
import com.dashlane.settings.security.SettingsDialog

data class SettingsNotificationsData(
    val notificationsSettings: List<SettingsItemData> = emptyList(),
    val filteredSettings: List<SettingsItemData> = emptyList(),
    val searchQuery: String = "",
    val error: SettingsError? = null,
    val dialog: SettingsDialog? = null,
) : State.View

sealed class SettingsNotificationsState : State {
    sealed class View : SettingsNotificationsState(), State.View {
        abstract val data: SettingsNotificationsData

        data class Initial(override val data: SettingsNotificationsData) : View()

        data class Loading(override val data: SettingsNotificationsData) : View()

        data class DisplaySettings(override val data: SettingsNotificationsData) : View()

        data class UpdateCopyAndPaste(override val data: SettingsNotificationsData, val checked: Boolean) : View()

        data class Error(override val data: SettingsNotificationsData) : View()
    }

    sealed class SideEffect : SettingsNotificationsState(), State.SideEffect {
        data object GoToSystemSettings : SideEffect()
    }
}