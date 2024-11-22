package com.dashlane.followupnotification.services

import com.dashlane.followupnotification.api.FollowUpNotificationPermissionManager
import javax.inject.Inject

class FollowUpNotificationFlagsImpl @Inject constructor(
    private val permissionsManager: FollowUpNotificationPermissionManager
) : FollowUpNotificationFlags {

    override fun canUseFollowUpNotification(): Boolean = permissionsManager.isNotificationPermissionGranted()
}