package com.dashlane.createaccount

import android.widget.EditText
import com.dashlane.accountrecovery.AccountRecovery
import com.dashlane.analytics.referrer.ReferrerManager
import com.dashlane.authentication.TermsOfService
import com.dashlane.authentication.create.AccountCreationRepository
import com.dashlane.authentication.localkey.AuthenticationLocalKeyRepository
import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.cryptography.SharingKeys
import com.dashlane.debug.DaDaDa
import com.dashlane.debug.DeveloperUtilities
import com.dashlane.login.LoginMode
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockPass
import com.dashlane.login.lock.LockTypeManager
import com.dashlane.notification.LocalNotificationCreator
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.AppKey
import com.dashlane.session.Session
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionInitializer
import com.dashlane.session.SessionResult
import com.dashlane.session.SessionTrasher
import com.dashlane.session.Username
import com.dashlane.session.VaultKey
import com.dashlane.session.repository.LockRepository
import com.dashlane.useractivity.log.install.InstallLogCode17
import com.dashlane.useractivity.log.install.InstallLogRepository
import com.dashlane.usersupportreporter.UserSupportFileLogger
import com.dashlane.util.hardwaresecurity.BiometricAuthModule
import com.dashlane.util.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.util.inject.qualifiers.GlobalCoroutineScope
import com.dashlane.util.installlogs.DataLossTrackingLogger
import com.dashlane.xml.domain.SyncObject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject



