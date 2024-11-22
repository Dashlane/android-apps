package com.dashlane.session

import com.dashlane.abtesting.RemoteConfiguration
import com.dashlane.crashreport.CrashReporter
import com.dashlane.crypto.keys.AppKey
import com.dashlane.crypto.keys.LocalKey
import com.dashlane.crypto.keys.VaultKey
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.cryptography.use
import com.dashlane.device.DeviceInfoRepository
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.Status
import com.dashlane.hermes.generated.events.user.Login
import com.dashlane.login.LoginInfo
import com.dashlane.login.LoginMode
import com.dashlane.login.toMode
import com.dashlane.login.verification
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.repository.SessionCoroutineScopeRepository
import com.dashlane.session.repository.UserDataRepository
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.storage.securestorage.SecureDataKey
import com.dashlane.storage.securestorage.SecureStorageManager
import com.dashlane.storage.securestorage.UserSecureStorageManager
import com.dashlane.user.UserAccountInfo
import com.dashlane.user.Username
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.MainCoroutineDispatcher
import com.dashlane.xml.domain.SyncObject
import dagger.Lazy
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManagerImpl @Inject constructor(
    @ApplicationCoroutineScope
    private val applicationCoroutineScope: CoroutineScope,
    @MainCoroutineDispatcher
    private val mainCoroutineDispatcher: CoroutineDispatcher,
    private val userSecureStorageManager: Lazy<UserSecureStorageManager>,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val crashReporter: Lazy<CrashReporter>,
    private val sessionCoroutineScopeRepository: Lazy<SessionCoroutineScopeRepository>,
    private val userDataRepository: Lazy<UserDataRepository>,
    private val userDatabaseRepository: Lazy<UserDatabaseRepository>,
    private val sessionRestorer: Lazy<SessionRestorer>,
    private val preferencesManager: PreferencesManager,
    private val remoteConfigurations: Lazy<Set<RemoteConfiguration>>,
    private val logRepository: LogRepository,
    private val sessionCreator: SessionCreator,
) : SessionManager, SessionInitializer {

    
    private val observerList = mutableListOf<SessionObserver>()
    private var alreadyTriedToRestore = false
    private var _session: Session? = null

    
    override val session: Session?
        get() {
            
            if (_session == null && !alreadyTriedToRestore) {
                
                runBlocking { sessionRestorer.get().job.join() }
                alreadyTriedToRestore = true
                
            }
            return _session
        }

    override fun attach(observer: SessionObserver) {
        observerList.add(observer)
    }

    override fun detach(observer: SessionObserver) {
        observerList.remove(observer)
    }

    override suspend fun createSession(
        username: Username,
        accessKey: String,
        secretKey: String,
        localKey: LocalKey,
        appKey: AppKey,
        userSettings: SyncObject.Settings,
        sharingPublicKey: String?,
        sharingPrivateKey: String?,
        remoteKey: VaultKey.RemoteKey?,
        deviceAnalyticsId: String,
        userAnalyticsId: String,
        loginMode: LoginMode,
        accountType: UserAccountInfo.AccountType
    ): SessionResult {
        _session?.let {
            
            destroySession(it, false)
        }
        _session = try {
            sessionCreator.createSession(
                username = username,
                accessKey = accessKey,
                secretKey = secretKey,
                localKey = localKey,
                appKey = appKey,
                remoteKey = remoteKey,
            )
        } catch (e: InvalidRemoteKeyException) {
            logRepository.queueEvent(
                Login(mode = loginMode.toMode(), isBackupCode = false, verificationMode = loginMode.verification, status = Status.ERROR_UNKNOWN)
            )
            return SessionResult.Error(SessionResult.ErrorCode.ERROR_REMOTE_KEY, "No remote key available")
        }
        return finalizeSessionSetup(
            userSettings,
            allowOverwriteAccessKey = true,
            loginInfo = LoginInfo(
                isFirstLogin = true,
                loginMode = loginMode
            ),
            accountType = accountType
        ).also {
            if (it is SessionResult.Success) {
                val userPreferencesManager = preferencesManager[username]
                if (sharingPublicKey != null) {
                    userPreferencesManager.publicKey = sharingPublicKey
                }
                val secureStorageManager = userSecureStorageManager.get()
                val session = it.session
                if (sharingPrivateKey != null) {
                    secureStorageManager.storeRsaPrivateKey(session.localKey, session.username, sharingPrivateKey)
                }
                if (remoteKey != null) {
                    secureStorageManager.secureStorageManager.storeRemoteKey(username, localKey, remoteKey)
                }
                secureStorageManager.storeDeviceAnalyticsId(session.localKey, session.username, deviceAnalyticsId)
                secureStorageManager.storeUserAnalyticsId(session.localKey, session.username, userAnalyticsId)
            }
        }
    }

    override suspend fun loadSession(
        username: Username,
        appKey: AppKey,
        secretKey: String,
        localKey: LocalKey,
        accessKey: String?,
        loginMode: LoginMode
    ): SessionResult {
        _session?.let {
            
            destroySession(it, false)
        }
        val secureStorage = userSecureStorageManager.get()
        val userPreferencesManager = preferencesManager[username]
        val sessionAccessKey = accessKey
            ?: userPreferencesManager.accessKey
            ?: deviceInfoRepository.deviceId
            ?: return SessionResult.Error(
                SessionResult.ErrorCode.ERROR_SESSION_ACCESS_KEY,
                "No session access key available"
            )

        val accountType: UserAccountInfo.AccountType =
            userPreferencesManager.accountType
                ?.let { UserAccountInfo.AccountType.fromString(it) }
                ?: UserAccountInfo.AccountType.MasterPassword

        val remoteKey = secureStorage.secureStorageManager.getRemoteKey(username, localKey)
        _session = try {
            sessionCreator.createSession(
                username = username,
                accessKey = sessionAccessKey,
                secretKey = secretKey,
                localKey = localKey,
                appKey = appKey,
                remoteKey = remoteKey,
            )
        } catch (e: InvalidRemoteKeyException) {
            logRepository.queueEvent(
                Login(mode = loginMode.toMode(), isBackupCode = false, verificationMode = loginMode.verification, status = Status.ERROR_UNKNOWN)
            )
            return SessionResult.Error(SessionResult.ErrorCode.ERROR_REMOTE_KEY, "No remote key available")
        }
        return finalizeSessionSetup(
            userSettings = null,
            accountType = accountType,
            allowOverwriteAccessKey = accessKey != null,
            loginInfo = LoginInfo(isFirstLogin = false, loginMode = loginMode)
        )
    }

    override suspend fun destroySession(session: Session, byUser: Boolean, forceLogout: Boolean) {
            "Received Logout for ${session.userId}. Logout by user:$byUser",
            logToUserSupportFile = true
        )
        crashReporter.get()
            .addInformation("[SessionManager] Session destroyed. Logout by user:$byUser")
        
        notifySessionEnded(session, byUser, forceLogout)
        userDataRepository.get().sessionCleanup(session, forceLogout)
        userDatabaseRepository.get().sessionCleanup(session, forceLogout)
        _session = null
    }

    override fun detachAll() {
        
        observerList.toList().forEach {
            detach(it)
        }
    }

    private suspend fun initialize(
        session: Session,
        userSettings: SyncObject.Settings? = null,
        accountType: UserAccountInfo.AccountType,
        allowOverwriteAccessKey: Boolean,
        loginInfo: LoginInfo
    ): SessionResult {
        return try {
            sessionCoroutineScopeRepository.get().sessionInitializing(session)
            userDataRepository.get().sessionInitializing(session, userSettings, accountType, allowOverwriteAccessKey)

            remoteConfigurations.get().forEach {
                it.initAndRefresh(session.authorization)
            }

            
            userDatabaseRepository.get().sessionInitializing(session, loginInfo)

            SessionResult.Success(session)
        } catch (e: Exception) {
                "A session initializationObserver has failed, session can't be initialized",
                throwable = e
            )
            destroySession(session, false)
            SessionResult.Error(
                SessionResult.ErrorCode.ERROR_INIT,
                "A session initializationObserver has failed, session can't be initialized",
                e
            )
        }
    }

    private suspend fun finalizeSessionSetup(
        userSettings: SyncObject.Settings?,
        accountType: UserAccountInfo.AccountType,
        allowOverwriteAccessKey: Boolean,
        loginInfo: LoginInfo
    ): SessionResult {
        return when (
            val sessionInitializationResult =
                initialize(_session!!, userSettings, accountType, allowOverwriteAccessKey, loginInfo)
        ) {
            is SessionResult.Success -> {
                
                startSession(_session!!, loginInfo)
                sessionInitializationResult
            }

            else -> sessionInitializationResult
        }
    }

    private suspend fun startSession(session: Session, loginInfo: LoginInfo?) =
        applicationCoroutineScope.launch(mainCoroutineDispatcher) {
            
            try {
                notifySessionStarted(session, loginInfo)
            } catch (e: Exception) {
                destroySession(session, false)
            }
        }

    private suspend fun notifySessionEnded(session: Session, byUser: Boolean, forceLogout: Boolean) {
        observerList.toList().forEach {
            try {
                it.sessionEnded(session, byUser, forceLogout)
            } catch (throwable: Throwable) {
                
            }
        }
    }

    private suspend fun notifySessionStarted(session: Session, loginInfo: LoginInfo?) {
        observerList.toList().forEach {
            it.sessionStarted(session, loginInfo)
        }
    }
}

private fun SecureStorageManager.storeRemoteKey(
    username: Username,
    localKey: LocalKey,
    remoteKey: VaultKey.RemoteKey
) {
    remoteKey.cryptographyKeyBytes.use(ObfuscatedByteArray::toByteArray).use {
        storeKeyData(it, SecureDataKey.REMOTE_KEY, username, localKey)
    }
}

private fun SecureStorageManager.getRemoteKey(
    username: Username,
    localKey: LocalKey
): VaultKey.RemoteKey? = getKeyData(SecureDataKey.REMOTE_KEY, username, localKey)?.let(VaultKey::RemoteKey)
