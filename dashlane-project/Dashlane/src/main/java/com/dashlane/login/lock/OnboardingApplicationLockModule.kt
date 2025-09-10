package com.dashlane.login.lock

import arrow.core.partially1
import com.dashlane.hardwaresecurity.BiometricAuthModule
import com.dashlane.hardwaresecurity.SecurityHelper
import com.dashlane.lock.LockTypeManager
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

@OptIn(ExperimentalCoroutinesApi::class)
@FlowPreview
@Module
@InstallIn(ViewModelComponent::class)
object OnboardingApplicationLockModule {
    @Provides
    @ViewModelScoped
    fun provideUseCases(
        biometricAuthModule: BiometricAuthModule,
        preferencesManager: PreferencesManager,
        sessionManager: SessionManager,
        lockTypeManager: LockTypeManager,
        securityHelper: SecurityHelper,
    ): MutableList<UseCase<OnboardingApplicationLockState.View, OnboardingApplicationLockEvent>> {
        return mutableListOf(
            ::initialiseOnboardingApplicationLock
                .partially1(preferencesManager)
                .partially1(sessionManager)
                .toFilteredUseCase(Strategy.DropOnNewState),
            ::refresh
                .partially1(sessionManager)
                .partially1(lockTypeManager)
                .partially1(securityHelper)
                .partially1(biometricAuthModule)
                .toFilteredUseCase(Strategy.DropOnNewState),
        )
    }
}