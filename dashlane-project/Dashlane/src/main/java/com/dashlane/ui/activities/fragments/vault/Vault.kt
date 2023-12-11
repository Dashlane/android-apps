package com.dashlane.ui.activities.fragments.vault

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import com.dashlane.teamspaces.model.Teamspace
import com.skocken.presentation.definition.Base
import kotlinx.coroutines.flow.StateFlow

interface Vault {
    interface Presenter : Base.IPresenter {
        val filter: StateFlow<Filter>
        fun onCreate(arguments: Bundle?, savedInstanceState: Bundle?)
        fun onSaveInstanceState(outState: Bundle)
        fun onStartFragment()
        fun onStopFragment()
        fun onResumeFragment()
        fun onTeamspaceChange(teamspace: Teamspace?)
        fun onSearchViewClicked()
        fun onFilterSelected(filter: Filter)
        fun onTabReselected(filter: Filter)
        fun onMenuAlertClicked()
        fun onTabClicked(filter: Filter)
    }

    interface View : Base.IView {
        fun setSelectedFilterTab(filter: Filter)
        fun getSelectedPosition(): Int
        fun showAnnouncement(@LayoutRes layout: Int?, onClick: () -> Unit = {})
        fun showSnackbar(@StringRes stringRes: Int)
    }

    interface DataProvider : Base.IDataProvider {
        fun subscribeTeamspaceManager()
        fun unsubscribeTeamspaceManager()
    }
}