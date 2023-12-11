package com.dashlane.ui.screens.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.core.sync.getAgnosticMessageFeedback
import com.dashlane.events.AppEvents
import com.dashlane.events.SyncFinishedEvent
import com.dashlane.events.registerAsFlow
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.events.user.UserSettings
import com.dashlane.ui.screens.settings.list.RootSettingsList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@HiltViewModel
class SettingsViewModel @Inject constructor(
    appEvents: AppEvents,
    settingsRootList: RootSettingsList,
    savedStateHandle: SavedStateHandle,
    private val userSettingsLogRepository: UserSettingsLogRepository,
    private val logRepository: LogRepository,
    private val use2faSettingStateHolder: Use2faSettingStateHolder
) : ViewModel(), SettingsViewModelContract {
    override val targetId = SettingsFragmentArgs.fromSavedStateHandle(savedStateHandle).id

    override val settingScreenItem = settingsRootList.getScreenForId(targetId)

    override var shouldHighlightSetting by savedStateHandle.value("shouldHighlightSetting", true)

    override var pendingAdapterPosition by savedStateHandle.value("pendingAdapterPosition", -1)

    override val syncFeedbacks = appEvents.registerAsFlow(
        this@SettingsViewModel,
        clazz = SyncFinishedEvent::class.java,
        deliverLastEvent = false
    ).mapNotNull { it.getAgnosticMessageFeedback() }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

    override val use2faSettingStateChanges = use2faSettingStateHolder.use2faSettingStateFlow
        .map { it.visible }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

    private var currentUserSettings: UserSettings? = null

    init {
        if (settingScreenItem.page == AnyPage.SETTINGS_SECURITY) {
            
            viewModelScope.launch { use2faSettingStateHolder.refresh() }
        }
    }

    override fun onRefresh() {
        logUserSettingsIfChanged()
    }

    override fun onSettingInteraction() {
        logUserSettingsIfChanged()
    }

    private fun logUserSettingsIfChanged() {
        val userSettings = userSettingsLogRepository.get()

        if (currentUserSettings == userSettings) return

        val skipLog = currentUserSettings == null

        currentUserSettings = userSettings

        if (skipLog) return

        logRepository.queueEvent(userSettings)
    }
}

private fun <T> SavedStateHandle.value(key: String, default: T) = object :
    ReadWriteProperty<Any, T> {
    override fun getValue(thisRef: Any, property: KProperty<*>) = get(key) ?: default

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        set(key, value)
    }
}