package com.dashlane.premium.paywall.common

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.CallToAction.ALL_OFFERS
import com.dashlane.hermes.generated.definitions.CallToAction.PREMIUM_OFFER
import com.dashlane.hermes.generated.events.user.CallToAction
import javax.inject.Inject
import com.dashlane.hermes.generated.definitions.CallToAction as CallToActionValue

class PaywallLoggerImpl @Inject constructor(
    private val logRepository: LogRepository,
) : PaywallLogger {

    private var chosenActionSent = false

    override fun onLeaving() {
        if (!chosenActionSent) {
            logUserAction(chosenAction = null)
        }
    }

    override fun onClickSeeAllOptions() {
        logUserAction(chosenAction = ALL_OFFERS)
    }

    override fun onClickUpgrade() {
        logUserAction(chosenAction = PREMIUM_OFFER)
    }

    override fun onClickClose() {
        logUserAction(chosenAction = null)
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