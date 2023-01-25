package com.dashlane.login.sso

import android.app.Activity
import com.dashlane.core.DataSync
import com.dashlane.hermes.LogRepository
import com.dashlane.login.LoginLogger
import com.dashlane.login.LoginMode
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.install.InstallLogCode69
import com.dashlane.useractivity.log.install.InstallLogRepository
import com.dashlane.useractivity.log.usage.UsageLogCode2
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.util.getParcelableExtraCompat
import dagger.Reusable
import javax.inject.Inject

@Reusable
class LoginSsoLoggerImpl @Inject constructor(
    activity: Activity,
    private val sessionManager: SessionManager,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val installLogRepository: InstallLogRepository,
    logRepository: LogRepository
) : LoginSsoLogger {
    private val config =
        activity.intent.getParcelableExtraCompat<LoginSsoLogger.Config>(LoginSsoLogger.Config.INTENT_EXTRA_KEY)
    private val trackingId get() = config?.trackingId
    private val installLogCode69Type get() = config?.installLogCode69Type

    private val usageLogRepository get() = sessionManager.session?.let(bySessionUsageLogRepository::get)
    private val loginLogger = LoginLogger(logRepository)

    override fun logLoginStart() {
        installLog(step = if (installLogCode69Type == InstallLogCode69.Type.LOGIN) "14.2" else "14.1")
        loginLogger.logAskAuthentication(LoginMode.Sso)
    }

    override fun logLoginSuccess() {
        

        usageLogRepository?.enqueue(
            UsageLogCode2(
                teamSso = true,
                appWebsite = config?.usageLogCode2AppWebsite,
                sender = config?.usageLogCode2Sender,
                loadDuration = DataSync.getLastSyncDuration().toLong()
            )
        )
    }

    override fun logInvalidSso() {
        loginLogger.logInvalidSso()
    }

    override fun logErrorUnknown() {
        loginLogger.logErrorUnknown(LoginMode.Sso)
    }

    override fun logGetUserSsoInfoSuccess() = installLog(step = "14.4", subAction = "success")

    override fun logGetUserSsoInfoCancel() = installLog(step = "14.4.1", subAction = "cancel")

    override fun logAccountCreationStart() = installLog(step = "14.5", action = "createAccount")

    override fun logNotProvisionedDisplay() = installLog(step = "14.3", subAction = "notProvisioned")

    private fun installLog(
        step: String,
        action: String = "continue",
        subAction: String? = null
    ) {
        installLogRepository.enqueue(
            InstallLogCode69(
                loginSession = trackingId,
                type = installLogCode69Type,
                subType = "ssoLogin",
                action = action,
                subAction = subAction,
                subStep = step
            )
        )
    }
}