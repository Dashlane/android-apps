package com.dashlane.followupnotification.api

interface FollowUpNotificationPermissionManager {
    fun isNotificationPermissionGranted(): Boolean
}