package com.dashlane.m2w

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
internal interface M2WModule {
    @Binds
    fun bindM2wLogger(impl: M2wConnectLoggerImpl): M2wConnectLogger
}