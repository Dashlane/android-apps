package com.dashlane.dagger

import com.dashlane.ApplicationObserver
import com.dashlane.DashlaneApplicationObserver
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ApplicationObserverModule {

    @Singleton
    @Binds
    abstract fun bindApplicationObserver(observer: DashlaneApplicationObserver): ApplicationObserver
}