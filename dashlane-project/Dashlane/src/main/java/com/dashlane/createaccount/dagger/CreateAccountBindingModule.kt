package com.dashlane.createaccount.dagger

import com.dashlane.login.dagger.AuthBindingModule
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import com.dashlane.createaccount.AccountCreatorImpl
import com.dashlane.createaccount.AccountCreator
import com.dashlane.createaccount.CreateAccountContract
import com.dashlane.createaccount.CreateAccountDataProvider
import com.dashlane.createaccount.pages.email.CreateAccountEmailLoggerImpl
import com.dashlane.createaccount.pages.email.CreateAccountEmailLogger
import dagger.Binds
import dagger.Module

@Module(includes = [AuthBindingModule::class])
@InstallIn(ActivityComponent::class)
interface CreateAccountBindingModule {
    @Binds
    fun bindDataProvider(dataProvider: CreateAccountDataProvider): CreateAccountContract.DataProvider

    @Binds
    fun bindAccountCreator(impl: AccountCreatorImpl): AccountCreator

    @Binds
    fun bindEmailLogger(impl: CreateAccountEmailLoggerImpl): CreateAccountEmailLogger
}