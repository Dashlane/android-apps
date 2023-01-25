package com.dashlane.session

import com.dashlane.account.UserAccountStorage
import com.dashlane.async.BroadcastManager
import com.dashlane.authentication.accountsmanager.AccountsManager
import com.dashlane.authentication.localkey.AuthenticationLocalKeyRepository
import com.dashlane.authentication.login.AuthenticationDeviceRepository
import com.dashlane.cryptography.decodeBase64ToByteArray
import com.dashlane.cryptography.decodeBase64ToByteArrayOrNull
import com.dashlane.cryptography.decodeUtf8ToString
import com.dashlane.cryptography.encodeUtf8ToByteArray
import com.dashlane.cryptography.encodeUtf8ToObfuscated
import com.dashlane.cryptography.toObfuscated
import com.dashlane.cryptography.use
import com.dashlane.device.DeviceInfoRepository
import com.dashlane.login.LoginDataReset
import com.dashlane.login.LoginMode
import com.dashlane.login.lock.LockManager
import com.dashlane.login.sso.MigrationToSsoMemberInfo
import com.dashlane.login.sso.toMigrationToSsoMemberInfo
import com.dashlane.performancelogger.TimeToLoadLocalLogger
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.storage.securestorage.SecureDataKey
import com.dashlane.storage.securestorage.SecureDataStorage
import com.dashlane.storage.securestorage.SecureStorageManager
import com.dashlane.usersupportreporter.UserSupportFileLogger
import com.dashlane.util.hardwaresecurity.CryptoObjectHelper
import com.dashlane.util.installlogs.DataLossTrackingLogger
import com.dashlane.util.logD
import com.dashlane.util.logE
import dagger.Lazy
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton



