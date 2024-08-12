package com.dashlane.autofill.api.followup

import com.dashlane.followupnotification.api.FollowUpNotificationLockManager
import com.dashlane.followupnotification.api.FollowUpNotificationPermissionManager
import com.dashlane.followupnotification.services.FollowUpNotificationLogger
import com.dashlane.followupnotification.services.FollowUpNotificationSettingsService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FollowUpNotificationComponentExternalModule {

    @Binds
    @Singleton
    abstract fun bindsFollowUpNotificationSettingsService(service: FollowUpNotificationSettingsServiceImpl): FollowUpNotificationSettingsService

    @Binds
    @Singleton
    abstract fun bindsFollowUpNotificationLockManager(manager: FollowUpNotificationLockManagerImpl): FollowUpNotificationLockManager

    @Binds
    @Singleton
    abstract fun bindsFollowUpNotificationPermissionManager(manager: FollowUpNotificationPermissionManagerImpl): FollowUpNotificationPermissionManager

    @Binds
    @Singleton
    abstract fun bindsFollowUpNotificationLogger(logger: FollowUpNotificationLoggerImpl): FollowUpNotificationLogger
}