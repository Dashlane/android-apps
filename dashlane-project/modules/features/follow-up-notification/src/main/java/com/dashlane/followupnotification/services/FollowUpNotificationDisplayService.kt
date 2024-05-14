package com.dashlane.followupnotification.services

import com.dashlane.followupnotification.domain.FollowUpNotification

interface FollowUpNotificationDisplayService {
    fun displayNotification(followUpNotification: FollowUpNotification)
    fun dismiss(followUpNotificationId: String)
}
