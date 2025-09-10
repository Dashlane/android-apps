package com.dashlane.settings.general.backup

import android.content.Context
import arrow.core.partially1
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.lock.LockManager
import com.dashlane.session.SessionManager
import com.dashlane.settings.Backup
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
object SettingsBackupModule {
    @Provides
    @ViewModelScoped
    fun provideSettingsBackupUseCases(
        @ApplicationContext context: Context,
        sessionManager: SessionManager,
        accountStatusRepository: AccountStatusRepository,
        lockManager: LockManager,
    ): MutableList<UseCase<SettingsBackupState.View, SettingsEvent<Backup>>> {
        return mutableListOf(
            ::loadBackupSettings
                .partially1(context)
                .partially1(sessionManager)
                .partially1(accountStatusRepository)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::waitForLock
                .partially1(lockManager)
                .toFilteredUseCase(Strategy.CancelOnNewState),
        )
    }
}