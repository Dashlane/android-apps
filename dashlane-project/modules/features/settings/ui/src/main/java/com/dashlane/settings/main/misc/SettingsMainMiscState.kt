package com.dashlane.settings.main.misc

import android.content.Intent
import com.dashlane.mvvm.State
import com.dashlane.settings.SettingsError
import com.dashlane.settings.SettingsItemData
import com.dashlane.settings.security.SettingsDialog
import java.net.URL

data class SettingsMainMiscData(
    val miscSettings: List<SettingsItemData> = emptyList(),
    val filteredSettings: List<SettingsItemData> = emptyList(),
    val searchQuery: String = "",
    val error: SettingsError? = null,
    val dialog: SettingsDialog? = null,
) : State.View

sealed class SettingsMainMiscState : State {
    sealed class View : SettingsMainMiscState(), State.View {
        abstract val data: SettingsMainMiscData

        data class Initial(override val data: SettingsMainMiscData) : View()

        data class Loading(override val data: SettingsMainMiscData) : View()

        data class DisplaySettings(override val data: SettingsMainMiscData) : View()

        data class DialogConfirmed(override val data: SettingsMainMiscData) : View()

        data class UpdateInviteFriend(override val data: SettingsMainMiscData) : View()

        data class UpdateLogout(override val data: SettingsMainMiscData) : View()

        data class UpdateVersion(override val data: SettingsMainMiscData) : View()

        data class Error(override val data: SettingsMainMiscData) : View()
    }

    sealed class SideEffect : SettingsMainMiscState(), State.SideEffect {
        data class GoToInviteFriend(val intent: Intent) : SideEffect()

        data object GoToSendLove : SideEffect()

        data object GoToLogout : SideEffect()

        data object GoToDebug : SideEffect()

        data class DisplayToast(val message: MainMiscToast) : SideEffect()

        data class GoToUrl(val url: URL) : SideEffect()
    }
}

sealed interface MainMiscToast {
    data class Error(val error: SettingsError) : MainMiscToast

    data object Copy : MainMiscToast
}