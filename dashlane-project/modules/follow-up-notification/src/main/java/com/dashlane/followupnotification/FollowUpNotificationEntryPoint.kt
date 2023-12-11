package com.dashlane.followupnotification

import android.content.Context
import com.dashlane.followupnotification.api.FollowUpNotificationApiProvider
import com.dashlane.followupnotification.api.FollowUpNotificationLockManager
import com.dashlane.followupnotification.api.FollowUpNotificationPermissionManager
import com.dashlane.followupnotification.data.FollowUpNotificationRepository
import com.dashlane.followupnotification.domain.FollowUpNotificationSettings
import com.dashlane.followupnotification.services.FollowUpNotificationDiscoveryService
import com.dashlane.followupnotification.services.FollowUpNotificationDisplayService
import com.dashlane.followupnotification.services.FollowUpNotificationLogger
import com.dashlane.util.Toaster
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FollowUpNotificationEntryPoint {
    val followUpNotificationApiProvider: FollowUpNotificationApiProvider
    val toaster: Toaster
    val followUpNotificationDisplayService: FollowUpNotificationDisplayService
    val followUpNotificationRepository: FollowUpNotificationRepository
    val followUpNotificationLogger: FollowUpNotificationLogger
    val followUpNotificationSettings: FollowUpNotificationSettings
    val followUpNotificationDiscoveryService: FollowUpNotificationDiscoveryService
    val followUpNotificationLockManager: FollowUpNotificationLockManager
    val followUpNotificationPermissionManager: FollowUpNotificationPermissionManager

    companion object {
        operator fun invoke(context: Context) = EntryPointAccessors.fromApplication(
            context,
            FollowUpNotificationEntryPoint::class.java
        )
    }
}
