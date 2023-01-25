package com.dashlane.autofill.api.followup

import android.Manifest
import android.os.Build
import com.dashlane.followupnotification.api.FollowUpNotificationPermissionManager
import com.dashlane.permission.PermissionsManager
import javax.inject.Inject

class FollowUpNotificationPermissionManagerImpl @Inject constructor(
    private val permissionsManager: PermissionsManager
) : FollowUpNotificationPermissionManager {
    override fun isNotificationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsManager.isAllowed(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true
        }
    }
}