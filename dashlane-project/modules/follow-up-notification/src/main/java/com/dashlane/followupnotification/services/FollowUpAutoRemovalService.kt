package com.dashlane.followupnotification.services



interface FollowUpAutoRemovalService {
    fun registerToRemove(followUpNotificationId: String)
    fun cancelRemoval(followUpNotificationId: String)
}