class AccountCreatorImpl @Inject constructor(
    @GlobalCoroutineScope
    private val globalCoroutineScope: CoroutineScope,
    @DefaultCoroutineDispatcher
    private val defaultCoroutineDispatcher: CoroutineDispatcher,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
    private val lockRepository: LockRepository,
    private val biometricAuthModule: BiometricAuthModule,
    private val accountCreationRepository: AccountCreationRepository,
    private val sessionInitializer: SessionInitializer,
    private val userSupportFileLogger: UserSupportFileLogger,
    private val userPreferencesManager: UserPreferencesManager,
    private val accountCreationSetup: AccountCreationSetup,
    private val sessionTrasher: SessionTrasher,
    private val localNotificationCreator: LocalNotificationCreator,
    private val lockManager: LockManager,
    private val installLogRepository: InstallLogRepository,
    private val authenticationLocalKeyRepository: AuthenticationLocalKeyRepository,
    private val accountRecovery: AccountRecovery,
    private val daDaDa: DaDaDa
) : AccountCreator {

    override val isGdprDebugModeEnabled: Boolean
        get() = daDaDa.isGdprDebugModeEnabled

    override val isGdprForced: Boolean
        get() = daDaDa.isGdprForced

    

    override suspend fun createAccount(
        username: String,
        password: ObfuscatedByteArray,
        termsState: AccountCreator.TermsState?,
        biometricEnabled: Boolean,
        resetMpEnabled: Boolean
    ) {
        val result = accountCreationRepository.createAccount(
            username,
            password,
            TermsOfService(
                conditions = termsState?.conditions,
                offers = termsState?.offers
            ),
            daDaDa.isCreateAccountWithRemoteKeyEnabled,
            daDaDa.isCreateAccountWithLegacyCryptoEnabled
        )

        createAccount(
            email = username,
            accessKey = result.accessKey,
            secretKey = result.secretKey,
            appKey = result.appKey,
            userSettings = result.settings,
            sharingPublicKey = result.sharingKeys.public,
            sharingPrivateKey = result.sharingKeys.private,
            emailConsentsGiven = termsState?.offers,
            accountReset = result.isAccountReset,
            origin = result.origin,
            remoteKey = result.remoteKey,
            biometricEnabled = biometricEnabled,
            resetMpEnabled = resetMpEnabled,
            deviceAnalyticsId = result.deviceAnalyticsId,
            userAnalyticsId = result.userAnalyticsId,
            cryptographyMarker = CryptographyMarker.Flexible.Defaults.argon2d,
            loginMode = LoginMode.MasterPassword()
        )
    }

    override suspend fun createAccountSso(
        username: String,
        ssoToken: String,
        serviceProviderKey: String,
        termsState: AccountCreator.TermsState?
    ) {
        val result = accountCreationRepository.createSsoAccount(
            login = username,
            ssoToken = ssoToken,
            serviceProviderKey = serviceProviderKey,
            termsOfService = TermsOfService(
                conditions = termsState?.conditions,
                offers = termsState?.offers
            )
        )

        createAccount(
            email = username,
            accessKey = result.accessKey,
            secretKey = result.secretKey,
            appKey = result.appKey,
            userSettings = result.settings,
            sharingPublicKey = result.sharingKeys.public,
            sharingPrivateKey = result.sharingKeys.private,
            emailConsentsGiven = termsState?.offers,
            accountReset = result.isAccountReset,
            origin = result.origin,
            remoteKey = result.remoteKey,
            biometricEnabled = false,
            resetMpEnabled = false,
            deviceAnalyticsId = result.deviceAnalyticsId,
            userAnalyticsId = result.userAnalyticsId,
            cryptographyMarker = CryptographyMarker.Flexible.Defaults.noDerivation64,
            loginMode = LoginMode.Sso
        )
    }

    override fun preFillUsername(usernameField: EditText, suggestedEmail: String?) {
        if (usernameField.length() == 0) {
            
            if (suggestedEmail == null) {
                DeveloperUtilities.preFillUsername(usernameField)
            } else {
                usernameField.setText(suggestedEmail)
            }
        }
    }

    override fun preFillPassword(passwordField: EditText) =
        DeveloperUtilities.preFillPassword(passwordField)

    private suspend fun createAccount(
        email: String,
        accessKey: String,
        secretKey: String,
        appKey: AppKey,
        userSettings: SyncObject.Settings,
        sharingPublicKey: SharingKeys.Public,
        sharingPrivateKey: SharingKeys.Private,
        emailConsentsGiven: Boolean?,
        accountReset: Boolean,
        origin: String?,
        remoteKey: VaultKey.RemoteKey?,
        biometricEnabled: Boolean,
        resetMpEnabled: Boolean,
        userAnalyticsId: String,
        deviceAnalyticsId: String,
        cryptographyMarker: CryptographyMarker,
        loginMode: LoginMode
    ) {
        val username = Username.ofEmail(email)
        if (accountReset) {
            DataLossTrackingLogger(installLogRepository).log(DataLossTrackingLogger.Reason.CREATE_ACCOUNT_RESET)
            sessionTrasher.trash(username)
        }
        val localKeyResult =
            authenticationLocalKeyRepository.createForRemote(username, appKey, cryptographyMarker)
        val sessionResult = sessionInitializer.createSession(
            username = username,
            accessKey = accessKey,
            secretKey = secretKey,
            localKey = localKeyResult,
            userSettings = userSettings,
            sharingPublicKey = sharingPublicKey.value,
            sharingPrivateKey = sharingPrivateKey.value,
            appKey = appKey,
            remoteKey = remoteKey,
            deviceAnalyticsId = deviceAnalyticsId,
            userAnalyticsId = userAnalyticsId,
            loginMode = loginMode
        )
        when (sessionResult) {
            is SessionResult.Success -> {
                val session = sessionResult.session

                userPreferencesManager
                    .preferencesFor(session.username)
                    .putBoolean(ConstantsPrefs.MIGRATION_15, true)

                finishAccountCreation(
                    session,
                    origin,
                    emailConsentsGiven,
                    accountReset,
                    biometricEnabled,
                    resetMpEnabled,
                    appKey
                )
            }
            is SessionResult.Error -> throw AccountCreator.CannotInitializeSessionException(sessionResult.cause)
        }
    }

    private suspend fun finishAccountCreation(
        session: Session,
        origin: String?,
        emailConsentsGiven: Boolean?,
        isAccountReset: Boolean,
        biometricEnabled: Boolean,
        resetMpEnabled: Boolean,
        appKey: AppKey
    ) {
        val username = session.userId
        userSupportFileLogger.add("Account Creation: $username")
        runCatching {
            accountCreationSetup.setupCreatedAccount(username, isAccountReset, origin, emailConsentsGiven)
        }

        installLogRepository.enqueue(InstallLogCode17(subStep = "26"))
        ReferrerManager.getInstance().accountHasBeenCreated()
        localNotificationCreator.registerAccountCreation()

        lockManager.unlock(LockPass.ofPassword(appKey))
        if (biometricEnabled) {
            installLogRepository.enqueue(InstallLogCode17(subStep = "37"))
            globalCoroutineScope.launch(defaultCoroutineDispatcher) {
                enableBiometric(session)
                if (resetMpEnabled) {
                    enableResetMp()
                    installLogRepository.enqueue(InstallLogCode17(subStep = "37.1"))
                }
            }
        } else {
            installLogRepository.enqueue(InstallLogCode17(subStep = "38"))
        }
    }

    private suspend fun enableBiometric(session: Session) = withContext(defaultCoroutineDispatcher) {
        
        runCatching {
            sessionCredentialsSaver.saveCredentials(session)
            
            val result = biometricAuthModule.createEncryptionKeyForBiometrics(username = session.userId)
            if (!result) return@withContext
        }
        
        lockRepository.getLockManager(session).setLockType(LockTypeManager.LOCK_TYPE_BIOMETRIC)
    }

    private fun enableResetMp() {
        
        accountRecovery.isFeatureKnown = true
        accountRecovery.setFeatureEnabled(
            true,
            "accountCreation"
        )
    }
}
