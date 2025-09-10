package com.dashlane.accountsummary.changelogin.verification

import arrow.core.partially1
import com.dashlane.createaccount.AccountCreationSetup
import com.dashlane.database.DatabaseProvider
import com.dashlane.debug.services.DaDaDaBase
import com.dashlane.hardwaresecurity.CryptoObjectHelper
import com.dashlane.hardwaresecurity.SecurityHelper
import com.dashlane.hermes.LogRepository
import com.dashlane.lock.LockManager
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.PreferencesManager
import com.dashlane.server.api.endpoints.authenticationqa.QaGetAllTokensForTestLoginService
import com.dashlane.server.api.endpoints.user.UserCompleteLoginChange
import com.dashlane.server.api.endpoints.user.UserSendLoginChangeToken
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionTrasher
import com.dashlane.storage.securestorage.LocalKeyRepository
import com.dashlane.storage.securestorage.UserSecureStorageManager
import com.dashlane.sync.DataSync
import com.dashlane.udf.Strategy
import com.dashlane.udf.UseCase
import com.dashlane.udf.toFilteredUseCase
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview


@Suppress("kotlin:S107")
@ExperimentalCoroutinesApi
@FlowPreview
@Module
@InstallIn(ViewModelComponent::class)
object EmailVerificationModule {
    @Provides
    @ViewModelScoped
    fun provideChangeLoginEmailUseCases(
        dadadaBase: DaDaDaBase,
        sessionManager: SessionManager,
        qaGetAllTokensForTestLoginService: QaGetAllTokensForTestLoginService,
        userSendLoginChangeToken: UserSendLoginChangeToken,
        userCompleteLoginChange: UserCompleteLoginChange,
        sessionCredentialsSaver: SessionCredentialsSaver,
        localKeyRepository: LocalKeyRepository,
        globalPreferencesManager: GlobalPreferencesManager,
        preferencesManager: PreferencesManager,
        dataSync: DataSync,
        lockManager: LockManager,
        securityHelper: SecurityHelper,
        sessionTrasher: SessionTrasher,
        cryptoObjectHelper: CryptoObjectHelper,
        userSecureStorageManager: UserSecureStorageManager,
        databaseProvider: DatabaseProvider,
        accountCreationSetup: AccountCreationSetup,
        logRepository: LogRepository,
        @IoCoroutineDispatcher ioCoroutineDispatcher: CoroutineDispatcher,
    ): MutableList<UseCase<EmailVerificationState.View, EmailVerificationEvent>> {
        return mutableListOf(
            ::logPageView
                .partially1(logRepository)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::checkDebugToken
                .partially1(sessionManager)
                .partially1(dadadaBase)
                .partially1(qaGetAllTokensForTestLoginService)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::resendEmailToken
                .partially1(sessionManager)
                .partially1(userSendLoginChangeToken)
                .partially1(ioCoroutineDispatcher)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::changeLoginEmail
                .partially1(sessionManager)
                .partially1(sessionCredentialsSaver)
                .partially1(sessionTrasher)
                .partially1(userCompleteLoginChange)
                .partially1(dataSync)
                .partially1(localKeyRepository)
                .partially1(globalPreferencesManager)
                .partially1(preferencesManager)
                .partially1(lockManager)
                .partially1(securityHelper)
                .partially1(cryptoObjectHelper)
                .partially1(userSecureStorageManager)
                .partially1(databaseProvider)
                .partially1(accountCreationSetup)
                .partially1(logRepository)
                .partially1(ioCoroutineDispatcher)
                .toFilteredUseCase(Strategy.CancelOnNewState),
        )
    }
}