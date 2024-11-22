package com.dashlane.followupnotification.domain

interface FollowUpNotificationSettings {

    fun isAvailable(): Boolean

    fun isActive(): Boolean
    fun getSetting(): FollowUpNotificationSettingModel
    fun toggleSetting(): FollowUpNotificationSettingModel
}