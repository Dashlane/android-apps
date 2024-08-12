package com.dashlane.ui.activities.fragments.vault

import android.os.Bundle
import androidx.annotation.StringRes
import com.dashlane.design.iconography.IconToken
import com.dashlane.design.theme.color.Mood
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.home.vaultlist.Filter
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
        fun onTeamspaceChange(teamspace: TeamSpace?)
        fun onSearchViewClicked()
        fun onFilterSelected(filter: Filter)
        fun onTabReselected(filter: Filter)
        fun onMenuAlertClicked()
        fun onTabClicked(filter: Filter)
    }

    interface View : Base.IView {
        fun setSelectedFilterTab(filter: Filter)
        fun getSelectedPosition(): Int
        fun showAnnouncement(iconToken: IconToken, title: String? = null, description: String, mood: Mood, onClick: () -> Unit = {})
        fun clearAnnouncements()
        fun showSnackbar(@StringRes stringRes: Int)
    }

    interface DataProvider : Base.IDataProvider {
        
    }
}