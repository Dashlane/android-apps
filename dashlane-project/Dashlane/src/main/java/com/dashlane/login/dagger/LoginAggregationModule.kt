package com.dashlane.login.dagger

import com.dashlane.createaccount.CreateAccountAuthModule
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module(includes = [LoginBindingModule::class, LoginAuthModule::class, CreateAccountAuthModule::class, LoginCreateAccountSharedModule::class])
@InstallIn(
    ActivityComponent::class
)
class LoginAggregationModule