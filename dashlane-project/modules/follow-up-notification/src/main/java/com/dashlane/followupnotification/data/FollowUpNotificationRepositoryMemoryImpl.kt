package com.dashlane.followupnotification.data

import com.dashlane.followupnotification.domain.FollowUpNotification
import javax.inject.Inject



class FollowUpNotificationRepositoryMemoryImpl @Inject constructor() : FollowUpNotificationRepository {
    private val mapFollowUpNotification = mutableMapOf<String, FollowUpNotification>()

    override fun add(followUpNotification: FollowUpNotification) {
        mapFollowUpNotification[followUpNotification.id] = followUpNotification
    }

    override fun remove(followUpNotificationId: String) {
        mapFollowUpNotification.remove(followUpNotificationId)
    }

    override fun removeAll() {
        mapFollowUpNotification.clear()
    }

    override fun get(followUpNotificationId: String) = mapFollowUpNotification[followUpNotificationId]

    override fun notificationsCount() = mapFollowUpNotification.size
}