package com.dashlane.settings.general.usagedata

import android.content.Context
import arrow.core.partially1
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.hermes.LogRepository
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.settings.SettingsEvent
import com.dashlane.settings.UsageData
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
object SettingsUsageDataModule {
    @Provides
    @ViewModelScoped
    fun provideSettingsUsageDataUseCases(
        @ApplicationContext context: Context,
        globalPreferencesManager: GlobalPreferencesManager,
        userFeaturesChecker: UserFeaturesChecker,
        logRepository: LogRepository,
    ): MutableList<UseCase<SettingsUsageDataState.View, SettingsEvent<UsageData>>> {
        return mutableListOf(
            ::loadUsageDataSettings
                .partially1(context)
                .partially1(globalPreferencesManager)
                .partially1(userFeaturesChecker)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::updateUsageData
                .partially1(globalPreferencesManager)
                .partially1(logRepository)
                .toFilteredUseCase(Strategy.CancelOnNewState),
        )
    }
}