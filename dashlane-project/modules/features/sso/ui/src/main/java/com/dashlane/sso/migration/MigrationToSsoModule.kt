package com.dashlane.sso.migration

import arrow.core.partially1
import com.dashlane.account.UserAccountStorage
import com.dashlane.accountrecoverykey.AccountRecoveryKeyRepository
import com.dashlane.accountrecoverykey.AccountRecoveryKeySettingStateRefresher
import com.dashlane.authentication.SsoServerKeyFactory
import com.dashlane.authentication.login.AuthenticationAuthTicketHelper
import com.dashlane.changemasterpassword.MasterPasswordChanger
import com.dashlane.hermes.LogRepository
import com.dashlane.lock.LockManager
import com.dashlane.preference.PreferencesManager
import com.dashlane.server.api.endpoints.account.ChangeAccountTypeService
import com.dashlane.session.SessionManager
import com.dashlane.udf.Strategy
import com.dashlane.udf.UseCase
import com.dashlane.udf.toFilteredUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
@Module
@InstallIn(ViewModelComponent::class)
object MigrationToSsoModule {
    @Provides
    @ViewModelScoped
    
    @SuppressWarnings("kotlin:S107")
    fun provideMigrationToSsoUseCases(
        sessionManager: SessionManager,
        authTicketHelper: AuthenticationAuthTicketHelper,
        ssoServerKeyFactory: SsoServerKeyFactory,
        masterPasswordChanger: MasterPasswordChanger,
        preferencesManager: PreferencesManager,
        accountRecoveryKeyRepository: AccountRecoveryKeyRepository,
        accountRecoveryKeySettingStateRefresher: AccountRecoveryKeySettingStateRefresher,
        lockManager: LockManager,
        logRepository: LogRepository,
        userAccountStorage: UserAccountStorage,
        changeAccountTypeService: ChangeAccountTypeService,
    ): MutableList<UseCase<MigrationToSsoState.View, MigrationToSsoEvent>> {
        return mutableListOf(
            ::startSsoLogin
                .partially1(logRepository)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::migrateToSso
                .partially1(sessionManager)
                .partially1(authTicketHelper)
                .partially1(ssoServerKeyFactory)
                .partially1(masterPasswordChanger)
                .partially1(preferencesManager)
                .partially1(accountRecoveryKeyRepository)
                .partially1(accountRecoveryKeySettingStateRefresher)
                .partially1(lockManager)
                .partially1(userAccountStorage)
                .partially1(changeAccountTypeService)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::logout
                .partially1(sessionManager)
                .toFilteredUseCase(Strategy.CancelOnNewState),
        )
    }
}