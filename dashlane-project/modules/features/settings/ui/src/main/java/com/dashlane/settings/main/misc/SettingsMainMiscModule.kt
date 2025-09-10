package com.dashlane.settings.main.misc

import android.content.Context
import arrow.core.partially1
import com.dashlane.account.UserAccountStorage
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.accountstatus.subscriptioncode.SubscriptionCodeRepository
import com.dashlane.debug.services.DaDaDaVersion
import com.dashlane.hermes.LogRepository
import com.dashlane.server.api.endpoints.invitation.GetSharingLinkService
import com.dashlane.session.SessionManager
import com.dashlane.settings.MainMisc
import com.dashlane.settings.SettingsEvent
import com.dashlane.udf.Strategy
import com.dashlane.udf.UseCase
import com.dashlane.udf.toFilteredUseCase
import com.dashlane.util.NetworkStateProvider
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
object SettingsMainMiscModule {
    @Provides
    @ViewModelScoped
    @Suppress("kotlin:S107", "LongMethod")
    fun provideSettingsMainMiscUseCases(
        @ApplicationContext context: Context,
        sessionManager: SessionManager,
        accountStatusRepository: AccountStatusRepository,
        dadadaVersion: DaDaDaVersion,
        networkStateProvider: NetworkStateProvider,
        subscriptionCodeRepository: SubscriptionCodeRepository,
        getSharingLinkService: GetSharingLinkService,
        userAccountStorage: UserAccountStorage,
        logRepository: LogRepository,
        clipboardCopy: ClipboardCopy,
    ): MutableList<UseCase<SettingsMainMiscState.View, SettingsEvent<MainMisc>>> {
        return mutableListOf(
            ::loadMainMiscSettings
                .partially1(context)
                .partially1(sessionManager)
                .partially1(accountStatusRepository)
                .partially1(dadadaVersion)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::dialogPositive
                .partially1(logRepository)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::updateInviteFriend
                .partially1(context)
                .partially1(networkStateProvider)
                .partially1(subscriptionCodeRepository)
                .partially1(getSharingLinkService)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::updateLogout
                .partially1(sessionManager)
                .partially1(userAccountStorage)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::updateVersion
                .partially1(context)
                .partially1(dadadaVersion)
                .partially1(clipboardCopy)
                .toFilteredUseCase(Strategy.CancelOnNewState),
        )
    }
}