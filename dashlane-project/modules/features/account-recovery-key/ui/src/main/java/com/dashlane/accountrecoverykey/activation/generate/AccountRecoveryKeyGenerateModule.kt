package com.dashlane.accountrecoverykey.activation.generate

import arrow.core.partially1
import com.dashlane.accountrecoverykey.AccountRecoveryKeyRepository
import com.dashlane.hermes.LogRepository
import com.dashlane.preference.PreferencesManager
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

@ExperimentalCoroutinesApi
@FlowPreview
@Module
@InstallIn(ViewModelComponent::class)
object AccountRecoveryKeyGenerateModule {
    @Provides
    @ViewModelScoped
    fun provideAccountRecoveryKeyGenerateUseCases(
        sessionManager: SessionManager,
        preferencesManager: PreferencesManager,
        accountRecoveryKeyRepository: AccountRecoveryKeyRepository,
        logRepository: LogRepository,
    ): MutableList<UseCase<AccountRecoveryKeyGenerateState, AccountRecoveryKeyGenerateEvent>> {
        return mutableListOf(
            ::activateArk
                .partially1(sessionManager)
                .partially1(preferencesManager)
                .partially1(accountRecoveryKeyRepository)
                .partially1(logRepository)
                .toFilteredUseCase(Strategy.CancelOnNewState),
        )
    }
}