package com.dashlane.session.restorer

import com.dashlane.account.UserAccountStorage
import com.dashlane.async.SyncBroadcastManager
import com.dashlane.authentication.DataLossTrackingLogger
import com.dashlane.authentication.LoginDataReset
import com.dashlane.authentication.accountsmanager.AccountsManager
import com.dashlane.authentication.localkey.AuthenticationLocalKeyRepository
import com.dashlane.authentication.login.AuthenticationDeviceRepository
import com.dashlane.common.logger.DeveloperLogAction
import com.dashlane.common.logger.DeveloperLogMessage
import com.dashlane.common.logger.Logs
import com.dashlane.crypto.keys.AppKey
import com.dashlane.crypto.keys.LocalKey
import com.dashlane.cryptography.decodeBase64ToByteArray
import com.dashlane.cryptography.decodeBase64ToByteArrayOrNull
import com.dashlane.cryptography.decodeUtf8ToString
import com.dashlane.cryptography.encodeUtf8ToByteArray
import com.dashlane.cryptography.encodeUtf8ToObfuscated
import com.dashlane.cryptography.toObfuscated
import com.dashlane.cryptography.use
import com.dashlane.data.login.LoginMode
import com.dashlane.device.DeviceInfoRepository
import com.dashlane.hardwaresecurity.CryptoObjectHelper
import com.dashlane.lock.LockSelfChecker
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.Session
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionRestorer
import com.dashlane.session.SessionResult
import com.dashlane.storage.securestorage.SecureDataKey
import com.dashlane.storage.securestorage.SecureDataStorage
import com.dashlane.storage.securestorage.SecureStorageManager
import com.dashlane.user.Username
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.crypto.AEADBadTagException
import javax.crypto.IllegalBlockSizeException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRestorerImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val userAccountStorage: UserAccountStorage,
    private val deviceRepository: AuthenticationDeviceRepository,
    private val secureStorageManager: SecureStorageManager,
    private val preferencesManager: PreferencesManager,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val dataReset: LoginDataReset,
    private val lockManager: LockSelfChecker,
    private val authenticationLocalKeyRepository: AuthenticationLocalKeyRepository,
    private val accountsManager: AccountsManager,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
    private val cryptoObjectHelper: CryptoObjectHelper,
    private val syncBroadcastManager: SyncBroadcastManager,
) : SessionRestorer {
    private val _job = Job()

    override val job: Job
        get() = _job

    override fun canRestoreSession(
        user: String,
        serverKey: String?,
        acceptLoggedOut: Boolean,
    ): Boolean {
        val username = Username.ofEmailOrNull(user) ?: return false
        return getSavedCredentials(username, serverKey, accessKey = null, acceptLoggedOut)?.use { true } ?: false
    }

    override suspend fun restore(username: Username?): Session? {
        if (username == null) {
            
            _job.complete()
            return null
        }
        val accessKey = getAccessKeyForRestore(username) ?: run {
            _job.complete()
            return null
        }
        var restoredSession: Session? = null
        try {
            
            
            coroutineScope {
                launch {
                    checkLoggedInUserValidity(username, accessKey)
                }
                restoredSession = restoreSession(
                    username = username,
                    serverKey = null,
                    accessKey = accessKey,
                    acceptLoggedOut = false,
                )
            }
        } catch (e: RevokedDeviceException) {
            
            _job.complete()
            
            val session = sessionManager.session
            if (session != null && session.username == username && session.accessKey == accessKey) {
                sessionManager.destroySession(session, byUser = false, forceLogout = true)
            }
        } finally {
            
            _job.complete()
        }
        return restoredSession
    }

    private fun getAccessKeyForRestore(username: Username): String? {
        val accessKey = preferencesManager[username].accessKey
        return if (accessKey == null) {
            
            if (secureStorageManager.isKeyDataStored(username, SecureDataKey.SECRET_KEY)) {
                deviceInfoRepository.deviceId
            } else {
                null
            }
        } else {
            accessKey
        }
    }

    override suspend fun restoreSession(
        username: Username,
        serverKey: String?,
        accessKey: String?,
        acceptLoggedOut: Boolean,
    ): Session? {
        val applicationPassword = getSavedCredentials(username, serverKey, accessKey, acceptLoggedOut)
        if (applicationPassword == null) {
            _job.complete()
            return null
        }
        val result = runCatching {
            authenticationLocalKeyRepository.validateForLocal(username, applicationPassword)
        }.onFailure {
                message = "Restoring session: Stored MP incorrect can not access local key repository",
                throwable = it,
                customLogs = listOf(
                    Logs.Kibana(
                        action = DeveloperLogAction.SESSION_ERROR,
                        username = username.email,
                        accessKey = accessKey,
                    ),
                ),
            )
            syncBroadcastManager.removePasswordBroadcastIntent()
            _job.complete()
            return null
        }.getOrThrow()

        val restoredSession = when (
            val result = sessionManager.loadSession(
                username = username,
                appKey = applicationPassword,
                secretKey = result.secretKey,
                localKey = result.localKey,
                loginMode = LoginMode.SessionRestore,
            )
        ) {
            is SessionResult.Error -> {
                syncBroadcastManager.removePasswordBroadcastIntent()
                null
            }

            is SessionResult.Success -> {
                result.session
            }
        }
        _job.complete()
        return restoredSession
    }

    private suspend fun checkLoggedInUserValidity(username: Username, accessKey: String) {
        
        when (deviceRepository.getAccessKeyStatus(username.email, accessKey)) {
            AuthenticationDeviceRepository.AccessKeyStatus.Revoked -> {
                dataReset.clearData(username, DataLossTrackingLogger.Reason.PASSWORD_OK_UKI_INVALID)
                throw RevokedDeviceException()
            }

            AuthenticationDeviceRepository.AccessKeyStatus.Invalid -> {
                dataReset.clearData(username, DataLossTrackingLogger.Reason.ACCESS_KEY_UNKNOWN)
                throw RevokedDeviceException()
            }

            is AuthenticationDeviceRepository.AccessKeyStatus.Valid -> Unit
        }
    }

    @Suppress("LongMethod")
    private fun getSavedCredentials(
        username: Username,
        serverKey: String?,
        accessKey: String?,
        acceptLoggedOut: Boolean = false,
    ): AppKey? {
        if (!acceptLoggedOut && globalPreferencesManager.isUserLoggedOut) {
            return null
        }

        
        migrateAccountManager(username)

        val secureDataStorage =
            secureStorageManager.getSecureDataStorage(username, SecureDataStorage.Type.ANDROID_KEYSTORE_PROTECTED)
        val bytes =
            secureDataStorage.readLegacy(SecureDataKey.LOCAL_KEY)?.value?.decodeBase64ToByteArrayOrNull() ?: return null

        val localKeyBytes = try {
            cryptoObjectHelper.decrypt(CryptoObjectHelper.LocalKeyLock(username.email), bytes)
        } catch (exception: Exception) {
            when (exception) {
                is AEADBadTagException,
                is IllegalBlockSizeException,
                -> {
                        message = "Session can't be restored because of ${exception::class.simpleName}",
                        throwable = exception,
                        customLogs = listOf(
                            Logs.Kibana(
                                action = DeveloperLogAction.SESSION_ERROR,
                                username = username.email,
                                accessKey = accessKey,
                            ),
                        ),
                    )
                    null
                }
                else -> throw exception
            }
        }
        if (localKeyBytes == null) {
            lockManager.markCredentialsEmpty()
            return null
        }

        val (masterPassword, key) = localKeyBytes
            .use(::LocalKey)
            .use {
                val masterPassword = secureStorageManager.getKeyData(SecureDataKey.MASTER_PASSWORD, username, it)
                    ?.use(ByteArray::toObfuscated)
                val key = serverKey ?: secureStorageManager.getServerKey(username, it)
                Pair(masterPassword, key)
            }

        val userAccountInfo = userAccountStorage[username]
        if (userAccountInfo?.otp2 == true && key == null) {
            
            return null
        }

        if (masterPassword == null) {
            return null
        }

        return if (userAccountInfo?.sso == true) {
            try {
                AppKey.SsoKey(masterPassword)
            } catch (e: IllegalArgumentException) {
                null
            }
        } else {
            AppKey.Password(masterPassword, key?.encodeUtf8ToObfuscated())
        }
    }

    private fun migrateAccountManager(username: Username) {
        val accountsManagerPassword = accountsManager.getPassword(username.email) ?: return
        if (!accountsManagerPassword.isLocalKey) return

        val localKeyBytes = accountsManagerPassword.data.decodeBase64ToByteArray()
        check(localKeyBytes.size == 32)

        localKeyBytes.use(::LocalKey).let { localKey ->
            sessionCredentialsSaver.saveLocalKey(localKey, username)
            accountsManagerPassword.serverKey?.let {
                sessionCredentialsSaver.saveServerKey(it.encodeUtf8ToByteArray(), localKey, username)
            }
        }

        accountsManager.clearAllAccounts()

        
        
            message = DeveloperLogMessage.SUCCESS.value,
            customLogs = listOf(
                Logs.Kibana(
                    action = DeveloperLogAction.ACCOUNT_MANAGER_MIGRATION,
                    username = username.email,
                ),
            ),
        )
    }

    private class RevokedDeviceException : Exception()
}

private fun SecureStorageManager.getServerKey(username: Username, localKey: LocalKey): String? {
    if (!isKeyDataStored(username, SecureDataKey.SERVER_KEY)) return null

    val serverKeyBytes = getKeyData(SecureDataKey.SERVER_KEY, username, localKey)
        
        ?: getKeyData(SecureDataKey.SERVER_KEY, username, LocalKey(ByteArray(32)))
            ?.also { data -> storeKeyData(data, SecureDataKey.SERVER_KEY, username, localKey) }

    return serverKeyBytes?.decodeUtf8ToString()
}