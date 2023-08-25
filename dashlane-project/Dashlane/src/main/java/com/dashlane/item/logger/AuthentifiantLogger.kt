package com.dashlane.item.logger

import com.dashlane.inapplogin.UsageLogCode35Action
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.DataCounter
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.useractivity.log.usage.UsageLogCode11
import com.dashlane.useractivity.log.usage.UsageLogCode35
import com.dashlane.useractivity.log.usage.UsageLogConstant
import com.dashlane.useractivity.log.usage.UsageLogRepository

class AuthentifiantLogger(
    teamspaceAccessor: TeamspaceAccessor,
    dataCounter: DataCounter,
    sessionManager: SessionManager,
    bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) : BaseLogger(teamspaceAccessor, dataCounter, sessionManager, bySessionUsageLogRepository) {

    fun logCopyLogin(url: String) {
        log(
            UsageLogCode35(
                type = UsageLogCode11.Type.AUTHENTICATION.code,
                action = UsageLogCode35Action.COPY_LOGIN,
                website = url
            )
        )
    }

    fun logCopyPassword(url: String) {
        log(
            UsageLogCode35(
                type = UsageLogCode11.Type.AUTHENTICATION.code,
                action = UsageLogCode35Action.COPY_PASSWORD,
                website = url
            )
        )
    }

    fun logRevealPassword(url: String) {
        log(
            UsageLogCode35(
                type = UsageLogCode11.Type.AUTHENTICATION.code,
                action = UsageLogCode35Action.SHOW_PASSWORD,
                website = url
            )
        )
    }

    fun logGoToWebsiteOptions() {
        log(
            UsageLogCode35(
                type = UsageLogCode11.Type.AUTHENTICATION.code,
                action = UsageLogConstant.ActionType.askGotoBrowserAfterCopyPassword
            )
        )
    }

    fun logGoToWebsite(isPasswordCopied: Boolean, isLoginCopied: Boolean, packageName: String) {
        val action = when {
            isPasswordCopied -> UsageLogConstant.ActionType.gotoBrowserAfterCopyPassword
            isLoginCopied -> UsageLogConstant.ActionType.gotoBrowserAfterCopyLogin
            else -> UsageLogConstant.ActionType.gotoBrowser
        }
        log(
            UsageLogCode35(
                type = UsageLogCode11.Type.AUTHENTICATION.code,
                action = action,
                subaction = packageName
            )
        )
    }
}