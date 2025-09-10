package com.dashlane.createaccount

import com.dashlane.authentication.create.AccountCreator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AccountCreatorModule {
    @Provides
    fun bindAccountCreator(impl: AccountCreatorImpl): AccountCreator = impl
}