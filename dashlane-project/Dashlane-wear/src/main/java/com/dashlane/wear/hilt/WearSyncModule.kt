package com.dashlane.wear.hilt

import com.dashlane.sync.DataSync
import com.dashlane.sync.domain.SupportedTransactionsFilter
import com.dashlane.wear.TypeSupportUtil.supportedVaultItemTypes
import com.dashlane.wear.credentialmanager.WearDataSync
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object WearSyncModule {
    @Provides
    @SupportedTransactionsFilter
    fun providesTransactionFilter() = supportedVaultItemTypes.map { it.transactionType }

    @Provides
    fun provideDataSync(): DataSync = WearDataSync()
}