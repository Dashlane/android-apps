package com.dashlane.settings.security.misc

import android.content.Context
import arrow.core.partially1
import com.dashlane.account.UserAccountStorage
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.accountstatus.subscriptioncode.SubscriptionCodeRepository
import com.dashlane.lock.LockManager
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserDataRepository
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.settings.Misc
import com.dashlane.settings.SettingsEvent
import com.dashlane.storage.userdata.RichIconsSettingProvider
import com.dashlane.udf.Strategy
import com.dashlane.udf.UseCase
import com.dashlane.udf.toFilteredUseCase
import com.dashlane.ui.ScreenshotPolicy
import com.dashlane.usercryptography.UserCryptographyRepository
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
object SettingsMiscModule {
    @Provides
    @ViewModelScoped
    @Suppress("kotlin:S107")
    fun provideSettingsMiscUseCases(
        @ApplicationContext context: Context,
        sessionManager: SessionManager,
        userAccountStorage: UserAccountStorage,
        accountStatusRepository: AccountStatusRepository,
        screenshotPolicy: ScreenshotPolicy,
        preferencesManager: PreferencesManager,
        userDatabaseRepository: UserDatabaseRepository,
        cryptographyRepository: UserCryptographyRepository,
        richIconsSettingProvider: RichIconsSettingProvider,
        subscriptionCodeRepository: SubscriptionCodeRepository,
        userDataRepository: UserDataRepository,
        lockManager: LockManager,
    ): MutableList<UseCase<SettingsMiscState.View, SettingsEvent<Misc>>> {
        return mutableListOf(
            ::loadMiscSecuritySettings
                .partially1(context)
                .partially1(sessionManager)
                .partially1(userAccountStorage)
                .partially1(accountStatusRepository)
                .partially1(screenshotPolicy)
                .partially1(preferencesManager)
                .partially1(userDatabaseRepository)
                .partially1(cryptographyRepository)
                .partially1(richIconsSettingProvider)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::updateScreenshotAllowed
                .partially1(lockManager)
                .partially1(screenshotPolicy)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::updateClearClipboard
                .partially1(sessionManager)
                .partially1(preferencesManager)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::updateCopy2Fa
                .partially1(sessionManager)
                .partially1(preferencesManager)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::updateChangeMasterPassword
                .partially1(lockManager)
                .partially1(sessionManager)
                .partially1(preferencesManager)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::updateKeyDerivation
                .partially1(sessionManager)
                .partially1(accountStatusRepository)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::updatePrivacy
                .partially1(subscriptionCodeRepository)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::updateRichIcons
                .partially1(sessionManager)
                .partially1(userDataRepository)
                .toFilteredUseCase(Strategy.CancelOnNewState),
        )
    }
}