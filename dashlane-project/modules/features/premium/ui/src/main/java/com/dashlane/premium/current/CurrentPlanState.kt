package com.dashlane.premium.current

import com.dashlane.mvvm.State
import com.dashlane.premium.current.model.CurrentPlan

internal sealed class CurrentPlanState : State {
    sealed class View : CurrentPlanState(), State.View {
        data object Initial : View()

        data object Loading : View()

        data class Loaded(val currentPlan: CurrentPlan, val moreInfo: MoreInfo) : View()
    }

    sealed class SideEffect : CurrentPlanState(), State.SideEffect {
        data class CloseWithoutAction(val recommendedActions: List<CurrentPlan.Action.Type>) : SideEffect()
    }
}

internal sealed class MoreInfo {
    data object Hidden : MoreInfo()

    data class Displaying(val moreInfoType: CurrentPlan.Benefit.MoreInfoType) : MoreInfo()
}