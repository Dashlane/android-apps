package com.dashlane.dagger.singleton

import com.dashlane.vault.util.IdentityNameHolderService
import com.dashlane.vault.util.IdentityNameHolderServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface IdentityServiceModule {

    @Binds
    fun bindIdentityNameHolderService(impl: IdentityNameHolderServiceImpl): IdentityNameHolderService
}