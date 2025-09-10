package com.dashlane.dagger.singleton

import com.dashlane.sharing.SharingDataProvider
import com.dashlane.sharing.util.AuditLogHelper
import com.dashlane.sharing.util.AuditLogHelperImpl
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

    @Provides
    @Singleton
    open fun provideAuditLogHelper(provider: AuditLogHelperImpl): AuditLogHelper {
        return provider
    }
}