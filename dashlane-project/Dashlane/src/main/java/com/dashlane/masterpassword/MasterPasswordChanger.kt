package com.dashlane.masterpassword

import com.dashlane.account.UserAccountInfo
import com.dashlane.account.UserAccountStorage
import com.dashlane.account.UserSecuritySettings
import com.dashlane.activatetotp.ActivateTotpServerKeyChanger
import com.dashlane.authentication.localkey.AuthenticationLocalKeyRepository
import com.dashlane.cryptography.CryptographyKeyGenerator
import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.cryptography.encodeUtf8ToByteArray
import com.dashlane.cryptography.encodeUtf8ToObfuscated
import com.dashlane.cryptography.toCryptographyMarkerOrNull
import com.dashlane.cryptography.use
import com.dashlane.debug.DaDaDa
import com.dashlane.exception.NotLoggedInException
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.login.LoginMode
import com.dashlane.network.tools.authorization
import com.dashlane.server.api.endpoints.sync.MasterPasswordUploadService
import com.dashlane.session.AppKey
import com.dashlane.session.Session
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.session.UserDataRepository
import com.dashlane.session.Username
import com.dashlane.session.VaultKey
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.session.repository.getCryptographyMarkerOrDefault
import com.dashlane.session.serverKeyUtf8Bytes
import com.dashlane.storage.securestorage.LocalKeyRepository
import com.dashlane.storage.securestorage.SecureDataKey
import com.dashlane.storage.securestorage.SecureDataStorage
import com.dashlane.storage.securestorage.SecureStorageManager
import com.dashlane.sync.DataSync
import com.dashlane.sync.cryptochanger.SyncCryptoChanger
import com.dashlane.sync.vault.SyncVault
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.xml.domain.SyncObject
import dagger.Lazy
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

interface MasterPasswordChanger : ActivateTotpServerKeyChanger {
    val canChangeMasterPassword: Boolean

    val job: Job?

    val progressStateFlow: StateFlow<Progress>

    suspend fun updateMasterPassword(
        newPassword: ObfuscatedByteArray,
        uploadReason: MasterPasswordUploadService.Request.UploadReason?
    ): Boolean

    suspend fun migrateToMasterPasswordUser(
        newPassword: ObfuscatedByteArray,
        authTicket: String
    ): Boolean

    suspend fun migrateToSsoMember(
        ssoKey: AppKey.SsoKey,
        authTicket: String,
        ssoServerKey: ByteArray
    ): Boolean

    suspend fun reset()

    sealed class Progress {

        object Initializing : Progress()

        object Downloading : Progress()

        class Ciphering(val index: Int, val total: Int) : Progress()

        object Uploading : Progress()

        object Confirmation : Progress()

        sealed class Completed : Progress() {
            data class Error(val progress: Progress? = null, val error: Exception) : Completed()

            object Success : Completed()
        }
    }

    class SyncFailedException : Exception()

    class NotLocalKeyMigratedException : Exception()
}

