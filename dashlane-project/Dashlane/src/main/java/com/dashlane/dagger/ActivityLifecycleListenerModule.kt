package com.dashlane.dagger

import com.dashlane.ui.ActivityLifecycleListener
import com.dashlane.ui.GlobalActivityLifecycleListener
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ActivityLifecycleListenerModule {

    @Provides
    fun provideActivityLifecycleListener(
        globalActivityLifecycleListener: GlobalActivityLifecycleListener
    ): ActivityLifecycleListener =
        globalActivityLifecycleListener
}