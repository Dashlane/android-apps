package com.dashlane.settings.general.usagedata

import android.content.Context
import com.dashlane.featureflipping.FeatureFlip
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.events.user.ToggleAnalytics
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.settings.SettingsEvent
import com.dashlane.settings.SettingsItemData
import com.dashlane.settings.UsageData
import com.dashlane.settings.general.usagedata.SettingsUsageDataState.View.Initial
import com.dashlane.settings.general.usagedata.SettingsUsageDataState.View.UpdateUsageData
import com.dashlane.settings.ui.R
import com.dashlane.settings.updateSetting
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun loadUsageDataSettings(
    @ApplicationContext context: Context,
    globalPreferencesManager: GlobalPreferencesManager,
    userFeaturesChecker: UserFeaturesChecker,
    state: Initial,
): Flow<SettingsEvent<UsageData>> {
    return flow {
        val usageDataSettings = mutableListOf<SettingsItemData>()

        if (userFeaturesChecker.has(FeatureFlip.SHOW_ALLOW_SEND_LOGS)) {
            usageDataSettings.add(
                SettingsItemData.Toggle(
                    id = UsageData.EnableUsageData,
                    title = context.getString(R.string.setting_allow_logs_title),
                    description = context.getString(R.string.setting_allow_logs_description),
                    enabled = true,
                    checked = globalPreferencesManager.allowSendLogs,
                ),
            )
        }
        emit(SettingsEvent.SettingsLoaded(usageDataSettings))
    }
}

fun updateUsageData(
    globalPreferencesManager: GlobalPreferencesManager,
    logRepository: LogRepository,
    state: UpdateUsageData,
): Flow<SettingsEvent<UsageData>> {
    return flow {
        
        
        if (state.checked) {
            globalPreferencesManager.allowSendLogs = true
            logRepository.apply {
                queueEvent(ToggleAnalytics(isAnalyticsEnabled = true))
                sessionEnded()
            }
        } else {
            logRepository.apply {
                queueEvent(ToggleAnalytics(isAnalyticsEnabled = false))
                sessionEnded()
            }
            globalPreferencesManager.allowSendLogs = false
        }

        val newSettings = state.data.usageDataSettings.toMutableList().updateSetting(UsageData.EnableUsageData, state.checked).toList()
        emit(SettingsEvent.SettingsLoaded(newSettings))
    }
}