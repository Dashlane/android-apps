package com.dashlane.accountsummary.changelogin

import arrow.core.partially1
import com.dashlane.hermes.LogRepository
import com.dashlane.server.api.endpoints.user.UserRequestLoginChange
import com.dashlane.session.SessionManager
import com.dashlane.udf.Strategy
import com.dashlane.udf.UseCase
import com.dashlane.udf.toFilteredUseCase
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
@Module
@InstallIn(ViewModelComponent::class)
object ChangeLoginEmailModule {
    @Provides
    @ViewModelScoped
    fun provideChangeLoginEmailUseCases(
        sessionManager: SessionManager,
        userRequestLoginChange: UserRequestLoginChange,
        logRepository: LogRepository,
        @IoCoroutineDispatcher ioCoroutineDispatcher: CoroutineDispatcher,
    ): MutableList<UseCase<ChangeLoginEmailState.View, ChangeLoginEmailEvent>> {
        return mutableListOf(
            ::loadEmail
                .partially1(sessionManager)
                .partially1(logRepository)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::requestLoginChange
                .partially1(sessionManager)
                .partially1(userRequestLoginChange)
                .partially1(logRepository)
                .partially1(ioCoroutineDispatcher)
                .toFilteredUseCase(Strategy.CancelOnNewState),
        )
    }
}