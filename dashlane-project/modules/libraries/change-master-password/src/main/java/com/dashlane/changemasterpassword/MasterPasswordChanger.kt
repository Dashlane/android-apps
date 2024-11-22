package com.dashlane.changemasterpassword

import androidx.annotation.VisibleForTesting
import com.dashlane.account.UserAccountStorage
import com.dashlane.authentication.getCryptographyMarkerOrDefault
import com.dashlane.authentication.localkey.AuthenticationLocalKeyRepository
import com.dashlane.crypto.keys.AppKey
import com.dashlane.crypto.keys.VaultKey
import com.dashlane.crypto.keys.serverKeyUtf8Bytes
import com.dashlane.cryptography.CryptographyKeyGenerator
import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.cryptography.encodeUtf8ToByteArray
import com.dashlane.cryptography.encodeUtf8ToObfuscated
import com.dashlane.cryptography.toCryptographyMarkerOrNull
import com.dashlane.cryptography.use
import com.dashlane.debug.services.DaDaDaSecurity
import com.dashlane.exception.NotLoggedInException
import com.dashlane.hardwaresecurity.SecurityHelper
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.lock.LockManager
import com.dashlane.lock.LockType
import com.dashlane.login.LoginMode
import com.dashlane.server.api.endpoints.sync.MasterPasswordUploadService
import com.dashlane.session.Session
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionResult
import com.dashlane.session.authorization
import com.dashlane.session.repository.UserDataRepository
import com.dashlane.storage.securestorage.LocalKeyRepository
import com.dashlane.storage.securestorage.SecureDataKey
import com.dashlane.storage.securestorage.SecureDataStorage
import com.dashlane.storage.securestorage.SecureStorageManager
import com.dashlane.sync.DataSync
import com.dashlane.sync.cryptochanger.SyncCryptoChanger
import com.dashlane.sync.vault.SyncVault
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.user.UserSecuritySettings
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.xml.domain.SyncObject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface MasterPasswordChanger {

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

    suspend fun updateServerKey(
        newServerKey: String?,
        authTicket: String
    ): Boolean

    sealed class Progress {

        data object Initializing : Progress()

        data object Downloading : Progress()

        class Ciphering(val index: Int, val total: Int) : Progress()

        data object Uploading : Progress()

        data object Confirmation : Progress()

        sealed class Completed : Progress() {
            data class Error(val progress: Progress? = null, val error: Exception) : Completed()

            data object Success : Completed()
        }
    }

    class SyncFailedException : Exception()

    class NotLocalKeyMigratedException : Exception()
}

