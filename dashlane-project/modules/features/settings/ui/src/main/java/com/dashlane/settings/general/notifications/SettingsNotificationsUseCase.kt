package com.dashlane.settings.general.notifications

import android.content.Context
import com.dashlane.followupnotification.domain.FollowUpNotificationSettings
import com.dashlane.settings.Notifications
import com.dashlane.settings.SettingsEvent
import com.dashlane.settings.SettingsItemData
import com.dashlane.settings.general.notifications.SettingsNotificationsState.View.Initial
import com.dashlane.settings.general.notifications.SettingsNotificationsState.View.UpdateCopyAndPaste
import com.dashlane.settings.ui.R
import com.dashlane.settings.updateSetting
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun loadNotificationsSettings(
    @ApplicationContext context: Context,
    followUpNotificationSettings: FollowUpNotificationSettings,
    state: Initial,
): Flow<SettingsEvent<Notifications>> {
    return flow {
        val notificationSettings = mutableListOf<SettingsItemData>()

        
        notificationSettings.add(
            SettingsItemData.Click(
                id = Notifications.General,
                title = context.getString(R.string.general_notifications_settings_title),
                description = context.getString(R.string.general_notifications_settings_description),
                enabled = true,
            ),
        )

        
        notificationSettings.add(
            SettingsItemData.Toggle(
                id = Notifications.CopyAndPaste,
                title = context.getString(R.string.follow_up_settings_title),
                description = context.getString(R.string.follow_up_settings_description),
                enabled = followUpNotificationSettings.isAvailable(),
                checked = followUpNotificationSettings.getSetting().isChecked,
            ),
        )
        emit(SettingsEvent.SettingsLoaded(notificationSettings))
    }
}

fun updateCopyAndPaste(
    followUpNotificationSettings: FollowUpNotificationSettings,
    state: UpdateCopyAndPaste,
): Flow<SettingsEvent<Notifications>> {
    return flow {
        val newModel = followUpNotificationSettings.toggleSetting()
        val newSettings = state.data.notificationsSettings
            .toMutableList()
            .updateSetting(Notifications.CopyAndPaste, newModel.isChecked)
            .toList()
        emit(SettingsEvent.SettingsLoaded(newSettings))
    }
}