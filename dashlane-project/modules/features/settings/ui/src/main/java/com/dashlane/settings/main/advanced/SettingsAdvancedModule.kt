package com.dashlane.settings.main.advanced

import android.content.Context
import arrow.core.partially1
import com.dashlane.lock.LockManager
import com.dashlane.settings.Advanced
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
object SettingsAdvancedModule {
    @Provides
    @ViewModelScoped
    fun provideSettingsAdvancedUseCases(
        @ApplicationContext context: Context,
        lockManager: LockManager,
    ): MutableList<UseCase<SettingsAdvancedState.View, SettingsEvent<Advanced>>> {
        return mutableListOf(
            ::loadAdvancedSettings
                .partially1(context)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::waitForLock
                .partially1(lockManager)
                .toFilteredUseCase(Strategy.CancelOnNewState),
        )
    }
}