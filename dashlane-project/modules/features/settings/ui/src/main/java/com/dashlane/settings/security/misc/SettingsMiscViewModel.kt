package com.dashlane.settings.security.misc

import com.dashlane.settings.Misc
import com.dashlane.settings.Misc.ChangeMasterPassword
import com.dashlane.settings.Misc.ClearClipboard
import com.dashlane.settings.Misc.Copy2FA
import com.dashlane.settings.Misc.KeyDerivation
import com.dashlane.settings.Misc.ManageDevices
import com.dashlane.settings.Misc.PrivacyAndData
import com.dashlane.settings.Misc.RichIcons
import com.dashlane.settings.Misc.Screenshots
import com.dashlane.settings.SettingsEvent
import com.dashlane.settings.SettingsEvent.ChangeMasterPasswordLoaded
import com.dashlane.settings.SettingsEvent.DialogNegative
import com.dashlane.settings.SettingsEvent.ItemCheckChanged
import com.dashlane.settings.SettingsEvent.ItemClicked
import com.dashlane.settings.SettingsEvent.KeyDerivationLoaded
import com.dashlane.settings.SettingsEvent.PrivacyLoaded
import com.dashlane.settings.SettingsEvent.Search
import com.dashlane.settings.SettingsEvent.SettingsLoaded
import com.dashlane.settings.search
import com.dashlane.settings.security.SettingsDialog
import com.dashlane.settings.security.misc.SettingsMiscState.SideEffect
import com.dashlane.settings.security.misc.SettingsMiscState.View
import com.dashlane.settings.security.misc.SettingsMiscState.View.DisplaySettings
import com.dashlane.settings.security.misc.SettingsMiscState.View.Initial
import com.dashlane.settings.security.misc.SettingsMiscState.View.UpdateChangeMasterPassword
import com.dashlane.settings.security.misc.SettingsMiscState.View.UpdateClearClipboard
import com.dashlane.settings.security.misc.SettingsMiscState.View.UpdateCopy2Fa
import com.dashlane.settings.security.misc.SettingsMiscState.View.UpdateKeyDerivation
import com.dashlane.settings.security.misc.SettingsMiscState.View.UpdatePrivacy
import com.dashlane.settings.security.misc.SettingsMiscState.View.UpdateRichIcons
import com.dashlane.settings.security.misc.SettingsMiscState.View.UpdateScreenshotAllowed
import com.dashlane.udf.StateMachine
import com.dashlane.udf.UDFViewModel
import com.dashlane.udf.UseCase
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class SettingsMiscViewModel @Inject constructor(
    useCases: MutableList<UseCase<View, SettingsEvent<Misc>>>,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : UDFViewModel<View, SideEffect, SettingsEvent<Misc>>(useCases = useCases, defaultDispatcher = defaultDispatcher) {
    init {
        start(createStateMachine())
    }

    @Suppress("LongMethod")
    internal fun createStateMachine() = StateMachine.create<View, SettingsEvent<Misc>, SideEffect> {
        initialState(Initial(SettingsMiscData()))
        state<Initial> {
            on<SettingsLoaded<Misc>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(miscSettings = it.settings, filteredSettings = filteredSettings)))
            }
        }
        state<DisplaySettings> {
            on<SettingsLoaded<Misc>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(miscSettings = it.settings, filteredSettings = filteredSettings)))
            }
            on<ItemClicked<Misc>> {
                val (state, sideEffect) = reduceItemClicked(it)
                transitionTo(state, sideEffect)
            }
            on<ItemCheckChanged<Misc>> {
                val (state, sideEffect) = reduceItemCheckChanged(it)
                transitionTo(state, sideEffect)
            }
            on<DialogNegative<Misc>> {
                transitionTo(DisplaySettings(data.copy(dialog = null)))
            }
            on<Search<Misc>> { event ->
                val filteredSettings = data.miscSettings.search(event.searchQuery)
                transitionTo(DisplaySettings(data.copy(filteredSettings = filteredSettings, searchQuery = event.searchQuery)))
            }
        }
        state<UpdateScreenshotAllowed> {
            on<SettingsLoaded<Misc>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(miscSettings = it.settings, filteredSettings = filteredSettings)))
            }
        }
        state<UpdateClearClipboard> {
            on<SettingsLoaded<Misc>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(miscSettings = it.settings, filteredSettings = filteredSettings)))
            }
        }
        state<UpdateCopy2Fa> {
            on<SettingsLoaded<Misc>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(miscSettings = it.settings, filteredSettings = filteredSettings)))
            }
        }
        state<UpdateChangeMasterPassword> {
            on<SettingsLoaded<Misc>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(miscSettings = it.settings, filteredSettings = filteredSettings)))
            }
            on<ChangeMasterPasswordLoaded> {
                transitionTo(DisplaySettings(data), SideEffect.GoToChangeMasterPassword(it.showWarningDesktop))
            }
        }
        state<UpdateKeyDerivation> {
            on<KeyDerivationLoaded> {
                transitionTo(DisplaySettings(data.copy(dialog = SettingsDialog.KeyDerivationWarning(it.hasCryptoForcedPayload))))
            }
        }
        state<UpdatePrivacy> {
            on<PrivacyLoaded> {
                transitionTo(DisplaySettings(data), SideEffect.GoToPrivacy(it.uri))
            }
        }
        state<UpdateRichIcons> {
            on<SettingsLoaded<Misc>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(miscSettings = it.settings, filteredSettings = filteredSettings)))
            }
        }
    }

    private fun View.reduceItemClicked(event: ItemClicked<Misc>): Pair<View, SideEffect?> {
        return when (event.id) {
            ChangeMasterPassword -> UpdateChangeMasterPassword(data) to SideEffect.DisplayLock(forceMasterPassword = true)
            KeyDerivation -> UpdateKeyDerivation(data) to null
            PrivacyAndData -> UpdatePrivacy(data) to null
            ManageDevices -> this to SideEffect.GoToManageDevices
            else -> this to null
        }
    }

    private fun View.reduceItemCheckChanged(event: ItemCheckChanged<Misc>): Pair<View, SideEffect?> {
        return when (event.id) {
            Screenshots -> UpdateScreenshotAllowed(data, event.checked) to SideEffect.DisplayLock(forceMasterPassword = false)
            ClearClipboard -> UpdateClearClipboard(data, event.checked) to null
            Copy2FA -> UpdateCopy2Fa(data, event.checked) to null
            RichIcons -> UpdateRichIcons(data, event.checked) to null
            else -> this to null
        }
    }
}