package com.dashlane.settings.general.notifications

import android.content.Context
import arrow.core.partially1
import com.dashlane.followupnotification.domain.FollowUpNotificationSettings
import com.dashlane.settings.Notifications
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
object SettingsNotificationsModule {
    @Provides
    @ViewModelScoped
    fun provideSettingsNotificationsUseCases(
        @ApplicationContext context: Context,
        followUpNotificationSettings: FollowUpNotificationSettings,
    ): MutableList<UseCase<SettingsNotificationsState.View, SettingsEvent<Notifications>>> {
        return mutableListOf(
            ::loadNotificationsSettings
                .partially1(context)
                .partially1(followUpNotificationSettings)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::updateCopyAndPaste
                .partially1(followUpNotificationSettings)
                .toFilteredUseCase(Strategy.CancelOnNewState),
        )
    }
}