package com.dashlane.premium.current

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.hermes.generated.events.user.CallToAction
import com.dashlane.premium.current.model.CurrentPlan
import javax.inject.Inject
import com.dashlane.hermes.generated.definitions.CallToAction as CallToActionValue

class CurrentPlanLogger @Inject constructor(
    private val logRepository: LogRepository
) {

    internal fun showCurrentPlan() {
        logRepository.queuePageView(component = BrowseComponent.MAIN_APP, page = AnyPage.CURRENT_PLAN)
    }

    internal fun showDwmInfo() {
        logRepository.queuePageView(component = BrowseComponent.MAIN_APP, page = AnyPage.CURRENT_PLAN_DWM_LEARN_MORE)
    }

    internal fun closeDwmInfo() {
        showCurrentPlan()
    }

    internal fun onActionClicked(
        actionType: CurrentPlan.Action.Type,
        recommendedActions: List<CurrentPlan.Action.Type>
    ) {
        logRepository.queueEvent(
            CallToAction(
                callToActionList = recommendedActions.mapNotNull { it.toCallToActionValue() },
                hasChosenNoAction = false,
                chosenAction = actionType.toCallToActionValue()
            )
        )
    }

    internal fun onCancel(recommendedActions: List<CurrentPlan.Action.Type>) {
        logRepository.queueEvent(
            CallToAction(
                callToActionList = recommendedActions.mapNotNull { it.toCallToActionValue() },
                hasChosenNoAction = true,
            )
        )
    }

    private fun CurrentPlan.Action.Type.toCallToActionValue() = when (this) {
        CurrentPlan.Action.Type.ALL_PLANS -> CallToActionValue.ALL_OFFERS
        CurrentPlan.Action.Type.PREMIUM -> CallToActionValue.PREMIUM_OFFER
        CurrentPlan.Action.Type.FAMILY -> CallToActionValue.FAMILY_OFFER
        CurrentPlan.Action.Type.CLOSE -> null
    }
}