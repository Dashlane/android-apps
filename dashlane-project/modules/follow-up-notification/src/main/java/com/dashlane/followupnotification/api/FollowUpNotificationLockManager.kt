package com.dashlane.followupnotification.api

interface FollowUpNotificationLockManager {
    fun isAccountLocked(): Boolean
    fun askForUnlockAndExecute(onUnlockSuccessful: () -> Unit)
}