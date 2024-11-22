package com.dashlane.session.restorer

import com.dashlane.account.UserAccountStorage
import com.dashlane.async.SyncBroadcastManager
import com.dashlane.authentication.DataLossTrackingLogger
import com.dashlane.authentication.LoginDataReset
import com.dashlane.authentication.accountsmanager.AccountsManager
import com.dashlane.authentication.localkey.AuthenticationLocalKeyRepository
import com.dashlane.authentication.login.AuthenticationDeviceRepository
import com.dashlane.common.logger.DeveloperInfoLogger
import com.dashlane.common.logger.DeveloperLogAction
import com.dashlane.common.logger.DeveloperLogMessage
import com.dashlane.crypto.keys.AppKey
import com.dashlane.crypto.keys.LocalKey
import com.dashlane.cryptography.decodeBase64ToByteArray
import com.dashlane.cryptography.decodeBase64ToByteArrayOrNull
import com.dashlane.cryptography.decodeUtf8ToString
import com.dashlane.cryptography.encodeUtf8ToByteArray
import com.dashlane.cryptography.encodeUtf8ToObfuscated
import com.dashlane.cryptography.toObfuscated
import com.dashlane.cryptography.use
import com.dashlane.device.DeviceInfoRepository
import com.dashlane.hardwaresecurity.CryptoObjectHelper
import com.dashlane.lock.LockSelfChecker
import com.dashlane.login.LoginMode
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.PreferencesManager
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
    private val developerInfoLogger: DeveloperInfoLogger
) : SessionRestorer {
    private val _job = Job()

    override val job: Job
        get() = _job

    override fun canRestoreSession(
        user: String,
        serverKey: String?,
        acceptLoggedOut: Boolean
    ): Boolean {
        val username = Username.ofEmailOrNull(user) ?: return false
        return getSavedCredentials(username, serverKey, acceptLoggedOut)?.use { true } ?: false
    }

    override suspend fun restore(username: Username?) {
        if (username == null) {
            
            _job.complete()
            return
        }
        val accessKey = getAccessKeyForRestore(username) ?: run {
            _job.complete()
            return
        }
        try {
            
            
            coroutineScope {
                launch {
                    checkLoggedInUserValidity(username, accessKey)
                }
                restoreSession(username, serverKey = null, acceptLoggedOut = false)
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
    }

    private fun getAccessKeyForRestore(username: Username): String? {
        val accessKey = preferencesManager[username].accessKey
        return if (accessKey == null) {
            
            if (secureStorageManager.isKeyDataStored(username, SecureDataKey.SECRET_KEY)) {
                    "Restore global device id to use as access key for $username",
                    logToUserSupportFile = true
                )
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
        acceptLoggedOut: Boolean
    ) {
        val applicationPassword = getSavedCredentials(username, serverKey, acceptLoggedOut)
        if (applicationPassword == null) {
            _job.complete()
            return
        }
        val result = runCatching {
            authenticationLocalKeyRepository.validateForLocal(username, applicationPassword)
        }.onFailure {
                "Restoring session: Stored MP incorrect can not access local key repository",
                logToUserSupportFile = true
            )
            syncBroadcastManager.removePasswordBroadcastIntent()
            _job.complete()
            return
        }.getOrThrow()

        when (
            sessionManager.loadSession(
                username,
                applicationPassword,
                result.secretKey,
                result.localKey,
                loginMode = LoginMode.SessionRestore
            )
        ) {
            is SessionResult.Error -> {
                    "Restoring session: Stored MP incorrect or session initialization failed",
                    logToUserSupportFile = true
                )
                syncBroadcastManager.removePasswordBroadcastIntent()
            }

            is SessionResult.Success -> {
            }
        }
        _job.complete()
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

    private fun getSavedCredentials(
        username: Username,
        serverKey: String?,
        acceptLoggedOut: Boolean = false
    ): AppKey? {
        if (!acceptLoggedOut && globalPreferencesManager.isUserLoggedOut) {
            return null
        }

        
        migrateAccountManager(username)

        val secureDataStorage = secureStorageManager.getSecureDataStorage(username, SecureDataStorage.Type.ANDROID_KEYSTORE_PROTECTED)
        val bytes = secureDataStorage.readLegacy(SecureDataKey.LOCAL_KEY)?.value?.decodeBase64ToByteArrayOrNull() ?: return null

        val localKeyBytes = try {
            cryptoObjectHelper.decrypt(CryptoObjectHelper.LocalKeyLock(username.email), bytes)
        } catch (exception: AEADBadTagException) {
            null
        } catch (exception: IllegalBlockSizeException) {
            null
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

        
        
        developerInfoLogger.log(
            username = username,
            action = DeveloperLogAction.ACCOUNT_MANAGER_MIGRATION,
            message = DeveloperLogMessage.SUCCESS
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
