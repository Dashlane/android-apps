package com.dashlane.ui.screens.fragments.onboarding.inapplogin

import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLog
import com.dashlane.useractivity.log.usage.UsageLogCode95
import com.dashlane.useractivity.log.usage.UsageLogRepository

class OnboardingInAppLoginLogger(
    private val sessionManager: SessionManager,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) {

    fun logDismissSuccessScreen(from: String?, type: UsageLogCode95.Type?) {
        log(
            UsageLogCode95(
                action = UsageLogCode95.Action.DISMISS_SUCCESS_SCREEN,
                fromStr = from,
                type = type
            )
        )
    }

    fun logSeeStep2(from: String?, type: UsageLogCode95.Type?) {
        log(
            UsageLogCode95(
                action = UsageLogCode95.Action.SEE_STEP2,
                fromStr = from,
                type = type
            )
        )
    }

    fun logNoThanks(from: String?, type: UsageLogCode95.Type?) {
        log(
            UsageLogCode95(
                action = UsageLogCode95.Action.NO_THANKS,
                fromStr = from,
                type = type
            )
        )
    }

    fun logGo(from: String?, type: UsageLogCode95.Type?) {
        log(
            UsageLogCode95(
                action = UsageLogCode95.Action.GO,
                fromStr = from,
                type = type
            )
        )
    }

    fun logDoItLaterGo(from: String?, type: UsageLogCode95.Type?) {
        log(
            UsageLogCode95(
                action = UsageLogCode95.Action.NO_POPUP_GO,
                fromStr = from,
                type = type
            )
        )
    }

    fun logDoItLaterNoThanks(from: String?, type: UsageLogCode95.Type?) {
        log(
            UsageLogCode95(
                action = UsageLogCode95.Action.NO_POPUP_NOTHANKS,
                fromStr = from,
                type = type
            )
        )
    }

    private fun log(log: UsageLog) {
        bySessionUsageLogRepository[sessionManager.session]?.enqueue(log)
    }

    companion object {

        operator fun invoke(): OnboardingInAppLoginLogger {
            return OnboardingInAppLoginLogger(
                SingletonProvider.getSessionManager(),
                SingletonProvider.getComponent().bySessionUsageLogRepository
            )
        }
    }
}