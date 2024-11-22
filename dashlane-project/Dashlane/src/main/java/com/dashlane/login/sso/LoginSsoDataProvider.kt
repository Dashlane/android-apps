package com.dashlane.login.sso

import android.content.Context
import android.content.Intent
import com.dashlane.account.UserAccountStorage
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.authentication.create.AccountCreator
import com.dashlane.authentication.login.AuthenticationSsoRepository
import com.dashlane.authentication.login.AuthenticationSsoRepository.ValidateResult
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.lock.LockManager
import com.dashlane.lock.LockPass
import com.dashlane.lock.LockType
import com.dashlane.login.LoginMode
import com.dashlane.login.LoginSuccessIntentFactory
import com.dashlane.login.lock.OnboardingApplicationLockActivity
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionInitializer
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionResult
import com.dashlane.sync.DataSync
import com.dashlane.user.UserAccountInfo
import com.dashlane.user.UserSecuritySettings
import com.dashlane.user.Username
import com.dashlane.util.clearTask
import com.dashlane.util.generateUniqueIdentifier
import com.skocken.presentation.provider.BaseDataProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LoginSsoDataProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ssoRepository: AuthenticationSsoRepository,
    private val sessionInitializer: SessionInitializer,
    private val preferencesManager: PreferencesManager,
    private val lockManager: LockManager,
    private val successIntentFactory: LoginSuccessIntentFactory,
    private val sessionManager: SessionManager,
    private val userAccountStorage: UserAccountStorage,
    private val accountCreator: AccountCreator,
    private val dataSync: DataSync,
    private val accountStatusRepository: AccountStatusRepository,
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
        val login = result.login
        val settings = result.settings.run {
            if (anonymousUserId == null) {
                
                copy { anonymousUserId = generateUniqueIdentifier() }
            } else {
                this
            }
        }

        val sessionResult = sessionInitializer.createSession(
            username = Username.ofEmail(login),
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

        preferencesManager[result.login].userSettingsBackupTimeMillis = result.settingsDate.toEpochMilli()

        when (sessionResult) {
            is SessionResult.Error -> throw LoginSsoContract.CannotStartSessionException("Session can't be created", sessionResult.cause)
            is SessionResult.Success -> {
                tryRefreshPremiumStatus(sessionResult)
                lockManager.unlock(session = sessionResult.session, pass = LockPass.ofPassword(result.ssoKey))

                return createMigrationToMasterPasswordUserIntent(login = login, result.authTicket)
                    ?: run {
                        val locks = lockManager.getLocks(sessionResult.session.username)
                        if (LockType.PinCode !in locks && LockType.Biometric !in locks) {
                            OnboardingApplicationLockActivity.newIntent(context, successIntentFactory.createLoginSyncProgressIntent()).clearTask()
                        } else {
                            successIntentFactory.createLoginSyncProgressIntent()
                        }
                    }
            }
        }
    }

    private suspend fun handleValidateLocalSuccess(result: ValidateResult.Local): Intent {
        val login = result.login
        val sessionResult = sessionManager.loadSession(
            username = Username.ofEmail(login),
            appKey = result.ssoKey,
            secretKey = result.secretKey,
            localKey = result.localKey,
            loginMode = LoginMode.Sso
        )

        when (sessionResult) {
            is SessionResult.Error -> {
                throw LoginSsoContract.CannotStartSessionException(
                    "Failed to load session ${sessionResult.errorCode} ${sessionResult.errorReason}",
                    sessionResult.cause
                )
            }
            is SessionResult.Success -> {
                tryRefreshPremiumStatus(sessionResult)

                val shouldLaunchInitialSync = preferencesManager[login].getInt(ConstantsPrefs.TIMESTAMP_LABEL, 0) == 0

                val intent = createMigrationToMasterPasswordUserIntent(login, result.authTicket) ?: if (shouldLaunchInitialSync) {
                    successIntentFactory.createLoginSyncProgressIntent()
                } else {
                    dataSync.sync(Trigger.LOGIN)
                    successIntentFactory.createApplicationHomeIntent()
                }

                lockManager.unlock(session = sessionResult.session, pass = LockPass.ofPassword(result.ssoKey))

                return intent
            }
        }
    }

    private fun createMigrationToMasterPasswordUserIntent(login: String, authTicket: String) = if (migrateToMasterPasswordUser) {
        val shouldLaunchInitialSync = preferencesManager[login].getInt(ConstantsPrefs.TIMESTAMP_LABEL, 0) == 0

        val successIntent = if (shouldLaunchInitialSync) {
            successIntentFactory.createLoginSyncProgressIntent()
        } else {
            successIntentFactory.createApplicationHomeIntent()
        }.let {
            val locks = lockManager.getLocks(Username.ofEmail(login))
            if (LockType.PinCode !in locks && LockType.Biometric !in locks) {
                OnboardingApplicationLockActivity.newIntent(context, it).clearTask()
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

        runCatching {
            sessionResult.session.let { session ->
                accountStatusRepository.refreshFor(session)
            }
        }
    }
}