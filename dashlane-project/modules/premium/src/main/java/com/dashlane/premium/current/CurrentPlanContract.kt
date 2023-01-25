package com.dashlane.premium.current

import com.dashlane.premium.current.model.CurrentPlan
import com.dashlane.premium.current.model.CurrentPlanType
import com.dashlane.premium.current.ui.CurrentBenefitItem
import com.skocken.presentation.definition.Base

internal interface CurrentPlanContract {
    interface ViewProxy : Base.IView {
        fun showCurrentPlan(currentPlan: CurrentPlan)
    }

    interface DataProvider : Base.IDataProvider {
        fun getType(): CurrentPlanType
        fun getBenefits(): List<CurrentPlan.Benefit>
        fun isVpnAllowed(): Boolean
    }

    interface Presenter : Base.IPresenter {
        fun refresh()
        fun onActionClicked(actionType: CurrentPlan.Action.Type)
        fun onItemClicked(item: CurrentBenefitItem)
        fun openDarkWebMonitoringInfo()
        fun onDestroy()
    }
}