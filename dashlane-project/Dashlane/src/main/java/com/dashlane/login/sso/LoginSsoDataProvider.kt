package com.dashlane.login.sso

import android.content.Context
import android.content.Intent
import com.dashlane.account.UserAccountInfo
import com.dashlane.account.UserAccountStorage
import com.dashlane.account.UserSecuritySettings
import com.dashlane.authentication.login.AuthenticationSsoRepository
import com.dashlane.authentication.login.AuthenticationSsoRepository.ValidateResult
import com.dashlane.core.DataSync
import com.dashlane.core.premium.PremiumStatusManager
import com.dashlane.createaccount.AccountCreator
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.login.LoginMode
import com.dashlane.login.LoginNewUserInitialization
import com.dashlane.login.LoginSuccessIntentFactory
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockPass
import com.dashlane.login.lock.LockTypeManager
import com.dashlane.login.lock.OnboardingApplicationLockActivity
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionResult
import com.dashlane.session.Username
import com.dashlane.util.clearTask
import com.dashlane.util.generateUniqueIdentifier
import com.skocken.presentation.provider.BaseDataProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LoginSsoDataProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ssoRepository: AuthenticationSsoRepository,
    private val initialization: LoginNewUserInitialization,
    private val userPreferencesManager: UserPreferencesManager,
    private val lockManager: LockManager,
    private val successIntentFactory: LoginSuccessIntentFactory,
    private val sessionManager: SessionManager,
    private val userAccountStorage: UserAccountStorage,
    private val premiumStatusManager: PremiumStatusManager,
    private val accountCreator: AccountCreator,
    private val dataSync: DataSync,
    intent: Intent
) : BaseDataProvider<LoginSsoContract.Presenter>(),
    LoginSsoContract.DataProvider {

    private val migrateToMasterPasswordUser = intent.getBooleanExtra(LoginSsoActivity.KEY_MIGRATE_TO_MASTER_PASSWORD_USER, false)

    override suspend fun login(
        login: String,
        ssoToken: String,
        serviceProviderKey: String
    ): Intent {
        userAccountStorage.saveSecuritySettings(login, UserSecuritySettings(isSso = true))

        val result = ssoRepository.validate(
            login = login,
            ssoToken = ssoToken,
            serviceProviderKey = serviceProviderKey
        )

        return when (result) {
            is ValidateResult.Remote -> handleValidateRemoteSuccess(result)
            is ValidateResult.Local -> handleValidateLocalSuccess(result)
        }
    }

    override suspend fun createAccount(
        login: String,
        ssoToken: String,
        serviceProviderKey: String,
        termsState: AccountCreator.TermsState
    ): Intent {
        userAccountStorage.saveSecuritySettings(login, UserSecuritySettings(isSso = true))

        accountCreator.createAccountSso(
            username = login,
            ssoToken = ssoToken,
            serviceProviderKey = serviceProviderKey,
            termsState = termsState
        )

        return OnboardingApplicationLockActivity.newIntent(
            context,
            successIntentFactory.createApplicationHomeIntent()
        ).clearTask()
    }

    private suspend fun handleValidateRemoteSuccess(result: ValidateResult.Remote): Intent {
        val settings = result.settings.run {
            if (anonymousUserId == null) {
                
                copy { anonymousUserId = generateUniqueIdentifier() }
            } else {
                this
            }
        }

        val sessionResult = initialization.initializeSession(
            username = result.login,
            accessKey = result.accessKey,
            secretKey = result.secretKey,
            localKey = result.localKey,
            appKey = result.ssoKey,
            remoteKey = result.remoteKey,
            userSettings = settings,
            sharingPublicKey = result.sharingKeys?.public?.value,
            sharingPrivateKey = result.sharingKeys?.private?.value,
            userAnalyticsId = result.userAnalyticsId,
            deviceAnalyticsId = result.deviceAnalyticsId,
            loginMode = LoginMode.Sso,
            accountType = UserAccountInfo.AccountType.MasterPassword
        )

        userPreferencesManager.userSettingsBackupTimeMillis = result.settingsDate.toEpochMilli()

        if (sessionResult is SessionResult.Error) {
            throw LoginSsoContract.CannotStartSessionException("Session can't be created", sessionResult.cause)
        }

        tryRefreshPremiumStatus(sessionResult)

        lockManager.unlock(LockPass.ofPassword(result.ssoKey))

        return createMigrationToMasterPasswordUserIntent(result.authTicket)
            ?: if (lockManager.getLockType() == LockTypeManager.LOCK_TYPE_MASTER_PASSWORD) {
                OnboardingApplicationLockActivity.newIntent(
                    context,
                    successIntentFactory.createLoginSyncProgressIntent()
                ).clearTask()
            } else {
                successIntentFactory.createLoginSyncProgressIntent()
            }
    }

    private suspend fun handleValidateLocalSuccess(result: ValidateResult.Local): Intent {
        val sessionResult = sessionManager.loadSession(
            username = Username.ofEmail(result.login),
            appKey = result.ssoKey,
            secretKey = result.secretKey,
            localKey = result.localKey,
            loginMode = LoginMode.Sso
        )

        if (sessionResult is SessionResult.Error) {
            throw LoginSsoContract.CannotStartSessionException(
                "Failed to load session ${sessionResult.errorCode} ${sessionResult.errorReason}",
                sessionResult.cause
            )
        }

        tryRefreshPremiumStatus(sessionResult)

        val shouldLaunchInitialSync = userPreferencesManager.getInt(ConstantsPrefs.TIMESTAMP_LABEL, 0) == 0

        val intent = createMigrationToMasterPasswordUserIntent(result.authTicket) ?: if (shouldLaunchInitialSync) {
            successIntentFactory.createLoginSyncProgressIntent()
        } else {
            dataSync.sync(Trigger.LOGIN)
            successIntentFactory.createApplicationHomeIntent()
        }

        lockManager.unlock(LockPass.ofPassword(result.ssoKey))

        return intent
    }

    private fun createMigrationToMasterPasswordUserIntent(authTicket: String) = if (migrateToMasterPasswordUser) {
        val shouldLaunchInitialSync = userPreferencesManager.getInt(ConstantsPrefs.TIMESTAMP_LABEL, 0) == 0

        val successIntent = if (shouldLaunchInitialSync) {
            successIntentFactory.createLoginSyncProgressIntent()
        } else {
            successIntentFactory.createApplicationHomeIntent()
        }.let {
            if (lockManager.getLockType() == LockTypeManager.LOCK_TYPE_MASTER_PASSWORD) {
                OnboardingApplicationLockActivity.newIntent(context, it)
                    .clearTask()
            } else {
                it
            }
        }

        successIntentFactory.createMigrationToMasterPasswordUserIntent(authTicket, successIntent)
    } else {
        null
    }

    private suspend fun tryRefreshPremiumStatus(sessionResult: SessionResult) {
        if (sessionResult !is SessionResult.Success) return

        runCatching { premiumStatusManager.refreshPremiumStatus(sessionResult.session) }
    }
}