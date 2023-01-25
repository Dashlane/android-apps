package com.dashlane.darkweb.ui.setup

import androidx.lifecycle.SavedStateHandle
import com.dashlane.darkweb.ui.intro.DarkWebSetupIntroActivity
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLogCode129
import com.dashlane.useractivity.log.usage.UsageLogRepository
import javax.inject.Inject

internal class DarkWebSetupMailLoggerImpl @Inject constructor(
    savedStateHandle: SavedStateHandle,
    usageLogRepositoryBySession: BySessionRepository<UsageLogRepository>,
    sessionManager: SessionManager
) : DarkWebSetupMailLogger {
    private val usageLogRepository = usageLogRepositoryBySession[sessionManager.session]

    private val origin: String? = savedStateHandle[DarkWebSetupIntroActivity.ORIGIN_KEY]

    override fun logShow() {
        log(UsageLogCode129.Action.SHOW)
    }

    override fun logCancel() {
        log(UsageLogCode129.Action.CANCEL)
    }

    override fun logNext() {
        log(UsageLogCode129.Action.NEXT)
    }

    override fun logInvalidMailLocal() {
        log(UsageLogCode129.Action.ERROR, INVALID_EMAIL_LOCAL)
    }

    override fun logEmptyMail() {
        log(UsageLogCode129.Action.ERROR, FIELD_EMPTY)
    }

    override fun logInvalidMailServer() {
        log(UsageLogCode129.Action.ERROR, INVALID_EMAIL_SERVER)
    }

    override fun logAlreadyValidatedMail() {
        log(UsageLogCode129.Action.ERROR, ALREADY_VALIDATED)
    }

    override fun logLimitReached() {
        log(UsageLogCode129.Action.ERROR, MAX_MAIL_LIMIT)
    }

    private fun log(action: UsageLogCode129.Action, subAction: String? = null) {
        usageLogRepository?.enqueue(
            UsageLogCode129(
                type = UsageLogCode129.Type.DARK_WEB_REGISTRATION,
                typeSub = TYPE_SUB_ENTER_EMAIL,
                action = action,
                actionSub = subAction,
                origin = origin
            )
        )
    }

    companion object {
        const val FIELD_EMPTY = "empty_field"
        const val INVALID_EMAIL_LOCAL = "invalid_email_local"
        const val INVALID_EMAIL_SERVER = "invalid_email_server"
        const val ALREADY_VALIDATED = "already_validated"
        const val MAX_MAIL_LIMIT = "max_mail_limit"

        const val TYPE_SUB_ENTER_EMAIL = "enter_email"
    }
}