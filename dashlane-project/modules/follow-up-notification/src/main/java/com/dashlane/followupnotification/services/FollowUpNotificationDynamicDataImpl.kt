package com.dashlane.followupnotification.services

import android.os.Build
import java.util.UUID
import javax.inject.Inject



class FollowUpNotificationDynamicDataImpl @Inject constructor() : FollowUpNotificationDynamicData {
    override fun generateRandomUUIDString() = UUID.randomUUID().toString()
    override fun deviceBuildVersionCode() = Build.VERSION.SDK_INT
}