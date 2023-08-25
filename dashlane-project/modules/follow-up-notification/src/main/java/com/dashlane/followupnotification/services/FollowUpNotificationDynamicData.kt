package com.dashlane.followupnotification.services

interface FollowUpNotificationDynamicData {
    fun generateRandomUUIDString(): String
    fun deviceBuildVersionCode(): Int
}