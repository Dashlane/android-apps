package com.dashlane.settings.main.advanced

import com.dashlane.settings.Advanced
import com.dashlane.settings.SettingsEvent
import com.dashlane.settings.SettingsEvent.ItemClicked
import com.dashlane.settings.SettingsEvent.Search
import com.dashlane.settings.SettingsEvent.SettingsLoaded
import com.dashlane.settings.SettingsEvent.Unlocked
import com.dashlane.settings.main.advanced.SettingsAdvancedState.SideEffect
import com.dashlane.settings.main.advanced.SettingsAdvancedState.SideEffect.DisplayLock
import com.dashlane.settings.main.advanced.SettingsAdvancedState.SideEffect.GoToGeneral
import com.dashlane.settings.main.advanced.SettingsAdvancedState.SideEffect.GoToHelp
import com.dashlane.settings.main.advanced.SettingsAdvancedState.SideEffect.GoToSecretTransfer
import com.dashlane.settings.main.advanced.SettingsAdvancedState.SideEffect.GoToSecurity
import com.dashlane.settings.main.advanced.SettingsAdvancedState.View
import com.dashlane.settings.main.advanced.SettingsAdvancedState.View.DisplaySettings
import com.dashlane.settings.main.advanced.SettingsAdvancedState.View.WaitingForLock
import com.dashlane.settings.search
import com.dashlane.udf.StateMachine
import com.dashlane.udf.UDFViewModel
import com.dashlane.udf.UseCase
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class SettingsAdvancedViewModel @Inject constructor(
    useCases: MutableList<UseCase<View, SettingsEvent<Advanced>>>,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : UDFViewModel<View, SideEffect, SettingsEvent<Advanced>>(useCases = useCases, defaultDispatcher = defaultDispatcher) {
    init {
        start(createStateMachine())
    }

    internal fun createStateMachine() = StateMachine.create<View, SettingsEvent<Advanced>, SideEffect> {
        initialState(View.Initial(SettingsAdvancedData()))
        state<View.Initial> {
            on<SettingsLoaded<Advanced>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(advancedSettings = it.settings, filteredSettings = filteredSettings)))
            }
        }
        state<DisplaySettings> {
            on<SettingsLoaded<Advanced>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(advancedSettings = it.settings, filteredSettings = filteredSettings)))
            }
            on<ItemClicked<Advanced>> {
                val (state, sideEffect) = reduceItemClicked(it)
                transitionTo(state, sideEffect)
            }
            on<Search<Advanced>> { event ->
                val filteredSettings = data.advancedSettings.search(event.searchQuery)
                transitionTo(DisplaySettings(data.copy(filteredSettings = filteredSettings, searchQuery = event.searchQuery)))
            }
        }
        state<WaitingForLock> {
            on<Unlocked<Advanced, SideEffect>> {
                transitionTo(DisplaySettings(data), it.sideEffect)
            }
            on<SettingsLoaded<Advanced>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(advancedSettings = it.settings, filteredSettings = filteredSettings)))
            }
        }
    }

    private fun View.reduceItemClicked(event: ItemClicked<Advanced>): Pair<View, SideEffect?> {
        return when (event.id) {
            Advanced.Security -> DisplaySettings(data) to GoToSecurity
            Advanced.General -> DisplaySettings(data) to GoToGeneral
            Advanced.Help -> DisplaySettings(data) to GoToHelp
            Advanced.AddNewDevice -> WaitingForLock(data, GoToSecretTransfer) to DisplayLock(forceMasterPassword = false)
            else -> this to null
        }
    }
}