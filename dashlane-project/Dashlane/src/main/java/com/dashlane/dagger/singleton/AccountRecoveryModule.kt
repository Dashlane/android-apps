package com.dashlane.dagger.singleton

import com.dashlane.accountrecoverykey.AccountRecoveryKeyRepository
import com.dashlane.accountrecoverykey.AccountRecoveryKeyRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface AccountRecoveryModule {
    @Binds
    fun bindsAccountRecoveryKeyRepository(impl: AccountRecoveryKeyRepositoryImpl): AccountRecoveryKeyRepository
}