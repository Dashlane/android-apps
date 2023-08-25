package com.dashlane.autofill.api.followup;

import com.dashlane.followupnotification.api.FollowUpNotificationLockManager;
import com.dashlane.followupnotification.api.FollowUpNotificationPermissionManager;
import com.dashlane.followupnotification.services.FollowUpNotificationLogger;
import com.dashlane.followupnotification.services.FollowUpNotificationSettingsService;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class FollowUpNotificationComponentExternalModule {

    @Provides
    @Named("autoRemovalElapsedTime")
    public long providesAutoRemovalElapsedTime() {
        return 30000L;
    }

    @Provides
    @Singleton
    public FollowUpNotificationSettingsService providesFollowUpNotificationSettingsService(
            FollowUpNotificationSettingsServiceImpl impl) {
        return impl;
    }

    @Provides
    public FollowUpNotificationLogger providesFollowUpNotificationLogger(FollowUpNotificationLoggerImpl impl) {
        return impl;
    }

    @Provides
    public FollowUpNotificationLockManager providesFollowUpNotificationLockManager(
            FollowUpNotificationLockManagerImpl impl) {
        return impl;
    }

    @Provides
    public FollowUpNotificationPermissionManager providesFollowUpNotificationPermissionManager(
            FollowUpNotificationPermissionManagerImpl impl) {
        return impl;
    }
}