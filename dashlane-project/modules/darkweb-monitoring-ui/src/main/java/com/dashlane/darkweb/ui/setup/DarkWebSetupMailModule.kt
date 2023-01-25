package com.dashlane.darkweb.ui.setup

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@InstallIn(ViewModelComponent::class)
@Module
internal abstract class DarkWebSetupMailModule {
    @Binds
    abstract fun bindDarkWebSetupMailLogger(impl: DarkWebSetupMailLoggerImpl): DarkWebSetupMailLogger
}