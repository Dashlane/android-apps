package com.dashlane.createaccount.dagger

import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import com.dashlane.createaccount.AccountCreatorImpl
import com.dashlane.authentication.create.AccountCreator
import com.dashlane.createaccount.CreateAccountContract
import com.dashlane.createaccount.CreateAccountDataProvider
import com.dashlane.createaccount.pages.email.CreateAccountEmailLoggerImpl
import com.dashlane.createaccount.pages.email.CreateAccountEmailLogger
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(ActivityComponent::class)
interface CreateAccountBindingModule {
    @Binds
    fun bindDataProvider(dataProvider: CreateAccountDataProvider): CreateAccountContract.DataProvider

    @Binds
    fun bindEmailLogger(impl: CreateAccountEmailLoggerImpl): CreateAccountEmailLogger
}

@Module
@InstallIn(SingletonComponent::class)
object AccountCreatorModule {

    @Provides
    fun bindAccountCreator(impl: AccountCreatorImpl): AccountCreator = impl
}
