package com.dashlane.followupnotification.services

import android.os.Build
import com.dashlane.followupnotification.api.FollowUpNotificationPermissionManager
import javax.inject.Inject
import javax.inject.Named

class FollowUpNotificationFlagsImpl @Inject constructor(
    private val followUpNotificationDynamicData: FollowUpNotificationDynamicData,
    @Named("autoRemovalElapsedTime") val notificationAutoRemovalTime: Long,
    private val permissionsManager: FollowUpNotificationPermissionManager
) : FollowUpNotificationFlags {

    

    override fun canUseFollowUpNotification(): Boolean =
        haveMinimumRequiredAndroidVersion() && permissionsManager.isNotificationPermissionGranted()

    

    override fun haveMinimumRequiredAndroidVersion(): Boolean =
        followUpNotificationDynamicData.deviceBuildVersionCode() >= Build.VERSION_CODES.Q
}