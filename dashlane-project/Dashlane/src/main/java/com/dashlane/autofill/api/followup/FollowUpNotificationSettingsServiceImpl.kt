package com.dashlane.autofill.api.followup

import com.dashlane.followupnotification.services.FollowUpNotificationLogger
import com.dashlane.followupnotification.services.FollowUpNotificationSettingsService
import com.dashlane.preference.UserPreferencesManager
import javax.inject.Inject

class FollowUpNotificationSettingsServiceImpl @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager,
    private val followUpNotificationLogger: FollowUpNotificationLogger
) : FollowUpNotificationSettingsService {

    override fun changeFollowUpNotificationSettingTo(checked: Boolean): Boolean {
        if (checked) {
            followUpNotificationLogger.logActivateFollowUpNotification()
        } else {
            followUpNotificationLogger.logDeactivateFollowUpNotification()
        }
        userPreferencesManager.isFollowUpNotificationChecked = checked
        return isFollowUpNotificationSettingChecked()
    }

    override fun isFollowUpNotificationSettingChecked(): Boolean {
        return userPreferencesManager.isFollowUpNotificationChecked
    }
}