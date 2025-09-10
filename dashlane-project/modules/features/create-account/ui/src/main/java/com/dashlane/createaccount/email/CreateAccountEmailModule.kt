package com.dashlane.createaccount.email

import arrow.core.partially1
import com.dashlane.authentication.create.AccountCreationEmailRepository
import com.dashlane.createaccount.CreateAccountRepository
import com.dashlane.hermes.LogRepository
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

@ExperimentalCoroutinesApi
@FlowPreview
@Module
@InstallIn(ViewModelComponent::class)
object CreateAccountEmailModule {
    @Provides
    @ViewModelScoped
    fun provideCreateAccountEmailUseCases(
        createAccountRepository: CreateAccountRepository,
        accountCreationEmailRepository: AccountCreationEmailRepository,
        logRepository: LogRepository,
    ): MutableList<UseCase<CreateAccountEmailState.View, CreateAccountEmailEvent>> {
        return mutableListOf(
            ::validateEmail
                .partially1(createAccountRepository)
                .partially1(accountCreationEmailRepository)
                .partially1(logRepository)
                .toFilteredUseCase(Strategy.CancelOnNewState),
        )
    }
}