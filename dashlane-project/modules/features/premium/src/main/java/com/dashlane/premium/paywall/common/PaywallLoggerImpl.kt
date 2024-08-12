package com.dashlane.premium.paywall.common

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.CallToAction.ALL_OFFERS
import com.dashlane.hermes.generated.definitions.CallToAction.CANCEL
import com.dashlane.hermes.generated.definitions.CallToAction.CLOSE
import com.dashlane.hermes.generated.definitions.CallToAction.PREMIUM_OFFER
import com.dashlane.hermes.generated.events.user.CallToAction
import javax.inject.Inject
import com.dashlane.hermes.generated.definitions.CallToAction as CallToActionValue

class PaywallLoggerImpl @Inject constructor(
    private val logRepository: LogRepository,
) : PaywallLogger {

    private var chosenActionSent = false

    override fun onLeaving(callToActionList: List<CallToActionValue>) {
        if (!chosenActionSent) {
            logUserAction(chosenAction = null, callToActionList = callToActionList)
        }
    }

    override fun onClickSeeAllOptions(callToActionList: List<CallToActionValue>) {
        logUserAction(chosenAction = ALL_OFFERS, callToActionList = callToActionList)
    }

    override fun onClickUpgrade(callToActionList: List<CallToActionValue>) {
        logUserAction(chosenAction = PREMIUM_OFFER, callToActionList = callToActionList)
    }

    override fun onNavigateUp(callToActionList: List<CallToActionValue>) {
        logUserAction(chosenAction = null, callToActionList = callToActionList)
    }

    override fun onClickCancel(callToActionList: List<CallToActionValue>) {
        logUserAction(chosenAction = CANCEL, callToActionList = callToActionList)
    }

    override fun onClickClose(callToActionList: List<CallToActionValue>) {
        logUserAction(chosenAction = CLOSE, callToActionList = callToActionList)
    }

    private fun logUserAction(chosenAction: CallToActionValue?, callToActionList: List<CallToActionValue>) =
        logRepository.queueEvent(
            CallToAction(
                callToActionList = callToActionList,
                hasChosenNoAction = chosenAction == null,
                chosenAction = chosenAction
            )
        ).also { chosenActionSent = true }
}