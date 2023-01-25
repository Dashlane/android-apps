package com.dashlane.ui.screens.settings.list.general

import android.content.Context
import com.dashlane.R
import com.dashlane.followupnotification.domain.FollowUpNotificationSettingModel
import com.dashlane.followupnotification.domain.FollowUpNotificationSettings
import com.dashlane.navigation.Navigator
import com.dashlane.ui.screens.settings.item.SettingCheckable
import com.dashlane.ui.screens.settings.item.SettingHeader
import com.dashlane.ui.screens.settings.item.SettingItem



class SettingsGeneralNotificationsList(
    private val context: Context,
    navigator: Navigator,
    private val followUpNotificationSettings: FollowUpNotificationSettings
) {

    private val autoLoginHeader =
        SettingHeader(context.getString(R.string.notifications_settings_header))

    private val generalNotificationsItem = object : SettingItem {
        override val id = "general-notifications"
        override val header = autoLoginHeader
        override val title = context.getString(R.string.general_notifications_settings_title)
        override val description =
            context.getString(R.string.general_notifications_settings_description)

        override fun isEnable(context: Context) = true
        override fun isVisible(context: Context) = true
        override fun onClick(context: Context) =
            navigator.goToManageDashlaneNotificationsSystem()
    }

    private val followUpNotificationItem = object : SettingItem, SettingCheckable {
        override val id = "follow-up-notification"
        override val header = autoLoginHeader
        override val title = getFollowUpSetting().title
        override val description = getFollowUpSetting().description
        override fun isEnable(context: Context) = followUpNotificationSettings.isAvailable()
        override fun isVisible(context: Context) = followUpNotificationSettings.isSupported()
        override fun onClick(context: Context) = onCheckChanged(context, !isChecked(context))

        override fun isChecked(context: Context): Boolean {
            val followUpSetting = getFollowUpSetting()
            return followUpSetting.isChecked
        }

        override fun onCheckChanged(context: Context, enable: Boolean) {
            followUpNotificationSettings.toggleSetting()
        }

        private fun getFollowUpSetting(): FollowUpNotificationSettingModel {
            return followUpNotificationSettings.getSetting()
        }
    }

    fun getAll(): List<SettingItem> {
        val supportedInDeviceFollowUpNotificationItem =
            if (isFollowUpNotificationFeatureAvailable()) followUpNotificationItem else null
        return listOfNotNull(generalNotificationsItem, supportedInDeviceFollowUpNotificationItem)
    }

    private fun isFollowUpNotificationFeatureAvailable() = followUpNotificationSettings.isSupported()
}