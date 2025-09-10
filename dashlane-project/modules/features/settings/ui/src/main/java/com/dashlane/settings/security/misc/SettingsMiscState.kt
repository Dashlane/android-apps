package com.dashlane.settings.security.misc

import android.net.Uri
import com.dashlane.mvvm.State
import com.dashlane.settings.SettingsError
import com.dashlane.settings.SettingsItemData
import com.dashlane.settings.security.SettingsDialog

data class SettingsMiscData(
    val miscSettings: List<SettingsItemData> = emptyList(),
    val filteredSettings: List<SettingsItemData> = emptyList(),
    val searchQuery: String = "",
    val error: SettingsError? = null,
    val dialog: SettingsDialog? = null,
) : State.View

sealed class SettingsMiscState : State {
    sealed class View : SettingsMiscState(), State.View {
        abstract val data: SettingsMiscData

        data class Initial(override val data: SettingsMiscData) : View()

        data class Loading(override val data: SettingsMiscData) : View()

        data class DisplaySettings(override val data: SettingsMiscData) : View()

        data class UpdateScreenshotAllowed(override val data: SettingsMiscData, val checked: Boolean) : View()

        data class UpdateClearClipboard(override val data: SettingsMiscData, val checked: Boolean) : View()

        data class UpdateCopy2Fa(override val data: SettingsMiscData, val checked: Boolean) : View()

        data class UpdateChangeMasterPassword(override val data: SettingsMiscData) : View()

        data class UpdateKeyDerivation(override val data: SettingsMiscData) : View()

        data class UpdatePrivacy(override val data: SettingsMiscData) : View()

        data class UpdateRichIcons(override val data: SettingsMiscData, val checked: Boolean) : View()

        data class Error(override val data: SettingsMiscData) : View()
    }

    sealed class SideEffect : SettingsMiscState(), State.SideEffect {
        data class DisplayLock(val forceMasterPassword: Boolean) : SideEffect()

        data class GoToPrivacy(val uri: Uri) : SideEffect()

        data object GoToManageDevices : SideEffect()

        data class GoToChangeMasterPassword(val showWarningDesktop: Boolean) : SideEffect()
    }
}