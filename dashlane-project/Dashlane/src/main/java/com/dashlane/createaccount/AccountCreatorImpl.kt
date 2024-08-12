package com.dashlane.createaccount

import android.widget.EditText
import com.dashlane.user.UserAccountInfo
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.authentication.TermsOfService
import com.dashlane.authentication.create.AccountCreationRepository
import com.dashlane.authentication.create.AccountCreator
import com.dashlane.authentication.localkey.AuthenticationLocalKeyRepository
import com.dashlane.biometricrecovery.BiometricRecovery
import com.dashlane.core.KeyChainHelper
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
import com.dashlane.server.api.endpoints.AccountType
import com.dashlane.crypto.keys.AppKey
import com.dashlane.session.Session
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionInitializer
import com.dashlane.session.SessionResult
import com.dashlane.session.SessionTrasher
import com.dashlane.user.Username
import com.dashlane.crypto.keys.VaultKey
import com.dashlane.session.repository.LockRepository
import com.dashlane.storage.securestorage.UserSecureStorageManager
import com.dashlane.util.hardwaresecurity.BiometricAuthModule
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.xml.domain.SyncObject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AccountCreatorImpl @Inject constructor(
    @ApplicationCoroutineScope
    private val applicationCoroutineScope: CoroutineScope,
    @DefaultCoroutineDispatcher
    private val defaultCoroutineDispatcher: CoroutineDispatcher,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
    private val lockRepository: LockRepository,
    private val biometricAuthModule: BiometricAuthModule,
    private val accountCreationRepository: AccountCreationRepository,
    private val sessionInitializer: SessionInitializer,
    private val userPreferencesManager: UserPreferencesManager,
    private val accountCreationSetup: AccountCreationSetup,
    private val sessionTrasher: SessionTrasher,
    private val localNotificationCreator: LocalNotificationCreator,
    private val lockManager: LockManager,
    private val authenticationLocalKeyRepository: AuthenticationLocalKeyRepository,
    private val biometricRecovery: BiometricRecovery,
    private val daDaDa: DaDaDa,
    private val keyChainHelper: KeyChainHelper,
    private val userSecureStorageManager: UserSecureStorageManager,
    private val accountStatusRepository: AccountStatusRepository
) : AccountCreator {

    override suspend fun createAccount(
        username: String,
        password: ObfuscatedByteArray,
        accountType: UserAccountInfo.AccountType,
        termsState: AccountCreator.TermsState?,
        biometricEnabled: Boolean,
        resetMpEnabled: Boolean,
        pinCode: String?,
        country: String?
    ) {
        val createAccountAccountType = when (accountType) {
            UserAccountInfo.AccountType.InvisibleMasterPassword -> AccountType.INVISIBLEMASTERPASSWORD
            UserAccountInfo.AccountType.MasterPassword -> AccountType.MASTERPASSWORD
        }

        val result = accountCreationRepository.createAccount(
            login = username,
            passwordUtf8Bytes = password,
            accountType = createAccountAccountType,
            termsOfService = TermsOfService(
                conditions = termsState?.conditions,
                offers = termsState?.offers
            ),
            withRemoteKey = daDaDa.isCreateAccountWithRemoteKeyEnabled,
            withLegacyCrypto = daDaDa.isCreateAccountWithLegacyCryptoEnabled,
            country = country
        )

        createAccount(
            email = username,
            accessKey = result.accessKey,
            secretKey = result.secretKey,
            appKey = result.appKey,
            userSettings = result.settings,
            sharingPublicKey = result.sharingKeys.public,
            sharingPrivateKey = result.sharingKeys.private,
            accountReset = result.isAccountReset,
            origin = result.origin,
            remoteKey = result.remoteKey,
            biometricEnabled = biometricEnabled,
            resetMpEnabled = resetMpEnabled,
            deviceAnalyticsId = result.deviceAnalyticsId,
            userAnalyticsId = result.userAnalyticsId,
            cryptographyMarker = CryptographyMarker.Flexible.Defaults.argon2d,
            loginMode = LoginMode.MasterPassword(),
            accountType = accountType,
            pinCode = pinCode
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
            accountReset = result.isAccountReset,
            origin = result.origin,
            remoteKey = result.remoteKey,
            biometricEnabled = false,
            resetMpEnabled = false,
            deviceAnalyticsId = result.deviceAnalyticsId,
            userAnalyticsId = result.userAnalyticsId,
            cryptographyMarker = CryptographyMarker.Flexible.Defaults.noDerivation64,
            loginMode = LoginMode.Sso,
            accountType = UserAccountInfo.AccountType.MasterPassword
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

    @SuppressWarnings("kotlin:S107") 
    private suspend fun createAccount(
        email: String,
        accessKey: String,
        secretKey: String,
        appKey: AppKey,
        userSettings: SyncObject.Settings,
        sharingPublicKey: SharingKeys.Public,
        sharingPrivateKey: SharingKeys.Private,
        accountReset: Boolean,
        origin: String?,
        remoteKey: VaultKey.RemoteKey?,
        biometricEnabled: Boolean,
        resetMpEnabled: Boolean,
        userAnalyticsId: String,
        deviceAnalyticsId: String,
        cryptographyMarker: CryptographyMarker,
        loginMode: LoginMode,
        accountType: UserAccountInfo.AccountType,
        pinCode: String? = null
    ) {
        val username = Username.ofEmail(email)
        if (accountReset) {
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
            loginMode = loginMode,
            accountType = accountType
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
                    biometricEnabled,
                    resetMpEnabled,
                    appKey,
                    pinCode
                )
            }

            is SessionResult.Error -> throw AccountCreator.CannotInitializeSessionException(
                sessionResult.cause
            )
        }
    }

    private suspend fun finishAccountCreation(
        session: Session,
        origin: String?,
        biometricEnabled: Boolean,
        resetMpEnabled: Boolean,
        appKey: AppKey,
        pinCode: String?
    ) {
        val username = session.userId
        runCatching {
            
            accountStatusRepository.refreshFor(session)
            accountCreationSetup.setupCreatedAccount(username = username, userOrigin = origin)
        }

        localNotificationCreator.registerAccountCreation()

        lockManager.unlock(LockPass.ofPassword(appKey))
        if (pinCode != null) {
            enablePinUnlock(session, pinCode)
        }
        if (biometricEnabled) {
            applicationCoroutineScope.launch(defaultCoroutineDispatcher) {
                enableBiometric(session)
                if (resetMpEnabled) {
                    enableResetMp()
                }
            }
        }
    }

    private suspend fun enableBiometric(session: Session) =
        withContext(defaultCoroutineDispatcher) {
            
            runCatching {
                sessionCredentialsSaver.saveCredentials(session)
                
                val result =
                    biometricAuthModule.createEncryptionKeyForBiometrics(username = session.userId)
                if (!result) return@withContext
            }
            
            lockRepository.getLockManager(session).setLockType(LockTypeManager.LOCK_TYPE_BIOMETRIC)
        }

    private suspend fun enablePinUnlock(session: Session, pinCode: String) =
        withContext(defaultCoroutineDispatcher) {
            
            keyChainHelper.initializeKeyStoreIfNeeded(session.userId)
            lockRepository.getLockManager(session).setLockType(LockTypeManager.LOCK_TYPE_PIN_CODE)

            userPreferencesManager.putBoolean(
                ConstantsPrefs.HOME_PAGE_GETTING_STARTED_PIN_IGNORE,
                true
            )

            userSecureStorageManager.storePin(session.localKey, session.username, pinCode)
            sessionCredentialsSaver.saveCredentials(session)
        }

    private fun enableResetMp() {
        
        biometricRecovery.isFeatureKnown = true
        biometricRecovery.setBiometricRecoveryFeatureEnabled(true)
    }
}
