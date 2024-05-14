package com.dashlane.dagger.singleton

import com.dashlane.ui.ScreenshotPolicy
import com.dashlane.ui.ScreenshotPolicyImpl
import com.dashlane.ui.screens.settings.WindowConfigurationImpl
import com.dashlane.util.WindowConfiguration
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface WindowConfigurationModule {

    @Binds
    fun bindWindowConfiguration(impl: WindowConfigurationImpl): WindowConfiguration

    @Binds
    fun bindScreenshotPolicy(impl: ScreenshotPolicyImpl): ScreenshotPolicy
}