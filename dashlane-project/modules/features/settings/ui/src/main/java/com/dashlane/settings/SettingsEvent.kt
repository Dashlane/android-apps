package com.dashlane.settings

import android.content.Intent
import android.net.Uri
import com.dashlane.mvvm.State.SideEffect
import com.dashlane.settings.security.SettingsDialog
import com.dashlane.udf.Event
import java.net.URL

sealed interface SettingsEvent<T : Settings> : Event {
    data class Search<T : Settings>(val searchQuery: String) : SettingsEvent<T>

    data class Error<T : Settings>(val error: SettingsError) : SettingsEvent<T>

    data class Unlocked<T : Settings, S : SideEffect>(val sideEffect: S) : SettingsEvent<T>

    data class GoTo<T : Settings, S : SideEffect>(val sideEffect: S) : SettingsEvent<T>

    data class DisplayDialog<T : Settings>(val dialog: SettingsDialog) : SettingsEvent<T>

    data class DialogDismissed<T : Settings>(val dialog: SettingsDialog) : SettingsEvent<T>

    data class DialogPositive<T : Settings>(val dialog: SettingsDialog) : SettingsEvent<T>

    data class DialogNegative<T : Settings>(val dialog: SettingsDialog) : SettingsEvent<T>

    data class ItemClicked<T : Settings>(val id: Settings.Click) : SettingsEvent<T>

    data class ItemCheckChanged<T : Settings>(val id: Settings.Toggle, val checked: Boolean) : SettingsEvent<T>

    data class SettingsLoaded<T : Settings>(val settings: List<SettingsItemData>) : SettingsEvent<T>

    data class SettingsUpdated<T : Settings>(val setting: SettingsItemData) : SettingsEvent<T>

    data class SettingsRemoved<T : Settings>(val settings: List<Settings>) : SettingsEvent<T>

    
    data class ChangeMasterPasswordLoaded(val showWarningDesktop: Boolean) : SettingsEvent<Misc>

    data class KeyDerivationLoaded(val hasCryptoForcedPayload: Boolean) : SettingsEvent<Misc>

    data class PrivacyLoaded(val uri: Uri) : SettingsEvent<Misc>

    
    data object ViewResumed : SettingsEvent<Autofill>

    
    data class InviteFriendIntent(val intent: Intent) : SettingsEvent<MainMisc>

    data object Logout : SettingsEvent<MainMisc>

    data class SocialMediaClick(val url: URL) : SettingsEvent<MainMisc>
}

sealed interface SettingsError {
    data object Offline : SettingsError

    data object Network : SettingsError

    data object Generic : SettingsError

    sealed interface MainMiscError : SettingsError {
        data object NoAppAvailable : SettingsError
    }
}