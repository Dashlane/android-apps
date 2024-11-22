package com.dashlane.autofill.api.followup

import com.dashlane.followupnotification.services.FollowUpNotificationLogger
import com.dashlane.followupnotification.services.FollowUpNotificationSettingsService
import com.dashlane.preference.PreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.SessionManager
import javax.inject.Inject

class FollowUpNotificationSettingsServiceImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val preferencesManager: PreferencesManager,
    private val followUpNotificationLogger: FollowUpNotificationLogger
) : FollowUpNotificationSettingsService {

    private val userPreferencesManager: UserPreferencesManager
        get() = preferencesManager[sessionManager.session?.username]

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