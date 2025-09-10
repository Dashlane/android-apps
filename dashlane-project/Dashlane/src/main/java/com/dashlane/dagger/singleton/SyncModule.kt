package com.dashlane.dagger.singleton

import com.dashlane.sync.domain.SupportedTransactionsFilter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class SyncModule {
    @Provides
    @SupportedTransactionsFilter
    @SuppressWarnings("FunctionOnlyReturningConstant")
    fun provideTransactionFilter(): List<String>? = null 
}