package com.dashlane.followupnotification.data

import com.dashlane.followupnotification.domain.FollowUpNotification

interface FollowUpNotificationRepository {
    fun add(followUpNotification: FollowUpNotification)
    fun remove(followUpNotificationId: String)
    fun removeAll()
    fun get(followUpNotificationId: String): FollowUpNotification?
    fun notificationsCount(): Int
}
