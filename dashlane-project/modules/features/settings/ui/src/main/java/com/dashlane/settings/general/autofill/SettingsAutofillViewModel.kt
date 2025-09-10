package com.dashlane.settings.general.autofill

import com.dashlane.settings.Autofill
import com.dashlane.settings.Autofill.EnableAutofill
import com.dashlane.settings.Autofill.EnableChromeAutofill
import com.dashlane.settings.Autofill.KeyboardAutofill
import com.dashlane.settings.Autofill.PausedAutofill
import com.dashlane.settings.Autofill.PhishingAlert
import com.dashlane.settings.SettingsEvent
import com.dashlane.settings.SettingsEvent.GoTo
import com.dashlane.settings.SettingsEvent.ItemCheckChanged
import com.dashlane.settings.SettingsEvent.ItemClicked
import com.dashlane.settings.SettingsEvent.Search
import com.dashlane.settings.SettingsEvent.SettingsLoaded
import com.dashlane.settings.SettingsEvent.ViewResumed
import com.dashlane.settings.general.autofill.SettingsAutofillState.SideEffect
import com.dashlane.settings.general.autofill.SettingsAutofillState.SideEffect.GoToPausedAutofill
import com.dashlane.settings.general.autofill.SettingsAutofillState.View
import com.dashlane.settings.general.autofill.SettingsAutofillState.View.DisplaySettings
import com.dashlane.settings.general.autofill.SettingsAutofillState.View.Initial
import com.dashlane.settings.general.autofill.SettingsAutofillState.View.RefreshSettings
import com.dashlane.settings.general.autofill.SettingsAutofillState.View.UpdateAutofill
import com.dashlane.settings.general.autofill.SettingsAutofillState.View.UpdateChromeAutofill
import com.dashlane.settings.general.autofill.SettingsAutofillState.View.UpdateKeyboardAutofill
import com.dashlane.settings.general.autofill.SettingsAutofillState.View.UpdatePhishingAlert
import com.dashlane.settings.search
import com.dashlane.udf.StateMachine
import com.dashlane.udf.UDFViewModel
import com.dashlane.udf.UseCase
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class SettingsAutofillViewModel @Inject constructor(
    useCases: MutableList<UseCase<View, SettingsEvent<Autofill>>>,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : UDFViewModel<View, SideEffect, SettingsEvent<Autofill>>(useCases = useCases, defaultDispatcher = defaultDispatcher) {
    init {
        start(createStateMachine())
    }

    @Suppress("LongMethod")
    internal fun createStateMachine() = StateMachine.create<View, SettingsEvent<Autofill>, SideEffect> {
        initialState(Initial(SettingsAutofillData()))
        state<Initial> {
            on<SettingsLoaded<Autofill>> {
                transitionTo(DisplaySettings(data.copy(autofillSettings = it.settings)))
            }
        }
        state<RefreshSettings> {
            on<SettingsLoaded<Autofill>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(autofillSettings = it.settings, filteredSettings = filteredSettings)))
            }
        }
        state<DisplaySettings> {
            on<ViewResumed> {
                transitionTo(RefreshSettings(data))
            }
            on<SettingsLoaded<Autofill>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(autofillSettings = it.settings, filteredSettings = filteredSettings)))
            }
            on<ItemCheckChanged<Autofill>> {
                val (state, sideEffect) = reduceItemCheckChanged(it)
                transitionTo(state, sideEffect)
            }
            on<ItemClicked<Autofill>> {
                val (state, sideEffect) = reduceItemClicked(it)
                transitionTo(state, sideEffect)
            }
            on<Search<Autofill>> { event ->
                val filteredSettings = data.autofillSettings.search(event.searchQuery)
                transitionTo(DisplaySettings(data.copy(filteredSettings = filteredSettings, searchQuery = event.searchQuery)))
            }
        }
        state<UpdateAutofill> {
            on<SettingsLoaded<Autofill>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(autofillSettings = it.settings, filteredSettings = filteredSettings)))
            }
            on<GoTo<Autofill, SideEffect>> {
                transitionTo(DisplaySettings(data), it.sideEffect)
            }
        }
        state<UpdateChromeAutofill> {
            on<SettingsLoaded<Autofill>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(autofillSettings = it.settings, filteredSettings = filteredSettings)))
            }
            on<GoTo<Autofill, SideEffect>> {
                transitionTo(DisplaySettings(data), it.sideEffect)
            }
        }
        state<UpdateKeyboardAutofill> {
            on<SettingsLoaded<Autofill>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(autofillSettings = it.settings, filteredSettings = filteredSettings)))
            }
        }
        state<UpdatePhishingAlert> {
            on<SettingsLoaded<Autofill>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(autofillSettings = it.settings, filteredSettings = filteredSettings)))
            }
        }
    }

    private fun View.reduceItemCheckChanged(event: ItemCheckChanged<Autofill>): Pair<View, SideEffect?> {
        return when (event.id) {
            EnableAutofill -> UpdateAutofill(data, event.checked) to null
            EnableChromeAutofill -> UpdateChromeAutofill(data, event.checked) to null
            KeyboardAutofill -> UpdateKeyboardAutofill(data, event.checked) to null
            PhishingAlert -> UpdatePhishingAlert(data, event.checked) to null
            else -> this to null
        }
    }

    private fun View.reduceItemClicked(event: ItemClicked<Autofill>): Pair<View, SideEffect?> {
        return when (event.id) {
            PausedAutofill -> DisplaySettings(data) to GoToPausedAutofill
            else -> this to null
        }
    }
}