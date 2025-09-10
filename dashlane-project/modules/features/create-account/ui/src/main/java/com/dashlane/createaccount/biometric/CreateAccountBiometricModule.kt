package com.dashlane.createaccount.biometric

import arrow.core.partially1
import com.dashlane.createaccount.CreateAccountRepository
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
object CreateAccountBiometricModule {
    @Provides
    @ViewModelScoped
    fun provideCreateAccountBiometricUseCases(
        createAccountRepository: CreateAccountRepository,
    ): MutableList<UseCase<CreateAccountBiometricState.View, CreateAccountBiometricEvent>> {
        return mutableListOf(
            ::saveBiometricSettings
                .partially1(createAccountRepository)
                .toFilteredUseCase(Strategy.DropOnNewState),
        )
    }
}