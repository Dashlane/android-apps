package com.dashlane.login.dagger

import com.dashlane.login.LoginIntentFactoryImpl
import com.dashlane.login.LoginLogger
import com.dashlane.login.LoginLoggerImpl
import com.dashlane.navigation.LoginIntentFactory
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface LoginBindingModule {
    @Binds
    fun bindsLoginLogger(impl: LoginLoggerImpl): LoginLogger

    @Binds
    fun bindsLoginSuccessIntentFactory(impl: LoginIntentFactoryImpl): LoginIntentFactory
}