package com.dashlane.followupnotification.services



interface FollowUpNotificationSettingsService {
    fun isFollowUpNotificationSettingChecked(): Boolean
    fun changeFollowUpNotificationSettingTo(checked: Boolean): Boolean
}
