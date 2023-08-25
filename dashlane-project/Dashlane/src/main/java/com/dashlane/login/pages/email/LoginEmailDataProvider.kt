package com.dashlane.login.pages.email

import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import com.dashlane.R
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
import com.dashlane.authentication.AuthenticationTeamException
import com.dashlane.authentication.CipheredBackupToken
import com.dashlane.authentication.UnauthenticatedUser
import com.dashlane.authentication.login.AuthenticationEmailRepository
import com.dashlane.authentication.login.AuthenticationEmailRepository.Result.RequiresDeviceRegistration
import com.dashlane.authentication.login.AuthenticationEmailRepository.Result.RequiresPassword
import com.dashlane.authentication.login.AuthenticationEmailRepository.Result.RequiresServerKey
import com.dashlane.authentication.login.AuthenticationEmailRepository.Result.RequiresSso
import com.dashlane.authentication.login.SsoInfo
import com.dashlane.crashreport.CrashReporter
import com.dashlane.logger.usersupportlogger.UserSupportFileUploadState
import com.dashlane.logger.usersupportlogger.UserSupportFileUploader
import com.dashlane.login.LoginSuccessIntentFactory
import com.dashlane.login.pages.LoginBaseContract
import com.dashlane.login.pages.LoginBaseDataProvider
import com.dashlane.login.sso.toMigrationToSsoMemberInfo
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.server.api.endpoints.account.AccountExistsService
import com.dashlane.util.Toaster
import com.dashlane.util.inject.qualifiers.IoCoroutineDispatcher
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LoginEmailDataProvider @Inject constructor(
    private val userAccountStorage: UserAccountStorageImpl,
    private val logger: LoginEmailLogger,
    private val userSupportFileUploader: UserSupportFileUploader,
    private val preferences: GlobalPreferencesManager,
    private val crashReporter: CrashReporter,
    private val emailRepository: AuthenticationEmailRepository,
    private val accountExistsService: AccountExistsService,
    private val successIntentFactory: LoginSuccessIntentFactory,
    private val clipboardManager: ClipboardManager?,
    private val toaster: Toaster,
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher
) : LoginBaseDataProvider<LoginEmailContract.Presenter>(), LoginEmailContract.DataProvider {

    override val loginHistory: List<String> by lazy { preferences.getUserListHistory() }
    override val preferredLogin: String? by lazy { loginHistory.firstOrNull() }
    override val preFillLogin: Boolean by lazy { !preferences.isUserLoggedOut && !preferredLogin.isNullOrEmpty() }

    override fun uploadUserSupportFile() {
        userSupportFileUploader.startCrashLogsUpload(
            crashDeviceId = crashReporter.crashReporterId
        )
            .flowOn(ioDispatcher)
            .onEach { state ->
                when (state) {
                    UserSupportFileUploadState.Failed -> presenter.showUploadFailedDialog()
                    is UserSupportFileUploadState.Finished -> presenter.showUploadFinishedDialog(
                        state.crashDeviceId,
                        ::copy
                    )

                    UserSupportFileUploadState.Uploading -> presenter.showUploadingDialog()
                }
            }
            .launchIn(MainScope())
    }

    override fun getTrackingInstallationId() = preferences.installationTrackingId

    private var autoFilledEmail = false

    override val username: String
        get() = preferredLogin?.takeIf { preFillLogin } ?: ""

    override fun onAutoFill() {
        autoFilledEmail = true
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
                handleUserStatusError(e)
            }
        } catch (e: AuthenticationException) {
            handleUserStatusError(e)
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
            is RequiresDeviceRegistration.SecondFactor -> handleUserRequiresDeviceRegistrationSecondFactor(
                userStatus
            )

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
                presenter.showOtpPage(secondFactor)
            }
        }
    }

    private fun handleUserRequiresDeviceRegistrationSso(userStatus: RequiresDeviceRegistration.Sso) {
        val login = userStatus.login
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
        presenter.showOtpPage(secondFactor)
    }

    private fun handleUserStatusRequiresPassword(userStatus: RequiresPassword) {
        presenter.showPasswordStep(
            userStatus.registeredUserDevice
        )
    }

    private fun handleUserStatusRequiresSso(userStatus: RequiresSso) {
        startLoginSso(userStatus.login, userStatus.ssoInfo)
    }

    private fun handleUserStatusError(e: AuthenticationException) {
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
                throw LoginBaseContract.OfflineException(e)
            }

            is AuthenticationNetworkException -> {
                presenter.notifyNetworkError()
            }

            is AuthenticationExpiredVersionException -> {
                throw LoginBaseContract.ExpiredVersionException(e)
            }

            is AuthenticationTeamException -> {
                presenter.notifyTeamError()
            }

            else -> {
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

    private fun copy(data: String) {
        runCatching {
            val clip = ClipData.newPlainText("data", data)
            if (clip != null) {
                clipboardManager?.setPrimaryClip(clip)
            }
            toaster.show(R.string.user_support_file_copied, Toast.LENGTH_SHORT)
        }
    }
}
