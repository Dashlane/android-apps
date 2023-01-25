package com.dashlane.autofill.core

import com.dashlane.autofill.api.pause.AutofillApiPauseLogger
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.install.InstallLogCode42
import com.dashlane.useractivity.log.install.InstallLogRepository
import com.dashlane.useractivity.log.usage.UsageLogCode96
import com.dashlane.useractivity.log.usage.UsageLogRepository
import javax.inject.Inject



class AutofillApiPauseLoggerImpl @Inject constructor(
    sessionManager: SessionManager,
    bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    installLogRepository: InstallLogRepository?
) : AutofillApiPauseLogger,
    AutofillLegacyLogger(sessionManager, bySessionUsageLogRepository, installLogRepository) {

    

    override fun onClickPauseSuggestion(
        packageName: String,
        webappDomain: String,
        hasCredentials: Boolean,
        loggedIn: Boolean
    ) {
        if (loggedIn)
            onClickPauseUL96(UsageLogCode96.Action.CLICK_PAUSE, packageName, webappDomain, hasCredentials)
        else
            onClickPauseIL42(InstallLogCode42.Action.CLICK_PAUSE, packageName)
    }

    

    override fun onClickShortPause(
        packageName: String,
        webappDomain: String,
        hasCredentials: Boolean,
        loggedIn: Boolean
    ) {
        if (loggedIn)
            onClickPauseUL96(UsageLogCode96.Action.SHORT_PAUSE, packageName, webappDomain, hasCredentials)
        else
            onClickPauseIL42(InstallLogCode42.Action.SHORT_PAUSE, packageName)
    }

    

    override fun onClickLongPause(
        packageName: String,
        webappDomain: String,
        hasCredentials: Boolean,
        loggedIn: Boolean
    ) {
        if (loggedIn)
            onClickPauseUL96(UsageLogCode96.Action.LONG_PAUSE, packageName, webappDomain, hasCredentials)
        else
            onClickPauseIL42(InstallLogCode42.Action.LONG_PAUSE, packageName)
    }

    

    override fun onClickDefinitePause(
        packageName: String,
        webappDomain: String,
        hasCredentials: Boolean,
        loggedIn: Boolean
    ) {
        if (loggedIn)
            onClickPauseUL96(UsageLogCode96.Action.DEFINITE_PAUSE, packageName, webappDomain, hasCredentials)
        else
            onClickPauseIL42(InstallLogCode42.Action.DEFINITE_PAUSE, packageName)
    }

    private fun onClickPauseUL96(
        action: UsageLogCode96.Action,
        packageName: String,
        webappDomain: String,
        hasCredentials: Boolean
    ) {
        log(
            UsageLogCode96(
                app = packageName,
                sender = UsageLogCode96.Sender.AUTOFILL_API,
                action = action,
                hasCredentials = hasCredentials,
                type = UsageLogCode96.Type.AUTHENTICATION,
                webappDomain = webappDomain
            )
        )
    }

    private fun onClickPauseIL42(action: InstallLogCode42.Action, packageName: String) {
        log(
            InstallLogCode42(
                sender = InstallLogCode42.Sender.AUTOFILL_API,
                action = action,
                appPackage = packageName
            )
        )
    }
}
