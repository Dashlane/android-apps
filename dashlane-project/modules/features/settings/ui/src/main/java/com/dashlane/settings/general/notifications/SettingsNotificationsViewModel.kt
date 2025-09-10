package com.dashlane.settings.general.notifications

import com.dashlane.settings.Notifications
import com.dashlane.settings.SettingsEvent
import com.dashlane.settings.SettingsEvent.ItemCheckChanged
import com.dashlane.settings.SettingsEvent.ItemClicked
import com.dashlane.settings.SettingsEvent.Search
import com.dashlane.settings.SettingsEvent.SettingsLoaded
import com.dashlane.settings.general.notifications.SettingsNotificationsState.SideEffect
import com.dashlane.settings.general.notifications.SettingsNotificationsState.View
import com.dashlane.settings.general.notifications.SettingsNotificationsState.View.DisplaySettings
import com.dashlane.settings.general.notifications.SettingsNotificationsState.View.Initial
import com.dashlane.settings.general.notifications.SettingsNotificationsState.View.UpdateCopyAndPaste
import com.dashlane.settings.search
import com.dashlane.udf.StateMachine
import com.dashlane.udf.UDFViewModel
import com.dashlane.udf.UseCase
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class SettingsNotificationsViewModel @Inject constructor(
    useCases: MutableList<UseCase<View, SettingsEvent<Notifications>>>,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : UDFViewModel<View, SideEffect, SettingsEvent<Notifications>>(useCases = useCases, defaultDispatcher = defaultDispatcher) {
    init {
        start(createStateMachine())
    }

    @Suppress("LongMethod")
    internal fun createStateMachine() = StateMachine.create<View, SettingsEvent<Notifications>, SideEffect> {
        initialState(Initial(SettingsNotificationsData()))
        state<Initial> {
            on<SettingsLoaded<Notifications>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(notificationsSettings = it.settings, filteredSettings = filteredSettings)))
            }
        }
        state<DisplaySettings> {
            on<SettingsLoaded<Notifications>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(notificationsSettings = it.settings, filteredSettings = filteredSettings)))
            }
            on<ItemCheckChanged<Notifications>> {
                val (state, sideEffect) = reduceItemCheckChanged(it)
                transitionTo(state, sideEffect)
            }
            on<ItemClicked<Notifications>> {
                val (state, sideEffect) = reduceItemClicked(it)
                transitionTo(state, sideEffect)
            }
            on<Search<Notifications>> { event ->
                val filteredSettings = data.notificationsSettings.search(event.searchQuery)
                transitionTo(DisplaySettings(data.copy(filteredSettings = filteredSettings, searchQuery = event.searchQuery)))
            }
        }
        state<UpdateCopyAndPaste> {
            on<SettingsLoaded<Notifications>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(notificationsSettings = it.settings, filteredSettings = filteredSettings)))
            }
        }
    }

    private fun View.reduceItemCheckChanged(event: ItemCheckChanged<Notifications>): Pair<View, SideEffect?> {
        return when (event.id) {
            Notifications.CopyAndPaste -> UpdateCopyAndPaste(data, event.checked) to null
            else -> this to null
        }
    }

    private fun View.reduceItemClicked(event: ItemClicked<Notifications>): Pair<View, SideEffect?> {
        return when (event.id) {
            Notifications.General -> DisplaySettings(data) to SideEffect.GoToSystemSettings
            else -> this to null
        }
    }
}