package com.dashlane.login.pages.password

import android.content.Intent
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.authentication.SecurityFeature
import com.dashlane.core.DataSync
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.VerificationMode
import com.dashlane.login.LoginLogger
import com.dashlane.login.LoginMode
import com.dashlane.login.UserAccountStatus
import com.dashlane.login.dagger.TrackingId
import com.dashlane.login.lock.LockSetting
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.install.InstallLog
import com.dashlane.useractivity.log.install.InstallLogCode69
import com.dashlane.useractivity.log.install.InstallLogRepository
import com.dashlane.useractivity.log.usage.UsageLog
import com.dashlane.useractivity.log.usage.UsageLogCode17
import com.dashlane.useractivity.log.usage.UsageLogCode2
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.getUsageLogCode2SenderFromOrigin
import javax.inject.Inject



class LoginPasswordLoggerImpl(
    private val trackingId: String,
    private val otpRequired: Boolean,
    private val deviceStatus: InstallLogCode69.DeviceStatus,
    private val accountTypeFlags: String?,
    private val sessionManager: SessionManager,
    private val bySessionRepositoryUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val installLogRepository: InstallLogRepository,
    private val loginLogger: LoginLogger,
    private val verification: VerificationMode
) : LoginPasswordLogger {
    override fun logLand(allowBypass: Boolean) =
        log("10", "land", subType = "masterPasswordAllowBypass".takeIf { allowBypass })

    override fun logBack() =
        log("11.1", "back")

    override fun logEmptyPassword() {
        loginLogger.logWrongPassword(verification)
        log("12.2", "next", subAction = "missingPassword")
    }

    override fun logPasswordInvalid() {
        loginLogger.logWrongPassword(verification)
        log("12.2", "next", subAction = "incorrectPassword")
    }

    override fun logPasswordInvalidWithRecovery() {
        loginLogger.logWrongPassword(verification)
        log("12.2", "next", subAction = "incorrectPasswordWithRecovery")
    }

    override fun logNetworkError(@LoginPasswordLogger.NetworkError error: String) {
        loginLogger.logErrorUnknown(loginMode = LoginMode.MasterPassword(verification))
        log("12.2", "next", subAction = "networkError$error")
    }

    override fun logAccountReset() =
        log("12.3", "next", subAction = "backToEmailPage")

    override fun logPasswordSuccess(origin: Intent) {
        log("12.1", "next", subAction = "success")
        log(
            UsageLogCode2(
                otp = otpRequired,
                appWebsite = origin.getStringExtra(LockSetting.EXTRA_DOMAIN),
                sender = getUsageLogCode2SenderFromOrigin(origin),
                loadDuration = DataSync.getLastSyncDuration().toLong()
            )
        )
    }

    override fun logPasswordVisibilityToggle(shown: Boolean) =
        log(if (shown) "11.2" else "11.3", if (shown) "show" else "hide")

    override fun logLoginIssuesClicked() =
        log("12.4", "loginIssue")

    override fun logLoginIssuesShown() =
        logLoginIssues("13", "land")

    override fun logLoginHelp() =
        logLoginIssues("13.1", "cannotLogin")

    override fun logPasswordHelp() =
        logLoginIssues("13.2", "forgotPassword")

    override fun logAccountChange() =
        logLoginIssues("13.3", "userAnotherAccount")

    override fun logAccountSwitch() =
        logLoginIssues("13.3", "switchAccount")

    override fun logPasswordForgot() {
        log("12.5", "forgot")
    }

    override fun logUserStatus(userAccountStatus: UserAccountStatus, anonymousDeviceId: String) {
        val otp = when (userAccountStatus) {
            UserAccountStatus.NO -> 0
            UserAccountStatus.YES_OTP_LOGIN -> 2
            UserAccountStatus.YES_OTP_NEWDEVICE -> 1
            else -> 0
        }
        log(
            UsageLogCode17(
                otpstyle = otp,
                anonymouscomputerid = anonymousDeviceId,
                creation = false,
                preloaded = false
            )
        )
    }

    override fun logAskMasterPasswordLater() =
        log(action = "askLater", step = "13.4", subType = "masterPasswordAllowBypass")

    override fun logRegisteredWithBackupToken() =
        log(step = "13.6", action = "registeredWithBackupToken", subType = "restoreDevice")

    override fun logRegisterWithBackupTokenError() =
        log(step = "13.5", action = "registerWithBackupTokenError", subType = "restoreDevice")

    private fun log(step: String, action: String, subType: String? = null, subAction: String? = null) =
        log("masterPassword", action, step, subType, subAction)

    private fun logLoginIssues(
        step: String,
        action: String,
        subType: String? = null,
        subAction: String? = null
    ) =
        log("loginIssues", action, step, subType, subAction)

    private fun log(
        value: String? = null,
        action: String,
        step: String,
        subType: String? = null,
        subAction: String? = null
    ) {
        log(
            InstallLogCode69(
                loginSession = trackingId,
                type = InstallLogCode69.Type.LOGIN,
                subType = subType ?: value,
                action = action,
                deviceStatus = deviceStatus,
                accountType = accountTypeFlags,
                subStep = step,
                subAction = subAction
            )
        )
    }

    private fun log(log: UsageLog) {
        sessionManager.session?.let { bySessionRepositoryUsageLogRepository[it] }
            ?.enqueue(log)
    }

    private fun log(log: InstallLog) {
        installLogRepository.enqueue(log)
    }

    class Factory @Inject constructor(
        @TrackingId private val trackingId: String,
        private val sessionManager: SessionManager,
        private val bySessionRepositoryUsageLogRepository: BySessionRepository<UsageLogRepository>,
        private val installLogRepository: InstallLogRepository,
        private val logRepository: LogRepository
    ) : LoginPasswordLogger.Factory {
        override fun create(
            registeredUserDevice: RegisteredUserDevice,
            verification: VerificationMode
        ): LoginPasswordLoggerImpl {
            val isLocalDevice = registeredUserDevice is RegisteredUserDevice.Local
            val deviceStatus =
                if (isLocalDevice) InstallLogCode69.DeviceStatus.REGISTERED else InstallLogCode69.DeviceStatus.NEW
            val isServerKeyRequired = registeredUserDevice.isServerKeyRequired
            val securityFeatures = registeredUserDevice.securityFeatures
            val otpRequired =
                isServerKeyRequired || (!isLocalDevice && securityFeatures.contains(SecurityFeature.TOTP))
            return LoginPasswordLoggerImpl(
                trackingId,
                otpRequired,
                deviceStatus,
                securityFeatures.toLogString(isServerKeyRequired),
                sessionManager,
                bySessionRepositoryUsageLogRepository,
                installLogRepository,
                LoginLogger(logRepository),
                verification = verification
            )
        }
    }
}

private fun Set<SecurityFeature>.toLogString(isServerKeyRequired: Boolean) =
    buildString {
        if (contains(SecurityFeature.EMAIL_TOKEN)) append("token")
        if (contains(SecurityFeature.TOTP)) append(if (isServerKeyRequired) "OTP2" else "OTP1")
        if (contains(SecurityFeature.DUO)) append("AndDuo")
    }