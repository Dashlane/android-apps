package com.dashlane.passwordhealth.identitydashboard

import com.dashlane.passwordanalysis.SecurityScoreEvaluator
import com.dashlane.passwordhealth.identitydashboard.item.IdentityDashboardItem
import com.skocken.presentation.definition.Base
import kotlinx.coroutines.Deferred

interface IdentityDashboardContract {
    interface ViewProxy : Base.IView {
        fun setItems(items: List<IdentityDashboardItem>)

        fun setOnboardingEmptyState()

        fun remove(item: IdentityDashboardItem)
    }

    interface Presenter : Base.IPresenter {
        fun onViewVisible()

        fun onViewHidden()

        fun requireRefresh(forceRefresh: Boolean)

        fun onAddLoginClick()
    }

    interface DataProvider : Base.IDataProvider {
        fun hasProtectionPackage(): Boolean

        fun shouldIdentityRestorationBeVisible(): Boolean

        fun getAuthentifiantsSecurityInfoAsync(forceRefresh: Boolean): Deferred<SecurityScoreEvaluator.Result?>

        fun listenForChanges()

        fun unlistenForChanges()
    }
}