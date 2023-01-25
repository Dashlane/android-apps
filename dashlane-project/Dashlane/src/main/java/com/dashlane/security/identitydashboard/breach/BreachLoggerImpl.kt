package com.dashlane.security.identitydashboard.breach

import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLogCode126
import com.dashlane.useractivity.log.usage.UsageLogCode129
import com.dashlane.useractivity.log.usage.UsageLogRepository
import javax.inject.Inject

class BreachLoggerImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) : BreachLogger {
    override fun logPopupShow(breachWrapper: BreachWrapper) {
        log(TYPE_POPUP, ACTION_SHOW, null, breachWrapper)
    }

    override fun logPopupView(breachWrapper: BreachWrapper) {
        log(TYPE_POPUP, ACTION_VIEW, null, breachWrapper)
    }

    override fun logPopupClose(breachWrapper: BreachWrapper) {
        log(TYPE_POPUP, ACTION_LATER, null, breachWrapper)
    }

    override fun logMultiPopupShow(alertCount: String) {
        log(TYPE_POPUP_MULTI, ACTION_SHOW, alertCount, null)
    }

    override fun logMultiPopupView(alertCount: String) {
        log(TYPE_POPUP_MULTI, ACTION_VIEW, alertCount, null)
    }

    override fun logMultiPopupClose(alertCount: String) {
        log(TYPE_POPUP_MULTI, ACTION_LATER, alertCount, null)
    }

    override fun logTrayShow(itemPosition: Int, breachWrapper: BreachWrapper) {
        log(TYPE_TRAY, ACTION_SHOW, itemPosition.toString(), breachWrapper)
    }

    override fun logTrayClose(itemPosition: Int, breachWrapper: BreachWrapper) {
        log(TYPE_TRAY, ACTION_LATER, itemPosition.toString(), breachWrapper)
    }

    override fun logOpenAlertDetail() {
        bySessionUsageLogRepository[sessionManager.session]?.enqueue(
            UsageLogCode129(
                UsageLogCode129.Type.IDENTITY_DASHBOARD,
                "dark_web_monitoring",
                UsageLogCode129.Action.CLICK,
                "open_alert",
                "dark_web_monitoring"
            )
        )
    }

    private fun log(
        type: UsageLogCode126.Type,
        action: UsageLogCode126.Action,
        actionSub: String?,
        breachWrapper: BreachWrapper?
    ) {
        bySessionUsageLogRepository[sessionManager.session]
            ?.enqueue(
                UsageLogCode126(
                    type = type,
                    typeSub = breachWrapper?.let { getBreachTypeSub(it) },
                    action = action,
                    actionSub = actionSub,
                    alertId = breachWrapper?.takeIf { it.publicBreach.isDarkWebBreach() }?.publicBreach?.id,
                    similarCredentialsCount = breachWrapper?.takeIf { it.publicBreach.isDarkWebBreach() }?.linkedAuthentifiant?.count()
                )
            )
    }

    companion object {

        fun getBreachTypeSub(breachWrapper: BreachWrapper): UsageLogCode126.TypeSub {
            val breach = breachWrapper.publicBreach
            return if (breach.isDarkWebBreach()) {
                when {
                    breach.domains.isNullOrEmpty() && breachWrapper.linkedAuthentifiant.isNullOrEmpty() ->
                        UsageLogCode126.TypeSub.DARK_WEB_NODOMAINNOMATCH
                    breach.domains.isNullOrEmpty() -> UsageLogCode126.TypeSub.DARK_WEB_NODOMAIN
                    breach.hasPasswordLeaked() && breach.hasPrivateInformationLeaked() -> UsageLogCode126.TypeSub.DARK_WEB_PASSWORD_PIIS
                    breach.hasPasswordLeaked() -> UsageLogCode126.TypeSub.DARK_WEB_PASSWORD
                    else -> UsageLogCode126.TypeSub.DARK_WEB_PIIS
                }
            } else {
                UsageLogCode126.TypeSub.SECURITY
            }
        }

        val TYPE_POPUP = UsageLogCode126.Type.POP_UP
        val TYPE_POPUP_MULTI = UsageLogCode126.Type.POP_UP_MULTI
        val TYPE_TRAY = UsageLogCode126.Type.FEED_ALERT
        val TYPE_OVERVIEW = UsageLogCode126.Type.FEED_OVERVIEW

        val ACTION_SHOW = UsageLogCode126.Action.SHOW
        val ACTION_VIEW = UsageLogCode126.Action.VIEW
        val ACTION_LATER = UsageLogCode126.Action.LATER
    }
}