@Singleton
class MasterPasswordChangerImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val dataSync: Lazy<DataSync>,
    private val userDataRepository: UserDataRepository,
    private val userDatabaseRepository: UserDatabaseRepository,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
    private val localKeyRepository: LocalKeyRepository,
    private val authenticationLocalKeyRepository: AuthenticationLocalKeyRepository,
    private val userAccountStorage: UserAccountStorage,
    private val secureStorageManager: SecureStorageManager,
    private val cryptographyKeyGenerator: CryptographyKeyGenerator,
    private val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>,
    private val dadada: DaDaDa,
    private val syncVault: Lazy<SyncVault>,
    private val syncCryptoChanger: SyncCryptoChanger
) : MasterPasswordChanger {

    private val teamSpaceAccessor: TeamSpaceAccessor?
        get() = teamSpaceAccessorProvider.get()

    private val teamspaceForcedPayload
        get() = teamSpaceAccessor?.cryptoForcedPayload
            ?.toCryptographyMarkerOrNull()

    override val canChangeMasterPassword: Boolean
        get() {
            val session = sessionManager.session ?: return false
            
            val accountType = sessionManager.session?.username?.let { username -> userAccountStorage[username]?.accountType }
            return accountType != UserAccountInfo.AccountType.InvisibleMasterPassword &&
                userDatabaseRepository.isRacletteDatabaseAccessible(session)
        }

    override var job: Job? = null
    override val progressStateFlow =
        MutableStateFlow<MasterPasswordChanger.Progress>(MasterPasswordChanger.Progress.Initializing)

    override suspend fun reset() {
        job?.cancelAndJoin()
        job = null
        progressStateFlow.value = MasterPasswordChanger.Progress.Initializing
    }

    override suspend fun updateMasterPassword(
        newPassword: ObfuscatedByteArray,
        uploadReason: MasterPasswordUploadService.Request.UploadReason?
    ): Boolean {
        
        
        
        val session = sessionManager.session
        if (session == null) {
            progressStateFlow.value =
                MasterPasswordChanger.Progress.Completed.Error(error = NotLoggedInException())
            return false
        }
        val newAppKey =
            session.appKey.use {
                AppKey.Password(
                    newPassword,
                    it.serverKeyUtf8Bytes
                )
            } 
        val newVaultKey = if (session.remoteKey == null) newAppKey.toVaultKey() else cryptographyKeyGenerator.generateRemoteKey()
        val newUserKeys = Session.UserKeys(newAppKey, newVaultKey)
        val securityUpdate = SecurityUpdate(
            newUserKeys,
            newCryptoMarker = teamspaceForcedPayload 
        )
        return changeMasterPassword(securityUpdate, uploadReason)
    }

    override suspend fun migrateToMasterPasswordUser(
        newPassword: ObfuscatedByteArray,
        authTicket: String
    ): Boolean {
        val newAppKey = AppKey.Password(newPassword)
        val newVaultKey =
            if (dadada.isCreateAccountWithRemoteKeyEnabled) cryptographyKeyGenerator.generateRemoteKey() else newAppKey.toVaultKey()
        val newUserKeys = Session.UserKeys(newAppKey, newVaultKey)
        val newUserSecuritySettings = UserSecuritySettings(isToken = true)
        val newCryptoMarker =
            teamspaceForcedPayload ?: CryptographyMarker.Flexible.Defaults.argon2d
        val securityUpdate = SecurityUpdate(
            newUserKeys,
            newUserSecuritySettings,
            newCryptoMarker = newCryptoMarker,
            authTicket = authTicket
        )
        return changeMasterPassword(securityUpdate)
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    override suspend fun migrateToSsoMember(
        ssoKey: AppKey.SsoKey,
        authTicket: String,
        ssoServerKey: ByteArray
    ): Boolean {
        val remoteKey = cryptographyKeyGenerator.generateRemoteKey()
        val newUserSecuritySettings = UserSecuritySettings(isSso = true)
        val newUserKeys = Session.UserKeys(ssoKey, remoteKey)
        val securityUpdate = SecurityUpdate(
            newUserKeys,
            newUserSecuritySettings,
            newCryptoMarker = CryptographyMarker.Flexible.Defaults.noDerivation64,
            authTicket = authTicket,
            ssoServerKey = ssoServerKey
        )
        return changeMasterPassword(securityUpdate)
    }

    override suspend fun updateServerKey(newServerKey: String?, authTicket: String): Boolean {
        val session = sessionManager.session
        if (session == null || session.appKey !is AppKey.Password) {
            progressStateFlow.value =
                MasterPasswordChanger.Progress.Completed.Error(error = NotLoggedInException())
            return false
        }

        val newAppKey = AppKey.Password(
            passwordUtf8Bytes = (session.appKey as AppKey.Password).passwordUtf8Bytes,
            serverKeyUtf8Bytes = newServerKey?.encodeUtf8ToObfuscated()
        )

        val newVaultKey =
            if (session.remoteKey == null) newAppKey.toVaultKey() else cryptographyKeyGenerator.generateRemoteKey()

        val newUserKeys = Session.UserKeys(
            app = newAppKey,
            vault = newVaultKey
        )

        val newUserSecuritySettings = if (newServerKey != null) {
            UserSecuritySettings(isTotp = true, isOtp2 = true)
        } else {
            UserSecuritySettings(isToken = true)
        }

        val securityUpdate = SecurityUpdate(
            newUserKeys,
            newUserSecuritySettings,
            newCryptoMarker = teamspaceForcedPayload,
            authTicket = authTicket
        )
        return changeMasterPassword(securityUpdate).also {
            if (newServerKey == null) {
                sessionCredentialsSaver.removeServerKey(session.username)
            } else {
                sessionCredentialsSaver.saveServerKey(
                    newServerKey.encodeUtf8ToByteArray(),
                    session.localKey,
                    session.username
                )
            }
        }
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    private suspend fun changeMasterPassword(
        securityUpdate: SecurityUpdate,
        uploadReason: MasterPasswordUploadService.Request.UploadReason? = null
    ): Boolean {
        if (!canChangeMasterPassword) {
            progressStateFlow.value =
                MasterPasswordChanger.Progress.Completed.Error(error = MasterPasswordChanger.NotLocalKeyMigratedException())
            return false
        }
        val session = sessionManager.session
        if (session == null) {
            progressStateFlow.value =
                MasterPasswordChanger.Progress.Completed.Error(error = NotLoggedInException())
            return false
        }

        
        if (!awaitSyncDone()) {
            progressStateFlow.value =
                MasterPasswordChanger.Progress.Completed.Error(error = MasterPasswordChanger.SyncFailedException())
            return false
        }

        
        dataSync.get().markSyncNotAllowed()

        return try {
            val newSettings = withContext(Dispatchers.Default) {
                syncCryptoChanger.updateCryptography(
                    authorization = session.authorization,
                    userKeys = session.userKeys,
                    newUserKeys = securityUpdate.newUserKeys,
                    cryptographyMarker = securityUpdate.newCryptoMarker,
                    authTicket = securityUpdate.authTicket,
                    ssoServerKey = securityUpdate.ssoServerKey,
                    syncVault = syncVault.get(),
                    uploadReason = uploadReason
                ) {
                    progressStateFlow.value = when (it) {
                        SyncCryptoChanger.Progress.Downloading ->
                            MasterPasswordChanger.Progress.Downloading

                        is SyncCryptoChanger.Progress.Ciphering ->
                            it.toMasterPasswordChangerProgress()

                        SyncCryptoChanger.Progress.Uploading ->
                            MasterPasswordChanger.Progress.Uploading

                        is SyncCryptoChanger.Progress.Completed ->
                            MasterPasswordChanger.Progress.Confirmation
                    }
                }
            }

            completePasswordChangeLocally(session, newSettings, securityUpdate)
            progressStateFlow.value = MasterPasswordChanger.Progress.Completed.Success
            true
        } catch (e: Exception) {
            progressStateFlow.value =
                MasterPasswordChanger.Progress.Completed.Error(progressStateFlow.value, error = e)
            false
        } finally {
            dataSync.get().markSyncAllowed()
        }
    }

    private suspend fun completePasswordChangeLocally(
        session: Session,
        newSettings: SyncObject.Settings,
        securityUpdate: SecurityUpdate
    ) {
        withContext(NonCancellable) {
            
            updateSettings(session, newSettings)

            val username = session.username

            securityUpdate.newUserSecuritySettings?.let {
                userAccountStorage.saveSecuritySettings(
                    username,
                    it
                )
            }

            
            updateRemoteKey(securityUpdate.newRemoteKey, username, session)

            
            val newAppKey = securityUpdate.newAppKey
            localKeyRepository.updateLocalKey(
                username,
                session.appKey,
                newAppKey,
                newSettings.getCryptographyMarkerOrDefault(newAppKey.cryptographyKeyType)
            )

            withContext(Dispatchers.Default) {
                
                sessionCredentialsSaver.deleteSavedCredentials(session.username)

                
                syncCryptoChanger.reAuthorizeDevice(session.authorization)

                
                val localKeyResult = runCatching {
                    authenticationLocalKeyRepository.validateForLocal(username, newAppKey)
                }.onFailure {
                    throw IllegalStateException("AuthenticationLocalKeyRepository can not validate for local")
                }.getOrThrow()

                sessionManager.loadSession(
                    username = username,
                    appKey = newAppKey,
                    secretKey = localKeyResult.secretKey,
                    localKey = localKeyResult.localKey,
                    loginMode = LoginMode.MasterPasswordChanger
                )
                sessionManager.session?.let(sessionCredentialsSaver::saveCredentialsIfNecessary)
            }
        }
    }

    private fun updateSettings(session: Session, newSettings: SyncObject.Settings) {
        userDataRepository.getSettingsManager(session)
            .updateSettings(newSettings, triggerSync = false)
    }

    private fun updateRemoteKey(
        remoteKey: VaultKey.RemoteKey?,
        username: Username,
        session: Session
    ) {
        if (remoteKey == null) {
            val secureDataStorage = secureStorageManager.getSecureDataStorage(
                username,
                SecureDataStorage.Type.LOCAL_KEY_PROTECTED
            )
            secureStorageManager.removeKeyData(secureDataStorage, SecureDataKey.REMOTE_KEY)
        } else {
            val localKey =
                checkNotNull(session.appKey.use { localKeyRepository.getLocalKey(username, it) })
            remoteKey.cryptographyKeyBytes.use(ObfuscatedByteArray::toByteArray).use {
                secureStorageManager.storeKeyData(
                    it,
                    SecureDataKey.REMOTE_KEY,
                    username,
                    localKey
                )
            }
        }
    }

    private suspend fun awaitSyncDone(): Boolean {
        val dataSync = dataSync.get()
        return dataSync.awaitSync(Trigger.CHANGE_MASTER_PASSWORD)
    }

    @Suppress("UseDataClass")
    class SecurityUpdate(
        val newUserKeys: Session.UserKeys,
        val newUserSecuritySettings: UserSecuritySettings? = null,
        val newCryptoMarker: CryptographyMarker? = null,
        val authTicket: String? = null,
        val ssoServerKey: ByteArray? = null
    ) {
        val newAppKey: AppKey
            get() = newUserKeys.app
        private val newVaultKey: VaultKey
            get() = newUserKeys.vault
        val newRemoteKey: VaultKey.RemoteKey?
            get() = newVaultKey as? VaultKey.RemoteKey
    }
}

private fun CryptographyKeyGenerator.generateRemoteKey() =
    generateRaw64().use(VaultKey::RemoteKey)

private fun SyncCryptoChanger.Progress.Ciphering.toMasterPasswordChangerProgress() =
    MasterPasswordChanger.Progress.Ciphering(index, total)
