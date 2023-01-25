package com.dashlane.premium.paywall.common

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.CallToAction.ALL_OFFERS
import com.dashlane.hermes.generated.definitions.CallToAction.PREMIUM_OFFER
import com.dashlane.hermes.generated.events.user.CallToAction
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLogCode75
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.util.setCurrentPageView
import javax.inject.Inject
import com.dashlane.hermes.generated.definitions.CallToAction as CallToActionValue

class PaywallLoggerImpl @Inject constructor(
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val sessionManager: SessionManager,
    private val logRepository: LogRepository,
) : PaywallLogger {

    override lateinit var trackingKey: String
    private var chosenActionSent = false

    override fun onShowPaywall(presenter: PaywallPresenter) {
        logLegacy("display")
        presenter.setCurrentPageView(page = presenter.page)
    }

    override fun onLeaving() {
        if (!chosenActionSent) {
            logUserAction(chosenAction = null)
        }
    }

    override fun onClickSeeAllOptions() {
        logLegacy("see_plan_options")
        logUserAction(chosenAction = ALL_OFFERS)
    }

    override fun onClickUpgrade() {
        logLegacy("go_premium")
        logUserAction(chosenAction = PREMIUM_OFFER)
    }

    override fun onClickClose() {
        logLegacy("close")
        logUserAction(chosenAction = null)
    }

    private fun logLegacy(action: String) {
        bySessionUsageLogRepository[sessionManager.session]?.enqueue(
            UsageLogCode75(
                type = "premium_prompt",
                subtype = trackingKey,
                action = action
            )
        )
    }

    private fun logUserAction(chosenAction: CallToActionValue?) =
        logRepository.queueEvent(
            CallToAction(
                callToActionList = listOf(PREMIUM_OFFER, ALL_OFFERS),
                hasChosenNoAction = chosenAction == null,
                chosenAction = chosenAction
            )
        ).also { chosenActionSent = true }
}