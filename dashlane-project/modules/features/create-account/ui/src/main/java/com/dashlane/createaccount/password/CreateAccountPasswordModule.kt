package com.dashlane.createaccount.password

import arrow.core.partially1
import com.dashlane.createaccount.CreateAccountRepository
import com.dashlane.debug.services.DaDaDaLogin
import com.dashlane.hardwaresecurity.BiometricAuthModule
import com.dashlane.passwordstrength.PasswordStrengthEvaluator
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
object CreateAccountPasswordModule {
    @Provides
    @ViewModelScoped
    fun provideCreateAccountPasswordUseCases(
        passwordStrengthEvaluator: PasswordStrengthEvaluator,
        createAccountRepository: CreateAccountRepository,
        biometricAuthModule: BiometricAuthModule,
        daDaDaLogin: DaDaDaLogin,
    ): MutableList<UseCase<CreateAccountPasswordState.View, CreateAccountPasswordEvent>> {
        return mutableListOf(
            ::checkDebugPassword
                .partially1(daDaDaLogin)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::passwordChanged
                .partially1(passwordStrengthEvaluator)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::checkDebugPasswordForConfirm
                .partially1(daDaDaLogin)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::savePassword
                .partially1(createAccountRepository)
                .partially1(biometricAuthModule)
                .toFilteredUseCase(Strategy.DropOnNewState),
        )
    }
}