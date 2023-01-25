package com.dashlane.login.pages.enforce2fa

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
interface Enforce2faLimitModule {
    @Binds
    fun bindHasEnforced2faLimitUseCase(impl: HasEnforced2FaLimitUseCaseImpl): HasEnforced2faLimitUseCase
}