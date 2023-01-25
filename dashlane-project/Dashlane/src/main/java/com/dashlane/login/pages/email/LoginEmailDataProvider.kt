package com.dashlane.login.pages.email

import android.content.Context
import com.dashlane.account.UserAccountStorageImpl
import com.dashlane.account.UserSecuritySettings
import com.dashlane.authentication.AuthenticationAccountNotFoundException
import com.dashlane.authentication.AuthenticationContactSsoAdministratorException
import com.dashlane.authentication.AuthenticationEmptyEmailException
import com.dashlane.authentication.AuthenticationException
import com.dashlane.authentication.AuthenticationExpiredVersionException
import com.dashlane.authentication.AuthenticationInvalidEmailException
import com.dashlane.authentication.AuthenticationNetworkException
import com.dashlane.authentication.AuthenticationOfflineException
import com.dashlane.authentication.AuthenticationSecondFactor
import com.dashlane.authentication.CipheredBackupToken
import com.dashlane.authentication.UnauthenticatedUser
import com.dashlane.authentication.login.AuthenticationEmailRepository
import com.dashlane.authentication.login.AuthenticationEmailRepository.Result.RequiresDeviceRegistration
import com.dashlane.authentication.login.AuthenticationEmailRepository.Result.RequiresPassword
import com.dashlane.authentication.login.AuthenticationEmailRepository.Result.RequiresServerKey
import com.dashlane.authentication.login.AuthenticationEmailRepository.Result.RequiresSso
import com.dashlane.authentication.login.SsoInfo
import com.dashlane.crashreport.CrashReporter
import com.dashlane.login.LoginSuccessIntentFactory
import com.dashlane.login.pages.LoginBaseContract
import com.dashlane.login.pages.LoginBaseDataProvider
import com.dashlane.login.sso.toMigrationToSsoMemberInfo
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.server.api.endpoints.account.AccountExistsService
import com.dashlane.usersupportreporter.UserSupportFileLogger
import com.dashlane.usersupportreporter.UserSupportFileUploader
import com.dashlane.util.stackTraceToSafeString
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject



