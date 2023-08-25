package com.dashlane.notificationcenter

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
interface NotificationCenterModule {
    @Binds
    fun bindActionItemsLogger(impl: NotificationCenterLoggerImpl): NotificationCenterLogger

    @Binds
    fun bindActionItemsRepository(impl: NotificationCenterRepositoryImpl): NotificationCenterRepository
}