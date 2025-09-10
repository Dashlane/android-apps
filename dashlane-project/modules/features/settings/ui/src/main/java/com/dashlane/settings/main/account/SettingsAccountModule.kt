package com.dashlane.settings.main.account

import android.content.Context
import arrow.core.partially1
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.premium.enddate.EndDateFormatter
import com.dashlane.session.SessionManager
import com.dashlane.settings.Account
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
import java.time.Clock

@ExperimentalCoroutinesApi
@FlowPreview
@Module
@InstallIn(ViewModelComponent::class)
object SettingsAccountModule {
    @Provides
    @ViewModelScoped
    fun provideSettingsAccountUseCases(
        @ApplicationContext context: Context,
        sessionManager: SessionManager,
        accountStatusRepository: AccountStatusRepository,
        frozenStateManager: FrozenStateManager,
        endDateFormatter: EndDateFormatter,
        clock: Clock,
    ): MutableList<UseCase<SettingsAccountState.View, SettingsEvent<Account>>> {
        return mutableListOf(
            ::loadAccountSettings
                .partially1(context)
                .partially1(sessionManager)
                .partially1(accountStatusRepository)
                .partially1(frozenStateManager)
                .partially1(endDateFormatter)
                .partially1(clock)
                .toFilteredUseCase(Strategy.CancelOnNewState),
        )
    }
}