package com.dashlane.followupnotification.services



interface FollowUpNotificationFlags {
    fun canUseFollowUpNotification(): Boolean
    fun haveMinimumRequiredAndroidVersion(): Boolean
}