package com.dashlane.dagger

import com.dashlane.notificator.Notificator
import com.dashlane.ui.widgets.NotificatorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
fun interface NotificatorModule {

    @Binds
    fun bindsNotificator(impl: NotificatorImpl): Notificator
}