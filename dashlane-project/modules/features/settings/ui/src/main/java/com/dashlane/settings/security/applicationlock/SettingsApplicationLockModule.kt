package com.dashlane.settings.security.applicationlock

import android.content.Context
import arrow.core.partially1
import com.dashlane.account.UserAccountStorage
import com.dashlane.accountrecoverykey.AccountRecoveryKeyRepository
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.authentication.twofa.Use2faSettingStateHolder
import com.dashlane.biometric.BiometricSetupRepository
import com.dashlane.hardwaresecurity.BiometricAuthModule
import com.dashlane.hardwaresecurity.BiometricRecovery
import com.dashlane.lock.LockManager
import com.dashlane.pin.PinSetupRepository
import com.dashlane.server.api.endpoints.authentication.Get2FAStatusService
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.settings.ApplicationLock
import com.dashlane.settings.SettingsEvent
import com.dashlane.udf.Strategy
import com.dashlane.udf.UseCase
import com.dashlane.udf.toFilteredUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
@Module
@InstallIn(ViewModelComponent::class)
object SettingsApplicationLockModule {
    @Provides
    @ViewModelScoped
    @Suppress("kotlin:S107", "LongMethod")
    fun provideSettingsApplicationLockUseCases(
        @ApplicationContext context: Context,
        sessionManager: SessionManager,
        sessionCredentialsSaver: SessionCredentialsSaver,
        userAccountStorage: UserAccountStorage,
        lockManager: LockManager,
        biometricAuthModule: BiometricAuthModule,
        biometricRecovery: BiometricRecovery,
        accountStatusRepository: AccountStatusRepository,
        accountRecoveryKeyRepository: AccountRecoveryKeyRepository,
        get2FAStatusService: Get2FAStatusService,
        pinSetupRepository: PinSetupRepository,
        biometricSetupRepository: BiometricSetupRepository,
        use2faSettingStateHolder: Use2faSettingStateHolder,
    ): MutableList<UseCase<SettingsApplicationLockState.View, SettingsEvent<ApplicationLock>>> {
        return mutableListOf(
            ::loadApplicationLockSecuritySettings
                .partially1(context)
                .partially1(sessionManager)
                .partially1(sessionCredentialsSaver)
                .partially1(userAccountStorage)
                .partially1(lockManager)
                .partially1(biometricAuthModule)
                .partially1(biometricRecovery)
                .partially1(accountStatusRepository)
                .partially1(accountRecoveryKeyRepository)
                .partially1(get2FAStatusService)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::listenToSettingsChange
                .partially1(context)
                .partially1(pinSetupRepository)
                .partially1(biometricSetupRepository)
                .partially1(biometricRecovery)
                .partially1(accountRecoveryKeyRepository)
                .partially1(use2faSettingStateHolder)
                .toFilteredUseCase(Strategy.DropOnNewState),
            ::waitForLock
                .partially1(lockManager)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::updatePinLock
                .partially1(lockManager)
                .partially1(sessionManager)
                .partially1(pinSetupRepository)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::updateBiometricLock
                .partially1(lockManager)
                .partially1(sessionManager)
                .partially1(biometricAuthModule)
                .partially1(biometricRecovery)
                .partially1(biometricSetupRepository)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::update2FA
                .partially1(sessionManager)
                .partially1(accountStatusRepository)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::updateBiometricRecovery
                .partially1(lockManager)
                .partially1(sessionManager)
                .partially1(biometricAuthModule)
                .partially1(biometricRecovery)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::updateDisableLocal2FA
                .partially1(lockManager)
                .partially1(sessionManager)
                .partially1(sessionCredentialsSaver)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::updateAutoLockOnExit
                .partially1(lockManager)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::updateAutoLockTime
                .partially1(context)
                .partially1(lockManager)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::autoLockTimeSelectedDialogPositive
                .partially1(context)
                .partially1(lockManager)
                .partially1(sessionManager)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::dialogPositive
                .partially1(sessionManager)
                .partially1(biometricRecovery)
                .partially1(biometricSetupRepository)
                .partially1(lockManager)
                .toFilteredUseCase(Strategy.CancelOnNewState),
        )
    }
}