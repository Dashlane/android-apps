package com.dashlane.login.sso

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
interface LoginSsoLoggerModule {
    @Binds
    fun bindLoginSsoLogger(impl: LoginSsoLoggerImpl): LoginSsoLogger
}