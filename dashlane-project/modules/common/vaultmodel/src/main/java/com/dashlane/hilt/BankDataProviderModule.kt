package com.dashlane.hilt

import com.dashlane.regioninformation.banks.BankRepository
import com.dashlane.regioninformation.banks.BankRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class BankDataProviderModule {

    @Provides
    fun providesBankRepository(): BankRepository = BankRepositoryImpl()
}