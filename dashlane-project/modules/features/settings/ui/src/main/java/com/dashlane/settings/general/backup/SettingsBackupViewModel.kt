package com.dashlane.settings.general.backup

import com.dashlane.settings.Backup
import com.dashlane.settings.Backup.Export
import com.dashlane.settings.Backup.ExportCsv
import com.dashlane.settings.Backup.Import
import com.dashlane.settings.SettingsEvent
import com.dashlane.settings.SettingsEvent.ItemClicked
import com.dashlane.settings.SettingsEvent.Search
import com.dashlane.settings.SettingsEvent.SettingsLoaded
import com.dashlane.settings.SettingsEvent.Unlocked
import com.dashlane.settings.general.backup.SettingsBackupState.SideEffect
import com.dashlane.settings.general.backup.SettingsBackupState.View
import com.dashlane.settings.general.backup.SettingsBackupState.View.DisplaySettings
import com.dashlane.settings.general.backup.SettingsBackupState.View.Initial
import com.dashlane.settings.general.backup.SettingsBackupState.View.WaitingForLock
import com.dashlane.settings.search
import com.dashlane.udf.StateMachine
import com.dashlane.udf.UDFViewModel
import com.dashlane.udf.UseCase
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class SettingsBackupViewModel @Inject constructor(
    useCases: MutableList<UseCase<View, SettingsEvent<Backup>>>,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : UDFViewModel<View, SideEffect, SettingsEvent<Backup>>(useCases = useCases, defaultDispatcher = defaultDispatcher) {
    init {
        start(createStateMachine())
    }

    @Suppress("LongMethod")
    internal fun createStateMachine() = StateMachine.create<View, SettingsEvent<Backup>, SideEffect> {
        initialState(Initial(SettingsBackupData()))
        state<Initial> {
            on<SettingsLoaded<Backup>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(backupSettings = it.settings, filteredSettings = filteredSettings)))
            }
        }
        state<DisplaySettings> {
            on<SettingsLoaded<Backup>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(backupSettings = it.settings, filteredSettings = filteredSettings)))
            }
            on<ItemClicked<Backup>> {
                val (state, sideEffect) = reduceItemClicked(it)
                transitionTo(state, sideEffect)
            }
            on<Search<Backup>> { event ->
                val filteredSettings = data.backupSettings.search(event.searchQuery)
                transitionTo(DisplaySettings(data.copy(filteredSettings = filteredSettings, searchQuery = event.searchQuery)))
            }
        }
        state<WaitingForLock> {
            on<Unlocked<Backup, SideEffect>> {
                transitionTo(DisplaySettings(data), it.sideEffect)
            }
            on<SettingsLoaded<Backup>> {
                val filteredSettings = it.settings.search(data.searchQuery)
                transitionTo(DisplaySettings(data.copy(backupSettings = it.settings, filteredSettings = filteredSettings)))
            }
        }
    }

    private fun View.reduceItemClicked(event: ItemClicked<Backup>): Pair<View, SideEffect?> {
        return when (event.id) {
            Export -> WaitingForLock(data, sideEffect = SideEffect.StartExport) to SideEffect.DisplayLock(true)
            ExportCsv -> WaitingForLock(data, sideEffect = SideEffect.GoToExportCsv) to SideEffect.DisplayLock(true)
            Import -> DisplaySettings(data) to SideEffect.GoToImport
            else -> this to null
        }
    }
}