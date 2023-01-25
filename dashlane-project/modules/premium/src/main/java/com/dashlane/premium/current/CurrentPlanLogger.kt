package com.dashlane.premium.current

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.events.user.CallToAction
import com.dashlane.premium.current.model.CurrentPlan
import com.dashlane.util.setCurrentPageView
import com.skocken.presentation.definition.Base
import com.dashlane.hermes.generated.definitions.CallToAction as CallToActionValue

class CurrentPlanLogger(
    private val logRepository: LogRepository
) {
    private lateinit var presenter: Base.IPresenter
    private lateinit var recommendedActions: List<CallToActionValue>
    private var hasChosenAction = false

    internal fun initPresenter(presenter: Base.IPresenter) {
        this.presenter = presenter
    }

    internal fun showCurrentPlan(primaryAction: CurrentPlan.Action, secondaryAction: CurrentPlan.Action?) {
        hasChosenAction = false
        presenter.setCurrentPageView(page = AnyPage.CURRENT_PLAN, fromAutofill = false)
        recommendedActions = listOfNotNull(
            primaryAction.type.toCallToActionValue().first,
            secondaryAction?.type?.toCallToActionValue()?.first
        )
    }

    internal fun showDwmInfo() {
        presenter.setCurrentPageView(page = AnyPage.CURRENT_PLAN_DWM_LEARN_MORE, fromAutofill = false)
    }

    internal fun closeDwmInfo() {
        presenter.setCurrentPageView(page = AnyPage.CURRENT_PLAN, fromAutofill = false)
    }

    internal fun onActionClicked(actionType: CurrentPlan.Action.Type) {
        val (chosenAction, hasChosenNoAction) = actionType.toCallToActionValue()
        this.hasChosenAction = !hasChosenNoAction
        logRepository.queueEvent(
            CallToAction(
                callToActionList = recommendedActions,
                hasChosenNoAction = hasChosenNoAction,
                chosenAction = chosenAction
            )
        )
    }

    internal fun onCurrentPlanFinishing() {
        if (!hasChosenAction) {
            logRepository.queueEvent(
                CallToAction(
                    callToActionList = recommendedActions,
                    hasChosenNoAction = true,
                )
            )
        }
    }

    private fun CurrentPlan.Action.Type.toCallToActionValue() = when (this) {
        CurrentPlan.Action.Type.ALL_PLANS -> CallToActionValue.ALL_OFFERS to false
        CurrentPlan.Action.Type.PREMIUM -> CallToActionValue.PREMIUM_OFFER to false
        CurrentPlan.Action.Type.FAMILY -> CallToActionValue.FAMILY_OFFER to false
        CurrentPlan.Action.Type.CLOSE -> null to true
    }
}