@Singleton
class MasterPasswordChangerImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val dataSync: DataSync,
    private val userDataRepository: UserDataRepository,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
    private val localKeyRepository: LocalKeyRepository,
    private val authenticationLocalKeyRepository: AuthenticationLocalKeyRepository,
    private val userAccountStorage: UserAccountStorage,
    private val secureStorageManager: SecureStorageManager,
    private val cryptographyKeyGenerator: CryptographyKeyGenerator,
    private val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>,
    private val dadadaSecurity: DaDaDaSecurity,
    private val syncVault: SyncVault,
    private val syncCryptoChanger: SyncCryptoChanger,
    private val lockManager: LockManager,
    private val securityHelper: SecurityHelper,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher
) : MasterPasswordChanger {

    private val teamSpaceAccessor: TeamSpaceAccessor?
        get() = teamSpaceAccessorProvider.get()

    private val teamspaceForcedPayload
        get() = teamSpaceAccessor?.cryptoForcedPayload?.toCryptographyMarkerOrNull()

    override var job: Job? = null
    override val progressStateFlow = MutableStateFlow<MasterPasswordChanger.Progress>(MasterPasswordChanger.Progress.Initializing)

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
            progressStateFlow.value = MasterPasswordChanger.Progress.Completed.Error(error = NotLoggedInException())
            return false
        }
        
        val newAppKey = session.appKey.use { AppKey.Password(newPassword, it.serverKeyUtf8Bytes) }
        val newVaultKey = if (session.remoteKey == null) newAppKey.toVaultKey() else cryptographyKeyGenerator.generateRemoteKey()
        val securityUpdate = SecurityUpdate(
            newAppKey = newAppKey,
            newVaultKey = newVaultKey,
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
            if (dadadaSecurity.isCreateAccountWithRemoteKeyEnabled) cryptographyKeyGenerator.generateRemoteKey() else newAppKey.toVaultKey()
        val newUserSecuritySettings = UserSecuritySettings(isToken = true)
        val newCryptoMarker = teamspaceForcedPayload ?: CryptographyMarker.Flexible.Defaults.argon2d
        val securityUpdate = SecurityUpdate(
            newAppKey = newAppKey,
            newVaultKey = newVaultKey,
            newUserSecuritySettings = newUserSecuritySettings,
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
        val securityUpdate = SecurityUpdate(
            newAppKey = ssoKey,
            newVaultKey = remoteKey,
            newUserSecuritySettings = newUserSecuritySettings,
            newCryptoMarker = CryptographyMarker.Flexible.Defaults.noDerivation64,
            authTicket = authTicket,
            ssoServerKey = ssoServerKey
        )
        return changeMasterPassword(securityUpdate)
    }

    override suspend fun updateServerKey(newServerKey: String?, authTicket: String): Boolean {
        val session = sessionManager.session
        if (session == null || session.appKey !is AppKey.Password) {
            progressStateFlow.value = MasterPasswordChanger.Progress.Completed.Error(error = NotLoggedInException())
            return false
        }

        val newAppKey = AppKey.Password(
            passwordUtf8Bytes = (session.appKey as AppKey.Password).passwordUtf8Bytes,
            serverKeyUtf8Bytes = newServerKey?.encodeUtf8ToObfuscated()
        )

        val newVaultKey = if (session.remoteKey == null) newAppKey.toVaultKey() else cryptographyKeyGenerator.generateRemoteKey()

        val newUserSecuritySettings = if (newServerKey != null) {
            UserSecuritySettings(isTotp = true, isOtp2 = true)
        } else {
            UserSecuritySettings(isToken = true)
        }

        val securityUpdate = SecurityUpdate(
            newAppKey = newAppKey,
            newVaultKey = newVaultKey,
            newUserSecuritySettings = newUserSecuritySettings,
            newCryptoMarker = teamspaceForcedPayload,
            authTicket = authTicket
        )

        val result = changeMasterPassword(securityUpdate)

        if (result) {
            if (newServerKey == null) {
                sessionCredentialsSaver.removeServerKey(session.username)
            } else {
                sessionCredentialsSaver.saveServerKey(
                    serverKey = newServerKey.encodeUtf8ToByteArray(),
                    localKey = session.localKey,
                    username = session.username
                )
            }
        }

        return result
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    @VisibleForTesting
    suspend fun changeMasterPassword(
        securityUpdate: SecurityUpdate,
        uploadReason: MasterPasswordUploadService.Request.UploadReason? = null
    ): Boolean {
        val session = sessionManager.session
        if (session == null) {
            progressStateFlow.value = MasterPasswordChanger.Progress.Completed.Error(error = NotLoggedInException())
            return false
        }

        
        if (!dataSync.awaitSync(Trigger.CHANGE_MASTER_PASSWORD)) {
            progressStateFlow.value = MasterPasswordChanger.Progress.Completed.Error(error = MasterPasswordChanger.SyncFailedException())
            return false
        }

        
        dataSync.markSyncNotAllowed()

        return try {
            val newSettings = withContext(defaultDispatcher) {
                syncCryptoChanger.updateCryptography(
                    authorization = session.authorization,
                    appKey = session.appKey,
                    vaultKey = session.vaultKey,
                    newAppKey = securityUpdate.newAppKey,
                    newVaultKey = securityUpdate.newVaultKey,
                    cryptographyMarker = securityUpdate.newCryptoMarker,
                    authTicket = securityUpdate.authTicket,
                    ssoServerKey = securityUpdate.ssoServerKey,
                    syncVault = syncVault,
                    uploadReason = uploadReason
                ) {
                    progressStateFlow.value = when (it) {
                        SyncCryptoChanger.Progress.Downloading -> MasterPasswordChanger.Progress.Downloading
                        is SyncCryptoChanger.Progress.Ciphering -> it.toMasterPasswordChangerProgress()
                        SyncCryptoChanger.Progress.Uploading -> MasterPasswordChanger.Progress.Uploading
                        is SyncCryptoChanger.Progress.Completed -> MasterPasswordChanger.Progress.Confirmation
                    }
                }
            }

            completePasswordChangeLocally(session, newSettings, securityUpdate)
            progressStateFlow.value = MasterPasswordChanger.Progress.Completed.Success
            true
        } catch (e: Exception) {
            progressStateFlow.value = MasterPasswordChanger.Progress.Completed.Error(progressStateFlow.value, error = e)
            false
        } finally {
            dataSync.markSyncAllowed()
        }
    }

    @VisibleForTesting
    suspend fun completePasswordChangeLocally(
        session: Session,
        newSettings: SyncObject.Settings,
        securityUpdate: SecurityUpdate
    ) {
        withContext(NonCancellable) {
            
            userDataRepository.getSettingsManager(session).updateSettings(newSettings, triggerSync = false)

            val username = session.username

            securityUpdate.newUserSecuritySettings?.let { userAccountStorage.saveSecuritySettings(username, it) }

            
            updateRemoteKey(securityUpdate.newRemoteKey, session)

            
            val newAppKey = securityUpdate.newAppKey
            localKeyRepository.updateLocalKey(
                username = username,
                appKey = session.appKey,
                newAppKey = newAppKey,
                cryptographyMarker = newSettings.getCryptographyMarkerOrDefault(newAppKey.cryptographyKeyType)
            )

            withContext(defaultDispatcher) {
                
                sessionCredentialsSaver.deleteSavedCredentials(session.username)

                
                syncCryptoChanger.reAuthorizeDevice(session.authorization)

                
                val localKeyResult = runCatching {
                    authenticationLocalKeyRepository.validateForLocal(username, newAppKey)
                }.onFailure {
                    throw IllegalStateException("AuthenticationLocalKeyRepository can not validate for local")
                }.getOrThrow()

                val sessionResult = sessionManager.loadSession(
                    username = username,
                    appKey = newAppKey,
                    secretKey = localKeyResult.secretKey,
                    localKey = localKeyResult.localKey,
                    loginMode = LoginMode.MasterPasswordChanger
                )

                when (sessionResult) {
                    is SessionResult.Error -> {
                        throw IllegalStateException(
                            "Failed to load session ${sessionResult.errorCode} ${sessionResult.errorReason}",
                            sessionResult.cause
                        )
                    }
                    is SessionResult.Success -> {
                        val lockType = lockManager.getLocks(username)
                        if ((LockType.PinCode in lockType || LockType.Biometric in lockType) &&
                            securityHelper.isDeviceSecured(username)
                        ) {
                            sessionCredentialsSaver.saveCredentials(sessionResult.session)
                        }
                    }
                }
            }
        }
    }

    @VisibleForTesting
    fun updateRemoteKey(
        remoteKey: VaultKey.RemoteKey?,
        session: Session
    ) {
        if (remoteKey == null) {
            val secureDataStorage = secureStorageManager.getSecureDataStorage(session.username, SecureDataStorage.Type.LOCAL_KEY_PROTECTED)
            secureStorageManager.removeKeyData(secureDataStorage, SecureDataKey.REMOTE_KEY)
        } else {
            val localKey = checkNotNull(session.appKey.use { localKeyRepository.getLocalKey(session.username, it) })
            remoteKey.cryptographyKeyBytes.use(ObfuscatedByteArray::toByteArray).use {
                secureStorageManager.storeKeyData(
                    keyData = it,
                    keyIdentifier = SecureDataKey.REMOTE_KEY,
                    username = session.username,
                    localKey = localKey
                )
            }
        }
    }

    @Suppress("UseDataClass")
    data class SecurityUpdate(
        val newAppKey: AppKey,
        val newVaultKey: VaultKey,
        val newUserSecuritySettings: UserSecuritySettings? = null,
        val newCryptoMarker: CryptographyMarker? = null,
        val authTicket: String? = null,
        val ssoServerKey: ByteArray? = null
    ) {
        val newRemoteKey: VaultKey.RemoteKey?
            get() = newVaultKey as? VaultKey.RemoteKey
    }
}

fun CryptographyKeyGenerator.generateRemoteKey() = generateRaw64().use(VaultKey::RemoteKey)

private fun SyncCryptoChanger.Progress.Ciphering.toMasterPasswordChangerProgress() = MasterPasswordChanger.Progress.Ciphering(index, total)
