package com.dashlane.security.identitydashboard

import com.dashlane.security.identitydashboard.item.IdentityDashboardItem
import com.dashlane.security.identitydashboard.password.AuthentifiantSecurityEvaluator
import com.skocken.presentation.definition.Base
import kotlinx.coroutines.Deferred



interface IdentityDashboardContract {

    interface ViewProxy : Base.IView {
        fun setItems(items: List<IdentityDashboardItem>)
        fun remove(item: IdentityDashboardItem)
    }

    interface Presenter : Base.IPresenter {
        fun onViewVisible()
        fun onViewHidden()
        fun requireRefresh()
        fun onClick(item: IdentityDashboardItem)
    }

    interface DataProvider : Base.IDataProvider {
        fun hasProtectionPackage(): Boolean

        fun shouldIdentityRestorationBeVisible(): Boolean

        fun getAuthentifiantsSecurityInfoAsync(): Deferred<AuthentifiantSecurityEvaluator.Result?>

        fun listenForChanges()
        fun unlistenForChanges()

        suspend fun getCreditMonitoringLink(): String?
    }
}