class LoginEmailDataProvider @Inject constructor(
    private val userAccountStorage: UserAccountStorageImpl,
    private val logger: LoginEmailLogger,
    private val userSupportFileLogger: UserSupportFileLogger,
    private val userSupportFileUploader: UserSupportFileUploader,
    private val preferences: GlobalPreferencesManager,
    private val crashReporter: CrashReporter,
    private val emailRepository: AuthenticationEmailRepository,
    private val accountExistsService: AccountExistsService,
    private val successIntentFactory: LoginSuccessIntentFactory
) : LoginBaseDataProvider<LoginEmailContract.Presenter>(), LoginEmailContract.DataProvider {

    override val loginHistory: List<String> by lazy { preferences.getUserListHistory() }
    override val preferredLogin: String? by lazy { loginHistory.firstOrNull() }
    override val preFillLogin: Boolean by lazy { !preferences.isUserLoggedOut && !preferredLogin.isNullOrEmpty() }

    override fun uploadUserSupportFile(context: Context, coroutineScope: CoroutineScope) {
        userSupportFileUploader.startCrashLogsUpload(context, coroutineScope, crashReporter.crashReporterId)
    }

    override fun getTrackingInstallationId() = preferences.installationTrackingId

    private val userSecuritySettings by lazy { preferredLogin?.let { userAccountStorage[it]?.securitySettings } }

    private var autoFilledEmail = false

    override val username: String
        get() = preferredLogin?.takeIf { preFillLogin } ?: ""

    override fun onShow() {
        @LoginEmailLogger.LandState
        val state = when {
            preFillLogin -> LoginEmailLogger.LAND_PRE_FILL
            !preferredLogin.isNullOrEmpty() -> LoginEmailLogger.LAND_AUTO_FILL
            else -> LoginEmailLogger.LAND_FIRST_LOGIN
        }
        logger.logLand(state, userSecuritySettings)
    }

    override fun onBack() = logger.logBack()

    override fun onAutoFill() {
        logger.logAutoFill(userSecuritySettings)
        autoFilledEmail = true
    }

    override fun onClear() {
        if (autoFilledEmail) {
            logger.logClearedEmail(userSecuritySettings)
        }
        autoFilledEmail = false
    }

    override suspend fun executeLogin(email: String) {
        val backupToken = preferences.getCipheredBackupToken(email)
        val user = if (backupToken != null) {
            UnauthenticatedUser(
                email,
                CipheredBackupToken(backupToken, preferences.getBackupTokenDate(email))
            )
        } else {
            UnauthenticatedUser(email)
        }

        try {
            val userStatus = emailRepository.getUserStatus(user)

            userStatus.ssoInfo
                ?.toMigrationToSsoMemberInfo(email)
                ?.let(presenter::setMigrationToSsoMember)

            handleUserStatus(userStatus)
        } catch (e: AuthenticationAccountNotFoundException) {
            if (userIsSsoButRequiresAccountCreation(email)) {
                presenter.onCreateAccountClicked(skipEmailIfPrefilled = true)
            } else {
                handleUserStatusError(email, e)
            }
        } catch (e: AuthenticationException) {
            handleUserStatusError(email, e)
        }
    }

    

    private suspend fun userIsSsoButRequiresAccountCreation(email: String): Boolean {
        val login = try {
            AccountExistsService.Request.Login(email.lowercase(Locale.US))
        } catch (e: IllegalArgumentException) {
            throw AuthenticationInvalidEmailException(remoteCheck = false, cause = e)
        }
        val response = accountExistsService.execute(
            AccountExistsService.Request(
                login = login
            )
        )
        return response.data.sso
    }

    override fun accountCreation() {
        logger.logCreateAccountClick()
    }

    private fun handleUserStatus(userStatus: AuthenticationEmailRepository.Result) {
        when (userStatus) {
            is RequiresDeviceRegistration -> handleUserRequiresDeviceRegistration(userStatus)
            is RequiresServerKey -> handleUserStatusRequiresServerKey(userStatus)
            is RequiresPassword -> handleUserStatusRequiresPassword(userStatus)
            is RequiresSso -> handleUserStatusRequiresSso(userStatus)
        }
    }

    private fun handleUserRequiresDeviceRegistration(userStatus: RequiresDeviceRegistration) {
        when (userStatus) {
            is RequiresDeviceRegistration.SecondFactor -> handleUserRequiresDeviceRegistrationSecondFactor(userStatus)
            is RequiresDeviceRegistration.Sso -> handleUserRequiresDeviceRegistrationSso(userStatus)
        }
    }

    private fun handleUserRequiresDeviceRegistrationSecondFactor(userStatus: RequiresDeviceRegistration.SecondFactor) {
        val secondFactor = userStatus.secondFactor
        val login = secondFactor.login
        when (secondFactor) {
            is AuthenticationSecondFactor.EmailToken -> {
                val securitySettings = UserSecuritySettings(
                    isToken = true,
                    isAuthenticatorEnabled = secondFactor.isAuthenticatorEnabled
                )

                userAccountStorage.saveSecuritySettings(login, securitySettings)
                logger.logValidatedEmail(
                    registeredDevice = false,
                    inputType = getLoginInputType(login),
                    securitySettings = securitySettings
                )

                if (secondFactor.isAuthenticatorEnabled) {
                    presenter.showAuthenticatorPage(secondFactor)
                } else {
                    presenter.showTokenPage(secondFactor)
                }
            }
            is AuthenticationSecondFactor.Totp -> {
                val securitySettings = UserSecuritySettings(
                    isTotp = true,
                    isU2fEnabled = secondFactor.u2f != null,
                    isDuoEnabled = secondFactor.duoPush != null,
                    isAuthenticatorEnabled = secondFactor.isAuthenticatorEnabled
                )
                userAccountStorage.saveSecuritySettings(login, securitySettings)
                logger.logValidatedEmail(
                    registeredDevice = false,
                    inputType = getLoginInputType(login),
                    securitySettings = securitySettings
                )
                presenter.showOtpPage(secondFactor)
            }
        }
    }

    private fun handleUserRequiresDeviceRegistrationSso(userStatus: RequiresDeviceRegistration.Sso) {
        val login = userStatus.login
        logger.logValidatedEmail(
            registeredDevice = false,
            inputType = getLoginInputType(login),
            securitySettings = UserSecuritySettings(isSso = true)
        )
        startLoginSso(login, userStatus.ssoInfo)
    }

    private fun handleUserStatusRequiresServerKey(userStatus: RequiresServerKey) {
        val secondFactor = userStatus.secondFactor
        val login = secondFactor.login
        val securitySettings = UserSecuritySettings(
            isTotp = true,
            isOtp2 = true,
            isU2fEnabled = secondFactor.u2f != null,
            isDuoEnabled = secondFactor.duoPush != null
        )
        userAccountStorage.saveSecuritySettings(login, securitySettings)
        logger.logValidatedEmail(
            registeredDevice = false,
            inputType = getLoginInputType(secondFactor.login),
            securitySettings = securitySettings
        )
        presenter.showOtpPage(secondFactor)
    }

    private fun handleUserStatusRequiresPassword(userStatus: RequiresPassword) {
        val securitySettings = userAccountStorage[username]?.securitySettings
        logger.logValidatedEmail(
            registeredDevice = true,
            inputType = getLoginInputType(username),
            securitySettings = securitySettings
        )
        presenter.showPasswordStep(
            userStatus.registeredUserDevice
        )
    }

    private fun handleUserStatusRequiresSso(userStatus: RequiresSso) {
        logger.logValidatedEmail(
            registeredDevice = true,
            inputType = getLoginInputType(username),
            securitySettings = UserSecuritySettings(isSso = true)
        )
        startLoginSso(userStatus.login, userStatus.ssoInfo)
    }

    private fun handleUserStatusError(login: String, e: AuthenticationException) {
        userSupportFileLogger.add(e.stackTraceToSafeString())
        when (e) {
            is AuthenticationEmptyEmailException -> {
                logger.logEmptyEmail()
                throw LoginEmailContract.EmptyEmailException(e)
            }
            is AuthenticationInvalidEmailException -> {
                logger.logInvalidEmail()
                throw LoginEmailContract.InvalidEmailException(e)
            }
            is AuthenticationAccountNotFoundException -> {
                logger.logRejectedEmail(false)
                presenter.notifyEmailError()
            }
            is AuthenticationContactSsoAdministratorException -> {
                throw LoginEmailContract.ContactSsoAdministratorException(e)
            }
            is AuthenticationOfflineException -> {
                logger.logNetworkError(getLoginInputType(username), LoginEmailLogger.NW_ERR_OFFLINE)
                throw LoginBaseContract.OfflineException(e)
            }
            is AuthenticationNetworkException -> {
                logger.logNetworkError(
                    inputType = getLoginInputType(login),
                    error = when (e.endpoint) {
                        AuthenticationNetworkException.Endpoint.REGISTRATION -> LoginEmailLogger.NW_ERR_ACCOUNT_SETTINGS
                        AuthenticationNetworkException.Endpoint.LOGIN -> LoginEmailLogger.NW_ERR_CHECK_DELETION
                        else -> LoginEmailLogger.NW_ERR_OFFLINE
                    }
                )
                presenter.notifyNetworkError()
            }
            is AuthenticationExpiredVersionException -> {
                throw LoginBaseContract.ExpiredVersionException(e)
            }
            else -> {
                logger.logNetworkError(getLoginInputType(username), LoginEmailLogger.NW_ERR_OFFLINE)
                presenter.notifyUnknownError()
            }
        }
    }

    private fun startLoginSso(login: String, ssoInfo: SsoInfo) {
        presenter.startLoginSso(
            successIntentFactory.createLoginSsoIntent(
                login = login,
                serviceProviderUrl = ssoInfo.serviceProviderUrl,
                isSsoProvider = ssoInfo.isNitroProvider,
                migrateToMasterPasswordUser = ssoInfo.migration == SsoInfo.Migration.TO_MASTER_PASSWORD_USER
            )
        )
    }

    

    private fun getLoginInputType(email: String) = when {
        preFillLogin && email == preferredLogin -> LoginEmailLogger.INPUT_PRE_FILL
        autoFilledEmail && email == preferredLogin -> LoginEmailLogger.INPUT_AUTO_FILL
        else -> LoginEmailLogger.INPUT_MANUAL
    }
}
