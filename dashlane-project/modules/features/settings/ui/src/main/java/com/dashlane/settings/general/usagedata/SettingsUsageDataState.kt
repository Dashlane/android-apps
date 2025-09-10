package com.dashlane.settings.general.usagedata

import com.dashlane.mvvm.State
import com.dashlane.settings.SettingsError
import com.dashlane.settings.SettingsItemData
import com.dashlane.settings.security.SettingsDialog

data class SettingsUsageDataData(
    val usageDataSettings: List<SettingsItemData> = emptyList(),
    val filteredSettings: List<SettingsItemData> = emptyList(),
    val searchQuery: String = "",
    val error: SettingsError? = null,
    val dialog: SettingsDialog? = null,
) : State.View

sealed class SettingsUsageDataState : State {
    sealed class View : SettingsUsageDataState(), State.View {
        abstract val data: SettingsUsageDataData

        data class Initial(override val data: SettingsUsageDataData) : View()

        data class Loading(override val data: SettingsUsageDataData) : View()

        data class DisplaySettings(override val data: SettingsUsageDataData) : View()

        data class UpdateUsageData(override val data: SettingsUsageDataData, val checked: Boolean) : View()
    }

    sealed class SideEffect : SettingsUsageDataState(), State.SideEffect
}