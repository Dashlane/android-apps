package com.dashlane.settings.general.usagedata

import com.dashlane.settings.SettingsEvent
import com.dashlane.settings.SettingsEvent.ItemCheckChanged
import com.dashlane.settings.UsageData
import com.dashlane.settings.UsageData.EnableUsageData
import com.dashlane.settings.general.usagedata.SettingsUsageDataState.SideEffect
import com.dashlane.settings.general.usagedata.SettingsUsageDataState.View
import com.dashlane.settings.general.usagedata.SettingsUsageDataState.View.DisplaySettings
import com.dashlane.settings.general.usagedata.SettingsUsageDataState.View.Initial
import com.dashlane.settings.general.usagedata.SettingsUsageDataState.View.UpdateUsageData
import com.dashlane.settings.search
import com.dashlane.udf.StateMachine
import com.dashlane.udf.UDFViewModel
import com.dashlane.udf.UseCase
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class SettingsUsageDataViewModel @Inject constructor(
    useCases: MutableList<UseCase<View, SettingsEvent<UsageData>>>,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : UDFViewModel<View, SideEffect, SettingsEvent<UsageData>>(useCases = useCases, defaultDispatcher = defaultDispatcher) {
    init {
        start(createStateMachine())
    }

    @Suppress("LongMethod")
    internal fun createStateMachine() = StateMachine.create<View, SettingsEvent<UsageData>, SideEffect> {
        initialState(Initial(SettingsUsageDataData()))
        state<Initial> {
            on<SettingsEvent.SettingsLoaded<UsageData>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(usageDataSettings = it.settings, filteredSettings = filteredSettings)))
            }
        }
        state<DisplaySettings> {
            on<ItemCheckChanged<UsageData>> {
                val (state, sideEffect) = reduceItemCheckChanged(it)
                transitionTo(state, sideEffect)
            }
            on<SettingsEvent.SettingsLoaded<UsageData>> {
                transitionTo(DisplaySettings(data.copy(usageDataSettings = it.settings)))
            }
            on<SettingsEvent.Search<UsageData>> { event ->
                val filteredSettings = data.usageDataSettings.search(event.searchQuery)
                transitionTo(DisplaySettings(data.copy(filteredSettings = filteredSettings, searchQuery = event.searchQuery)))
            }
        }
        state<UpdateUsageData> {
            on<SettingsEvent.SettingsLoaded<UsageData>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(usageDataSettings = it.settings, filteredSettings = filteredSettings)))
            }
        }
    }

    private fun View.reduceItemCheckChanged(event: ItemCheckChanged<UsageData>): Pair<View, SideEffect?> {
        return when (event.id) {
            EnableUsageData -> UpdateUsageData(data, event.checked) to null
            else -> this to null
        }
    }
}