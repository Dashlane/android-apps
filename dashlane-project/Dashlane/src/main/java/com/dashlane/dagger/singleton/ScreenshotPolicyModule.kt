package com.dashlane.dagger.singleton

import com.dashlane.ui.ScreenshotPolicy
import com.dashlane.ui.ScreenshotPolicyImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ScreenshotPolicyModule {
    @Binds
    abstract fun bindScreenshotPolicy(impl: ScreenshotPolicyImpl): ScreenshotPolicy
}