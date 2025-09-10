package com.dashlane.createaccount.tos

import arrow.core.partially1
import com.dashlane.authentication.create.AccountCreator
import com.dashlane.common.logger.adjust.AttributionsLogDataProvider
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
object CreateAccountTermsAndConditionsModule {
    @Provides
    @ViewModelScoped
    fun provideCreateAccountTermsAndConditionsInitialState(): CreateAccountTermsAndConditionsState.View {
        return CreateAccountTermsAndConditionsState.View.Initial(CreateAccountTermsAndConditionsData())
    }

    @Provides
    @ViewModelScoped
    fun provideCreateAccountTermsAndConditionsUseCases(
        createAccountRepository: CreateAccountRepository,
        attributionsLogDataProvider: AttributionsLogDataProvider,
        accountCreator: AccountCreator,
        logRepository: LogRepository,
    ): MutableList<UseCase<CreateAccountTermsAndConditionsState.View, CreateAccountTermsAndConditionsEvent>> {
        return mutableListOf(
            ::createAccount
                .partially1(createAccountRepository)
                .partially1(attributionsLogDataProvider)
                .partially1(accountCreator)
                .partially1(logRepository)
                .toFilteredUseCase(Strategy.DropOnNewState),
        )
    }
}