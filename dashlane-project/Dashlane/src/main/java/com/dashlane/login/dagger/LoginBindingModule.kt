package com.dashlane.login.dagger

import com.dashlane.login.LoginLogger
import com.dashlane.login.LoginLoggerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module(includes = [AuthBindingModule::class])
@InstallIn(SingletonComponent::class)
interface LoginBindingModule {

    @Binds
    fun bindsLoginLogger(impl: LoginLoggerImpl): LoginLogger
}