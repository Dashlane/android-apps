package com.dashlane.followupnotification

import android.content.Context
import com.dashlane.followupnotification.api.FollowUpNotificationApiProvider
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FollowUpNotificationEntryPoint {
    val followUpNotificationApiProvider: FollowUpNotificationApiProvider

    companion object {
        operator fun invoke(context: Context) = EntryPointAccessors.fromApplication(
            context,
            FollowUpNotificationEntryPoint::class.java
        )
    }
}
