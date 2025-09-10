package com.dashlane.settings.help

import android.os.Build
import com.dashlane.settings.Help
import com.dashlane.settings.Help.Licenses
import com.dashlane.settings.Help.PrivacyPolicy
import com.dashlane.settings.Help.TermsOfService
import com.dashlane.settings.Help.TroubleShooting
import com.dashlane.settings.Help.UserSupportId
import com.dashlane.settings.SettingsEvent
import com.dashlane.settings.SettingsEvent.ItemClicked
import com.dashlane.settings.SettingsEvent.Search
import com.dashlane.settings.SettingsEvent.SettingsLoaded
import com.dashlane.settings.help.SettingsHelpState.SideEffect
import com.dashlane.settings.help.SettingsHelpState.SideEffect.GoToLicences
import com.dashlane.settings.help.SettingsHelpState.SideEffect.GoToPrivacyPolicy
import com.dashlane.settings.help.SettingsHelpState.SideEffect.GoToTermsOfService
import com.dashlane.settings.help.SettingsHelpState.SideEffect.GoToTroubleShooting
import com.dashlane.settings.help.SettingsHelpState.View
import com.dashlane.settings.help.SettingsHelpState.View.DisplaySettings
import com.dashlane.settings.help.SettingsHelpState.View.Initial
import com.dashlane.settings.search
import com.dashlane.udf.StateMachine
import com.dashlane.udf.UDFViewModel
import com.dashlane.udf.UseCase
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class SettingsHelpViewModel @Inject constructor(
    useCases: MutableList<UseCase<View, SettingsEvent<Help>>>,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : UDFViewModel<View, SideEffect, SettingsEvent<Help>>(useCases = useCases, defaultDispatcher = defaultDispatcher) {
    init {
        start(createStateMachine())
    }

    internal fun createStateMachine() = StateMachine.create<View, SettingsEvent<Help>, SideEffect> {
        initialState(Initial(SettingsHelpData()))
        state<Initial> {
            on<SettingsLoaded<Help>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(helpSettings = it.settings, filteredSettings = filteredSettings)))
            }
        }
        state<DisplaySettings> {
            on<SettingsLoaded<Help>> {
                transitionTo(DisplaySettings(data.copy(helpSettings = it.settings)))
            }
            on<ItemClicked<Help>> {
                val (state, sideEffect) = reduceItemClicked(it)
                transitionTo(state, sideEffect)
            }
            on<Search<Help>> { event ->
                val filteredSettings = data.helpSettings.search(event.searchQuery)
                transitionTo(DisplaySettings(data.copy(filteredSettings = filteredSettings, searchQuery = event.searchQuery)))
            }
        }
        state<View.UpdateUserSupportId> {
            on<SettingsLoaded<Help>> {
                val sideEffect = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    SideEffect.DisplayToast
                } else {
                    null
                }
                transitionTo(DisplaySettings(data.copy(helpSettings = it.settings)), sideEffect)
            }
        }
    }

    private fun View.reduceItemClicked(event: ItemClicked<Help>): Pair<View, SideEffect?> {
        return when (event.id) {
            TroubleShooting -> DisplaySettings(data) to GoToTroubleShooting
            PrivacyPolicy -> DisplaySettings(data) to GoToPrivacyPolicy
            TermsOfService -> DisplaySettings(data) to GoToTermsOfService
            Licenses -> DisplaySettings(data) to GoToLicences
            UserSupportId -> View.UpdateUserSupportId(data) to null
            else -> this to null
        }
    }
}