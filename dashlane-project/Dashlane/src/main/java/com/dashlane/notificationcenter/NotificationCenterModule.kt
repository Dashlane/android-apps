package com.dashlane.notificationcenter

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface NotificationCenterModule {
    @Binds
    fun bindActionItemsRepository(impl: NotificationCenterRepositoryImpl): NotificationCenterRepository
}