package com.dashlane.login.pages.password

import android.content.Intent
import com.dashlane.accountrecoverykey.AccountRecoveryStatus
import com.dashlane.authentication.AuthenticationDeviceCredentialsInvalidException
import com.dashlane.authentication.AuthenticationEmptyPasswordException
import com.dashlane.authentication.AuthenticationException
import com.dashlane.authentication.AuthenticationInvalidPasswordException
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.authentication.SecurityFeature
import com.dashlane.authentication.login.AuthenticationPasswordRepository
import com.dashlane.biometricrecovery.BiometricRecovery
import com.dashlane.core.DataSync
import com.dashlane.cryptography.encodeUtf8ToObfuscated
import com.dashlane.help.HelpCenterLink
import com.dashlane.help.newIntent
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.hermes.generated.definitions.VerificationMode
import com.dashlane.inapplogin.InAppLoginManager
import com.dashlane.limitations.DeviceLimitActivityListener
import com.dashlane.limitations.Enforce2faLimiter
import com.dashlane.login.LoginDataReset
import com.dashlane.login.LoginMode
import com.dashlane.login.LoginNewUserInitialization
import com.dashlane.login.LoginStrategy
import com.dashlane.login.LoginStrategy.Strategy.DEVICE_LIMIT
import com.dashlane.login.LoginStrategy.Strategy.ENFORCE_2FA
import com.dashlane.login.LoginStrategy.Strategy.MONOBUCKET
import com.dashlane.login.LoginSuccessIntentFactory
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryKeyRepository
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockPass
import com.dashlane.login.pages.ChangeAccountHelper
import com.dashlane.login.pages.LoginBaseContract
import com.dashlane.login.pages.LoginLockBaseDataProvider
import com.dashlane.login.pages.password.LoginPasswordContract.InvalidPasswordException
import com.dashlane.login.pages.password.LoginPasswordContract.InvalidPasswordException.InvalidReason
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.AppKey
import com.dashlane.session.BySessionRepository
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionRestorer
import com.dashlane.session.SessionResult
import com.dashlane.session.Username
import com.dashlane.useractivity.log.usage.UsageLogConstant
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.util.hardwaresecurity.CryptoObjectHelper
import com.dashlane.util.installlogs.DataLossTrackingLogger
import com.dashlane.util.usagelogs.ViewLogger
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class LoginPasswordDataProvider @Inject constructor(
    private val successIntentFactory: LoginSuccessIntentFactory,
    private val dataReset: LoginDataReset,
    private val initialization: LoginNewUserInitialization,
    private val loggerFactory: LoginPasswordLogger.Factory,
    private val userPreferencesManager: UserPreferencesManager,
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val loginStrategy: LoginStrategy,
    private val biometricRecovery: BiometricRecovery,
    private val sessionManager: SessionManager,
    private val sessionRestorer: SessionRestorer,
    private val cryptoObjectHelper: CryptoObjectHelper,
    private val passwordRepository: AuthenticationPasswordRepository,
    private val changeAccountHelper: ChangeAccountHelper,
    private val deviceLimitActivityListener: DeviceLimitActivityListener,
    private val enforce2faLimiter: Enforce2faLimiter,
    private val dataSync: DataSync,
    private val loginAccountRecoveryKeyRepository: LoginAccountRecoveryKeyRepository,
    private val viewLogger: ViewLogger,
    lockManager: LockManager,
    inAppLoginManager: InAppLoginManager,
    bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
) : LoginLockBaseDataProvider<LoginPasswordContract.Presenter>(
    lockManager,
    successIntentFactory,
    inAppLoginManager,
    sessionManager,
    bySessionUsageLogRepository
),
    LoginPasswordContract.DataProvider {

    var registeredUserDevice: RegisteredUserDevice? = null
        set(value) {
            field = value
            registeredUserDevice?.let { logger = loggerFactory.create(it, it.toVerification()) }
        }

    var authTicket: String? = null

    private lateinit var logger: LoginPasswordLogger

    override val username: String
        get() = registeredUserDevice!!.login

    override val loginHistory: List<String> by lazy { globalPreferencesManager.getUserListHistory() }

    override val canMakeBiometricRecovery: Boolean
        get() = biometricRecovery.isSetUpForUser(username) &&
            sessionRestorer.canRestoreSession(
                username,
                registeredUserDevice!!.serverKey,
                acceptLoggedOut = true
            ) &&
            
            cryptoObjectHelper.getEncryptCipher(CryptoObjectHelper.BiometricsSeal(username)) is CryptoObjectHelper.CipherInitResult.Success

    override fun onPromptBiometricForRecovery() {
        biometricRecovery.logger.logPromptBiometricForRecovery(UsageLogConstant.ViewType.login)
        userPreferencesManager.isMpResetRecoveryStarted = true
    }

    override fun onGoToChangeMP() = biometricRecovery.logger.logGoToChangeMP(UsageLogConstant.ViewType.login)

    override fun loginHelp(): Intent {
        return HelpCenterLink.ARTICLE_CANNOT_LOGIN.newIntent(presenter.context!!, viewLogger, false)
    }

    override fun passwordForgotten(): Intent {
        return HelpCenterLink.ARTICLE_FORGOT_PASSWORD.newIntent(presenter.context!!, viewLogger, false)
    }

    override suspend fun changeAccount(email: String?): Intent {
        return changeAccountHelper.execute(email)
    }

    override suspend fun loadStaleSession() = sessionRestorer.restoreSession(
        Username.ofEmail(username),
        registeredUserDevice!!.serverKey,
        acceptLoggedOut = true
    )

    override fun unloadSession() {
        runBlocking {
            val currentSession = sessionManager.session
            if (currentSession != null) {
                sessionManager.destroySession(currentSession, byUser = false, forceLogout = false)
            }
        }
    }

    override fun getChangeMPIntent(): Intent? = if (biometricRecovery.isFeatureAvailable) {
        successIntentFactory.createBiometricRecoveryIntent()
    } else {
        null
    }

    override fun getAccountRecoveryKeyIntent(): Intent? {
        return registeredUserDevice?.let { successIntentFactory.createAccountRecoveryKeyIntent(it, authTicket) }
    }

    override suspend fun validatePassword(password: CharSequence, leaveAfterSuccess: Boolean):
        LoginPasswordContract.SuccessfulLogin {
        if (password.isEmpty()) {
            handleEmptyPassword()
        }
        val device = registeredUserDevice!!
        sessionManager.session
            ?.takeIf { it.userId == device.login }
            ?.let { return validateSessionPassword(AppKey.Password(password, device.serverKey)) }
        return try {
            val result = passwordRepository.validate(device, password.encodeUtf8ToObfuscated())
            if (result is AuthenticationPasswordRepository.Result.Remote) {
                
                registeredUserDevice = result.registeredUserDevice
            }
            handlePasswordSuccess(result, leaveAfterSuccess)
        } catch (e: AuthenticationException) {
            when (e) {
                is AuthenticationEmptyPasswordException -> handleEmptyPassword(e)
                is AuthenticationInvalidPasswordException -> handleInvalidPasswordError(e)
                is AuthenticationDeviceCredentialsInvalidException -> handleRemoteDeletion(e)
                else -> {
                    handleNetworkError(e)
                }
            }
        }
    }

    override suspend fun getAccountRecoveryKeyStatus(): AccountRecoveryStatus {
        return registeredUserDevice?.let {
            loginAccountRecoveryKeyRepository.getAccountRecoveryStatus(it)
                .getOrElse { throwable ->
                    AccountRecoveryStatus(false)
                }
        } ?: AccountRecoveryStatus(false)
    }

    override fun askMasterPasswordLater() {
        lockManager.resetLockoutTime()
    }

    private fun validateSessionPassword(
        appKey: AppKey
    ): LoginPasswordContract.SuccessfulLogin {
        if (lockManager.unlock(LockPass.ofPassword(appKey))) {
            usageLogUnlock()
            return LoginPasswordContract.SuccessfulLogin(null)
        } else {
            usageLogFailedUnlockAttempt()
            lockManager.addFailUnlockAttempt()
            throw InvalidPasswordException(InvalidReason.FAILED_UNLOCK)
        }
    }

    private suspend fun handlePasswordSuccess(
        result: AuthenticationPasswordRepository.Result,
        leaveAfterSuccess: Boolean
    ) =
        when (result) {
            is AuthenticationPasswordRepository.Result.Local -> handleLocalPasswordSuccess(
                result,
                leaveAfterSuccess
            )

            is AuthenticationPasswordRepository.Result.Remote -> handleRemotePasswordSuccess(result)
        }

    private suspend fun handleLocalPasswordSuccess(
        result: AuthenticationPasswordRepository.Result.Local,
        leaveAfterSuccess: Boolean
    ): LoginPasswordContract.SuccessfulLogin {
        val sessionResult = sessionManager.loadSession(
            Username.ofEmail(username),
            result.password,
            result.secretKey,
            result.localKey,
            result.accessKey.takeIf { result.isAccessKeyRefreshed },
            LoginMode.MasterPassword(verification = registeredUserDevice!!.toVerification())
        )
        if (sessionResult is SessionResult.Error) {
            throw IllegalStateException(
                "Failed to load session ${sessionResult.errorCode} ${sessionResult.errorReason}",
                sessionResult.cause
            )
        }
        val shouldLaunchInitialSync = userPreferencesManager.getInt(ConstantsPrefs.TIMESTAMP_LABEL, 0) == 0
        
        
        
        
        
        
        deviceLimitActivityListener.isFirstLogin = shouldLaunchInitialSync
        enforce2faLimiter.isFirstLogin = shouldLaunchInitialSync
        val intent = createMigrationToSsoMemberIntent() ?: when {
            
            shouldLaunchInitialSync -> createLocalStrategyIntent(sessionManager.session!!)
            leaveAfterSuccess -> null
            else -> successIntentFactory.createApplicationHomeIntent()
        }

        val successfulLogin = handleSuccessfulLogin(result, intent)

        if (!shouldLaunchInitialSync) {
            dataSync.sync(Trigger.LOGIN)
        }

        return successfulLogin
    }

    private suspend fun createLocalStrategyIntent(session: Session): Intent {
        val strategy = loginStrategy.getStrategy(session)
        return when {
            strategy == DEVICE_LIMIT -> successIntentFactory.createDeviceLimitIntent(loginStrategy.devices)
            strategy == MONOBUCKET && userPreferencesManager.ukiRequiresMonobucketConfirmation ->
                successIntentFactory.createMonobucketIntent(loginStrategy.monobucketHelper.getMonobucketOwner()!!)

            strategy == ENFORCE_2FA -> successIntentFactory.createEnforce2faLimitActivityIntent()
            else -> successIntentFactory.createLoginBiometricSetupIntent()
        }
    }

    private suspend fun handleRemotePasswordSuccess(result: AuthenticationPasswordRepository.Result.Remote): LoginPasswordContract.SuccessfulLogin {
        val accessKey = result.accessKey
        val secretKey = result.secretKey
        val sessionResult = initialization.initializeSession(
            username,
            result.password,
            accessKey,
            secretKey,
            result.localKey,
            result.settings,
            result.sharingKeys?.public?.value,
            result.sharingKeys?.private?.value,
            result.remoteKey,
            result.deviceAnalyticsId,
            result.userAnalyticsId,
            LoginMode.MasterPassword(verification = registeredUserDevice!!.toVerification())
        )

        
        
        
        
        
        
        
        userPreferencesManager.userSettingsBackupTimeMillis = result.settingsDate.toEpochMilli()

        return when (sessionResult) {
            is SessionResult.Success -> handleSessionSuccess(result, sessionResult.session)
            is SessionResult.Error -> throw sessionResult.cause ?: IllegalStateException("Session can't be created")
        }
    }

    private suspend fun handleSessionSuccess(
        result: AuthenticationPasswordRepository.Result.Remote,
        session: Session
    ): LoginPasswordContract.SuccessfulLogin {
        
        deviceLimitActivityListener.isFirstLogin = true
        enforce2faLimiter.isFirstLogin = true
        val strategy = loginStrategy.getStrategy(session, result.securityFeatures)
        val intent = createMigrationToSsoMemberIntent() ?: strategy.let {
            when (it) {
                DEVICE_LIMIT -> successIntentFactory.createDeviceLimitIntent(loginStrategy.devices)
                MONOBUCKET -> {
                    userPreferencesManager.ukiRequiresMonobucketConfirmation = true
                    successIntentFactory.createMonobucketIntent(loginStrategy.monobucketHelper.getMonobucketOwner()!!)
                }

                ENFORCE_2FA -> successIntentFactory.createEnforce2faLimitActivityIntent()
                else -> successIntentFactory.createLoginBiometricSetupIntent()
            }
        }
        return handleSuccessfulLogin(result, intent)
    }

    private fun handleSuccessfulLogin(
        credentials: AuthenticationPasswordRepository.Result,
        intent: Intent?
    ): LoginPasswordContract.SuccessfulLogin {
        val username = credentials.login
        lockManager.unlock(LockPass.ofPassword(credentials.password))
        return LoginPasswordContract.SuccessfulLogin(intent)
    }

    private fun handleEmptyPassword(cause: AuthenticationEmptyPasswordException? = null): Nothing {
        logger.logEmptyPassword()
        throw InvalidPasswordException(InvalidReason.EMPTY, cause)
    }

    private fun handleInvalidPasswordError(cause: AuthenticationInvalidPasswordException): Nothing {
        if (canMakeBiometricRecovery) {
            logger.logPasswordInvalidWithRecovery()
        } else {
            logger.logPasswordInvalid()
        }
        throw InvalidPasswordException(cause = cause)
    }

    private suspend fun handleRemoteDeletion(
        cause: AuthenticationDeviceCredentialsInvalidException
    ): Nothing {
        dataReset.clearData(
            Username.ofEmail(username),
            when {
                cause.isDataCorruption -> DataLossTrackingLogger.Reason.ACCESS_KEY_UNKNOWN
                cause.isValidPassword -> DataLossTrackingLogger.Reason.PASSWORD_OK_UKI_INVALID
                else -> DataLossTrackingLogger.Reason.PASSWORD_CHANGED
            }
        )
        throw LoginPasswordContract.AccountResetException(cause)
    }

    private fun handleNetworkError(e: AuthenticationException): Nothing {
        logger.logNetworkError(LoginPasswordLogger.NW_ERR_OFFLINE)
        throw LoginBaseContract.OfflineException(e)
    }

    private fun createMigrationToSsoMemberIntent() = migrationToSsoMemberInfoProvider?.invoke()?.run {
        successIntentFactory.createMigrationToSsoMemberIntent(
            login = login,
            serviceProviderUrl = serviceProviderUrl,
            isNitroProvider = isNitroProvider,
            totpAuthTicket = totpAuthTicket
        )
    }
}

private fun RegisteredUserDevice.toVerification() = when (this) {
    is RegisteredUserDevice.Local -> if (isServerKeyRequired) {
        VerificationMode.OTP2
    } else {
        VerificationMode.NONE
    }

    is RegisteredUserDevice.ToRestore -> VerificationMode.EMAIL_TOKEN
    is RegisteredUserDevice.Remote -> when {
        SecurityFeature.TOTP in securityFeatures -> VerificationMode.OTP1
        SecurityFeature.EMAIL_TOKEN in securityFeatures -> VerificationMode.EMAIL_TOKEN
        else -> VerificationMode.NONE
    }
}
