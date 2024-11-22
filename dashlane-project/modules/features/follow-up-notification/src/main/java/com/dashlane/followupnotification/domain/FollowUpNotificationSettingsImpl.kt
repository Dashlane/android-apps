package com.dashlane.followupnotification.domain

import com.dashlane.followupnotification.services.FollowUpNotificationFlags
import com.dashlane.followupnotification.services.FollowUpNotificationSettingsService
import com.dashlane.followupnotification.services.FollowUpNotificationsStrings
import javax.inject.Inject

class FollowUpNotificationSettingsImpl @Inject constructor(
    private val followUpNotificationsStrings: FollowUpNotificationsStrings,
    private val followUpNotificationFlags: FollowUpNotificationFlags,
    private val followUpNotificationPersistedSettingsService: FollowUpNotificationSettingsService
) : FollowUpNotificationSettings {

    override fun isAvailable(): Boolean = followUpNotificationFlags.canUseFollowUpNotification()

    override fun isActive(): Boolean = isChecked() && isAvailable()

    override fun getSetting(): FollowUpNotificationSettingModel = FollowUpNotificationSettingModel(
        isChecked = isChecked(),
        title = getTitle(),
        description = getDescription()
    )

    private fun isChecked(): Boolean =
        followUpNotificationPersistedSettingsService.isFollowUpNotificationSettingChecked()

    private fun getDescription(): String = followUpNotificationsStrings.getFollowUpNotificationsDescription()

    override fun toggleSetting(): FollowUpNotificationSettingModel {
        val nowSetting = getSetting()

        return if (isAvailable()) {
            val changedSettings =
                nowSetting.copy(isChecked = changeFollowUpNotificationSettingTo(!nowSetting.isChecked))
            changedSettings
        } else {
            nowSetting
        }
    }

    private fun changeFollowUpNotificationSettingTo(checked: Boolean): Boolean {
        return followUpNotificationPersistedSettingsService.changeFollowUpNotificationSettingTo(checked)
    }

    private fun getTitle(): String {
        return followUpNotificationsStrings.getFollowUpNotificationsTitle()
    }
}