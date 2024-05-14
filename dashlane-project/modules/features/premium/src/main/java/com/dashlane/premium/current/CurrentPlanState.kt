package com.dashlane.premium.current

import com.dashlane.premium.current.model.CurrentPlan
import com.dashlane.premium.offer.common.model.OfferType

internal sealed class CurrentPlanState {

    abstract val uiData: UiData

    object Init : CurrentPlanState() {
        override val uiData: UiData = UiData(null)
    }

    object Loading : CurrentPlanState() {
        override val uiData: UiData = UiData(null)
    }

    data class Loaded(override val uiData: UiData) : CurrentPlanState()

    data class NavigateToPlansPage(
        override val uiData: UiData,
        val offerType: OfferType?
    ) : CurrentPlanState()

    data class CloseWithoutAction(override val uiData: UiData) : CurrentPlanState()

    data class UiData(val plan: CurrentPlan?)
}