package com.dashlane.biometric

import arrow.core.partially1
import com.dashlane.hardwaresecurity.BiometricAuthModule
import com.dashlane.hardwaresecurity.BiometricRecovery
import com.dashlane.lock.LockManager
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
object BiometricSetupModule {
    @Provides
    @ViewModelScoped
    fun provideBiometricSetupUseCases(
        lockManager: LockManager,
        biometricAuthModule: BiometricAuthModule,
        sessionManager: SessionManager,
        biometricSetupRepository: BiometricSetupRepository,
        biometricRecovery: BiometricRecovery,
        preferencesManager: PreferencesManager,
    ): MutableList<UseCase<BiometricSetupState.View, BiometricSetupEvent>> {
        return mutableListOf(
            ::startBiometricSetup
                .partially1(biometricAuthModule)
                .partially1(sessionManager)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::startBiometricRecoverySetup
                .partially1(lockManager)
                .partially1(sessionManager)
                .partially1(biometricRecovery)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::saveBiometric
                .partially1(sessionManager)
                .partially1(biometricSetupRepository)
                .partially1(biometricRecovery)
                .partially1(preferencesManager)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::saveBiometricRecovery
                .partially1(biometricRecovery)
                .toFilteredUseCase(Strategy.CancelOnNewState),
        )
    }
}