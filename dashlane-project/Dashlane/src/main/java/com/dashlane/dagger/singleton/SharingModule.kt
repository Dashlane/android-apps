package com.dashlane.dagger.singleton

import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProvider
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProviderImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
open class SharingModule {
    @Provides
    @Singleton
    open fun provideSharingDataProvider(provider: SharingDataProviderImpl): SharingDataProvider {
        return provider
    }
}