@Singleton
class SessionRestorer @Inject constructor(
    private val sessionManager: SessionManager,
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val userAccountStorage: UserAccountStorage,
    private val userSupportFileLogger: UserSupportFileLogger,
    private val deviceRepository: AuthenticationDeviceRepository,
    private val secureStorageManager: SecureStorageManager,
    private val userPreferencesManagerLazy: Lazy<UserPreferencesManager>,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val dataReset: LoginDataReset,
    private val lockManager: LockManager,
    private val authenticationLocalKeyRepository: AuthenticationLocalKeyRepository,
    private val performanceLocalLogger: TimeToLoadLocalLogger,
    private val accountsManager: AccountsManager,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
    private val cryptoObjectHelper: CryptoObjectHelper
) {
    private val _job = Job()

    

    val job: Job
        get() = _job

    var restoredSessionMigrationToSsoMemberInfo: MigrationToSsoMemberInfo? = null

    

    fun canRestoreSession(
        user: String,
        serverKey: String?,
        acceptLoggedOut: Boolean = false
    ): Boolean {
        val username = Username.ofEmailOrNull(user) ?: return false
        return getSavedCredentials(username, serverKey, acceptLoggedOut)?.use { true } ?: false
    }

    suspend fun restore(username: Username?) {
        performanceLocalLogger.logStart()
        if (username == null) {
            
            performanceLocalLogger.clear()
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
            performanceLocalLogger.clear()
        } finally {
            
            _job.complete()
        }
    }

    private fun getAccessKeyForRestore(username: Username): String? {
        val accessKey = userPreferencesManagerLazy.get().preferencesFor(username).accessKey
        return if (accessKey == null) {
            
            if (secureStorageManager.isKeyDataStored(username, SecureDataKey.SECRET_KEY)) {
                userSupportFileLogger.add("Restore global device id to use as access key for $username")
                deviceInfoRepository.deviceId
            } else {
                userSupportFileLogger.add("Nothing to restore for $username")
                null
            }
        } else {
            userSupportFileLogger.add("Restore API access key for $username")
            accessKey
        }
    }

    

    suspend fun restoreSession(
        username: Username,
        serverKey: String?,
        acceptLoggedOut: Boolean = false
    ) {
        val applicationPassword = getSavedCredentials(username, serverKey, acceptLoggedOut)
        if (applicationPassword == null) {
            userSupportFileLogger.add("Restoring session: Credential Result error")
            performanceLocalLogger.clear()
            _job.complete()
            return
        }

        logD(tag = TAG) { "Retrieved credentials, trying to initialize" }

        val result = runCatching {
            authenticationLocalKeyRepository.validateForLocal(username, applicationPassword)
        }.onFailure {
            logE(tag = TAG) { "Restoring session: Stored MP incorrect can not access local key repository, logout!" }
            userSupportFileLogger.add("Restoring session: Stored MP incorrect can not access local key repository")
            BroadcastManager.removeAllBufferedIntent()
            _job.complete()
            return
        }.getOrThrow()

        when (sessionManager.loadSession(
            username,
            applicationPassword,
            result.secretKey,
            result.localKey,
            loginMode = LoginMode.SessionRestore
        )) {
            is SessionResult.Error -> {
                logE(tag = TAG) { "Password is not ok or session initialization failed, logout!" }
                userSupportFileLogger.add("Restoring session: Stored MP incorrect or session initialization failed")
                BroadcastManager.removeAllBufferedIntent()
                performanceLocalLogger.clear()
            }
            is SessionResult.Success -> {
                userSupportFileLogger.add("Restoring session: Session successfully restored")
            }
        }
        _job.complete()
    }

    private suspend fun checkLoggedInUserValidity(username: Username, accessKey: String) {
        
        val accessKeyStatus = deviceRepository.getAccessKeyStatus(username.email, accessKey)
        when (accessKeyStatus) {
            AuthenticationDeviceRepository.AccessKeyStatus.Revoked -> {
                userSupportFileLogger.add("Restored revoked device access key")
                dataReset.clearData(username, DataLossTrackingLogger.Reason.PASSWORD_OK_UKI_INVALID)
                throw RevokedDeviceException()
            }
            AuthenticationDeviceRepository.AccessKeyStatus.Invalid -> {
                userSupportFileLogger.add("Restored invalid device access key")
                dataReset.clearData(username, DataLossTrackingLogger.Reason.ACCESS_KEY_UNKNOWN)
                throw RevokedDeviceException()
            }
            is AuthenticationDeviceRepository.AccessKeyStatus.Valid -> {
                restoredSessionMigrationToSsoMemberInfo =
                    accessKeyStatus.ssoInfo?.toMigrationToSsoMemberInfo(username.email)
            }
        }
    }

    private fun getSavedCredentials(
        username: Username,
        serverKey: String?,
        acceptLoggedOut: Boolean = false
    ): AppKey? {
        if (!acceptLoggedOut && globalPreferencesManager.isUserLoggedOut) {
            userSupportFileLogger.add("Session can't be restored because user is logged out")
            return null
        }

        
        migrateAccountManager(username)

        val secureDataStorage = secureStorageManager.getSecureDataStorage(username, SecureDataStorage.Type.ANDROID_KEYSTORE_PROTECTED)
        val bytes = secureDataStorage.read(SecureDataKey.LOCAL_KEY)?.value?.decodeBase64ToByteArrayOrNull() ?: return null
        val localKeyBytes = cryptoObjectHelper.decrypt(CryptoObjectHelper.LocalKeyLock(username.email), bytes)

        if (localKeyBytes == null) {
            logE(TAG) { "LocalKey was null, aborting restoration procedure" }
            userSupportFileLogger.add("Session can't be restored because LocalKey was null")
            lockManager.markCredentialsEmpty()
            return null
        }

        val (masterPassword, key) = localKeyBytes
            .use(::LocalKey)
            .use {
                val masterPassword = secureStorageManager.getKeyData(SecureDataKey.MASTER_PASSWORD, username, it)?.use(ByteArray::toObfuscated)
                val key = serverKey ?: secureStorageManager.getServerKey(username, it)
                Pair(masterPassword, key)
            }

        val userAccountInfo = userAccountStorage[username]
        if (userAccountInfo?.otp2 == true && key == null) {
            
            userSupportFileLogger.add("Session can't be restored because OTP serverKey is missing")
            return null
        }

        if (masterPassword == null) {
            logE(TAG) { "masterPassword is null " + "credential, aborting restoration procedure" }
            userSupportFileLogger.add("Session can't be restored because masterPassword is missing")
            return null
        }

        return if (userAccountInfo?.sso == true) {
            try {
                AppKey.SsoKey(masterPassword)
            } catch (e: IllegalArgumentException) {
                userSupportFileLogger.add("Session can't be restored because SSO key is invalid")
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
    }

    companion object {
        private const val TAG = "SessionRestorer"
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