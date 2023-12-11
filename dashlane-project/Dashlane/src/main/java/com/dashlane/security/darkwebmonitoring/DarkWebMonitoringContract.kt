package com.dashlane.security.darkwebmonitoring

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.dashlane.darkweb.DarkWebEmailStatus
import com.dashlane.security.darkwebmonitoring.item.DarkWebBreachItem
import com.dashlane.security.darkwebmonitoring.item.DarkWebEmailItem
import com.dashlane.security.identitydashboard.breach.BreachWrapper
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.skocken.presentation.definition.Base

interface DarkWebMonitoringContract {

    interface ViewProxy : Base.IView {
        fun setItems(
            pendingItems: List<DashlaneRecyclerAdapter.ViewTypeProvider>,
            resolvedItems: List<DashlaneRecyclerAdapter.ViewTypeProvider>,
            emails: List<DarkWebEmailItem>
        )

        fun showDarkwebInactiveScene()
        fun goToPendingTab()
        fun updateActionBar(updateTitle: Boolean)
        fun showDeleteCompleted(breachesDeleted: Int)
    }

    interface Presenter : Base.IPresenter {
        var selectedItems: MutableList<DarkWebBreachItem>

        fun onViewVisible()
        fun onViewHidden()
        fun requireRefresh()
        fun onClick(item: DashlaneRecyclerAdapter.ViewTypeProvider)
        fun onInactiveDarkwebCtaClick()
        fun onAddDarkWebEmailClick()
        fun onCreateOptionsMenu(inflater: MenuInflater, menu: Menu)
        fun onOptionsItemSelected(item: MenuItem): Boolean
    }

    interface DataProvider : Base.IDataProvider {
        suspend fun getDarkwebBreaches(): List<BreachWrapper>
        suspend fun getDarkwebEmailStatuses(): List<DarkWebEmailStatus>?
        fun listenForChanges()
        fun unlistenForChanges()
        suspend fun unlistenDarkWeb(email: String)
        suspend fun deleteBreaches(breaches: List<BreachWrapper>)
    }
}