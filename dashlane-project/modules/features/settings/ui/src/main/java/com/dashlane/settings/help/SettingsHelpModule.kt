package com.dashlane.settings.help

import android.content.Context
import arrow.core.partially1
import com.dashlane.crashreport.CrashReporter
import com.dashlane.settings.Help
import com.dashlane.settings.SettingsEvent
import com.dashlane.udf.Strategy
import com.dashlane.udf.UseCase
import com.dashlane.udf.toFilteredUseCase
import com.dashlane.util.clipboard.ClipboardCopy
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
object SettingsHelpModule {
    @Provides
    @ViewModelScoped
    fun provideSettingsHelpUseCases(
        @ApplicationContext context: Context,
        crashReporter: CrashReporter,
        clipboardCopy: ClipboardCopy,
    ): MutableList<UseCase<SettingsHelpState.View, SettingsEvent<Help>>> {
        return mutableListOf(
            ::loadHelpSettings
                .partially1(context)
                .partially1(crashReporter)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::updateUserSupportId
                .partially1(clipboardCopy)
                .toFilteredUseCase(Strategy.CancelOnNewState),
        )
    }
}