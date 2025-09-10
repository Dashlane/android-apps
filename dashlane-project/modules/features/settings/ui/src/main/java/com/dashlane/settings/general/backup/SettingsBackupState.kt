package com.dashlane.settings.general.backup

import com.dashlane.mvvm.State
import com.dashlane.settings.SettingsError
import com.dashlane.settings.SettingsItemData
import com.dashlane.settings.security.SettingsDialog

data class SettingsBackupData(
    val backupSettings: List<SettingsItemData> = emptyList(),
    val filteredSettings: List<SettingsItemData> = emptyList(),
    val searchQuery: String = "",
    val error: SettingsError? = null,
    val dialog: SettingsDialog? = null,
) : State.View

sealed class SettingsBackupState : State {
    sealed class View : SettingsBackupState(), State.View {
        abstract val data: SettingsBackupData

        data class Initial(override val data: SettingsBackupData) : View()

        data class Loading(override val data: SettingsBackupData) : View()

        data class DisplaySettings(override val data: SettingsBackupData) : View()

        data class WaitingForLock(override val data: SettingsBackupData, val sideEffect: SideEffect) : View()
    }

    sealed class SideEffect : SettingsBackupState(), State.SideEffect {
        data class DisplayLock(val forceMasterPassword: Boolean) : SideEffect()

        data object StartExport : SideEffect()

        data object GoToExportCsv : SideEffect()

        data object GoToImport : SideEffect()
    }
}