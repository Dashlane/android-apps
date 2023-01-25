package com.dashlane.notificationcenter

import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
object NotificationCenterFragmentModule {

    @Provides
    fun bindNotificationCenterLoggerOriginProvider(fragment: Fragment): NotificationCenterLogger.OriginProvider =
        (fragment as NotificationCenterLogger.OriginProvider)
}
