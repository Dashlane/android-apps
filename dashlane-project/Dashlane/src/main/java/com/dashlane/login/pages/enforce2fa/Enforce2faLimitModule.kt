package com.dashlane.login.pages.enforce2fa

import arrow.core.partially1
import com.dashlane.limitations.Enforce2faLimiter
import com.dashlane.session.SessionManager
import com.dashlane.udf.Strategy
import com.dashlane.udf.UseCase
import com.dashlane.udf.toFilteredUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(ExperimentalCoroutinesApi::class)
@FlowPreview
@Module
@InstallIn(ViewModelComponent::class)
object Enforce2faLimitModule {
    @Provides
    @ViewModelScoped
    fun provideUseCases(
        enforce2faLimiter: Enforce2faLimiter,
        sessionManager: SessionManager,
    ): MutableList<UseCase<Enforce2faLimitState.View, Enforce2faLimitEvent>> {
        return mutableListOf(
            ::updateRedirectionFlag
                .partially1(enforce2faLimiter)
                .toFilteredUseCase(Strategy.DropOnNewState),
            ::logout
                .partially1(sessionManager)
                .toFilteredUseCase(Strategy.DropOnNewState),
        )
    }
}