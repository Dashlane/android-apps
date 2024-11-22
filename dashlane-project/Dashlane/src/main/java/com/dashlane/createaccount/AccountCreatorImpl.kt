package com.dashlane.createaccount

import android.widget.EditText
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.authentication.TermsOfService
import com.dashlane.authentication.create.AccountCreationRepository
import com.dashlane.authentication.create.AccountCreator
import com.dashlane.authentication.localkey.AuthenticationLocalKeyRepository
import com.dashlane.biometricrecovery.BiometricRecovery
import com.dashlane.crypto.keys.AppKey
import com.dashlane.crypto.keys.VaultKey
import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.cryptography.SharingKeys
import com.dashlane.debug.DeveloperUtilities
import com.dashlane.hardwaresecurity.BiometricAuthModule
import com.dashlane.lock.LockEvent
import com.dashlane.debug.services.DaDaDaSecurity
import com.dashlane.lock.LockManager
import com.dashlane.lock.LockPass
import com.dashlane.lock.LockType
import com.dashlane.login.LoginMode
import com.dashlane.notification.LocalNotificationCreator
import com.dashlane.pin.PinSetupRepository
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.PreferencesManager
import com.dashlane.server.api.endpoints.AccountType
import com.dashlane.session.Session
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionInitializer
import com.dashlane.session.SessionResult
import com.dashlane.session.SessionTrasher
import com.dashlane.session.repository.LockRepository
import com.dashlane.user.UserAccountInfo
import com.dashlane.user.Username
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class AccountCreatorImpl @Inject constructor(
    @DefaultCoroutineDispatcher
    private val defaultCoroutineDispatcher: CoroutineDispatcher,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
    private val lockRepository: LockRepository,
    private val biometricAuthModule: BiometricAuthModule,
    private val accountCreationRepository: AccountCreationRepository,
    private val sessionInitializer: SessionInitializer,
    private val preferencesManager: PreferencesManager,
    private val accountCreationSetup: AccountCreationSetup,
    private val sessionTrasher: SessionTrasher,
    private val localNotificationCreator: LocalNotificationCreator,
    private val lockManager: LockManager,
    private val authenticationLocalKeyRepository: AuthenticationLocalKeyRepository,
    private val biometricRecovery: BiometricRecovery,
    private val dadadaSecurity: DaDaDaSecurity,
    private val accountStatusRepository: AccountStatusRepository,
    private val pinSetupRepository: PinSetupRepository,
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
            withRemoteKey = dadadaSecurity.isCreateAccountWithRemoteKeyEnabled,
            withLegacyCrypto = dadadaSecurity.isCreateAccountWithLegacyCryptoEnabled,
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
            sessionTrasher.trash(username = username, deletePreferences = true)
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

                preferencesManager[session.username].putBoolean(ConstantsPrefs.MIGRATION_15, true)

                finishAccountCreation(
                    session = session,
                    origin = origin,
                    biometricEnabled = biometricEnabled,
                    biometricRecoveryEnabled = resetMpEnabled,
                    appKey = appKey,
                    pinCode = pinCode
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
        biometricRecoveryEnabled: Boolean,
        appKey: AppKey,
        pinCode: String?
    ) {
        val username = session.userId
        runCatching {
            
            accountStatusRepository.refreshFor(session)
            accountCreationSetup.setupCreatedAccount(username = username, userOrigin = origin)
        }

        localNotificationCreator.registerAccountCreation()

        lockManager.unlock(session = session, pass = LockPass.ofPassword(appKey))
        runCatching {
            lockManager.sendUnlockEvent(
                LockEvent.Unlock(reason = LockEvent.Unlock.Reason.AppAccess, lockType = LockType.MasterPassword)
            )
        }
        if (pinCode != null) {
            enablePinUnlock(session, pinCode)
        }
        if (biometricEnabled) {
            enableBiometric(session)
            if (biometricRecoveryEnabled) {
                enableBiometricRecovery()
            }
        }
    }

    private suspend fun enableBiometric(session: Session) =
        withContext(defaultCoroutineDispatcher) {
            runCatching {
                
                val result =
                    biometricAuthModule.createEncryptionKeyForBiometrics(username = session.userId)
                if (!result) return@withContext
            }
            
            sessionCredentialsSaver.saveCredentials(session)
            lockRepository.getLockManager(session).addLock(session.username, LockType.Biometric)
        }

    private suspend fun enablePinUnlock(session: Session, pinCode: String) =
        withContext(defaultCoroutineDispatcher) {
            pinSetupRepository.savePinValue(session = session, pin = pinCode)
        }

    private fun enableBiometricRecovery() {
        
        biometricRecovery.isFeatureKnown = true
        biometricRecovery.setBiometricRecoveryFeatureEnabled(true)
    }
}
