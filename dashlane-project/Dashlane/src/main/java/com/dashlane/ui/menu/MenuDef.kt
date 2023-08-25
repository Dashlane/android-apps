package com.dashlane.ui.menu

import androidx.annotation.IntDef
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter.ViewTypeProvider
import com.dashlane.ui.menu.teamspace.TeamspaceAdapterItem
import com.skocken.presentation.definition.Base

interface MenuDef {
    @IntDef(Mode.DEFAULT, Mode.TEAMSPACE)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Mode {
        companion object {
            const val DEFAULT = 0
            const val TEAMSPACE = 1
        }
    }

    interface IPresenter : Base.IPresenter {
        @Mode
        var mode: Int
        fun onTeamspaceSelected(item: TeamspaceAdapterItem)
        fun refreshMenuList()
    }

    interface IDataProvider : Base.IDataProvider {
        val teamspaces: List<Teamspace?>?
        fun onTeamspaceSelected(teamspace: Teamspace?)
        val isVPNVisible: Boolean
        val menuUsageLogger: MenuUsageLogger?
        val isPersonalPlanVisible: Boolean
    }

    interface IView : Base.IView {
        fun setItems(items: List<Item>)
    }

    interface Item : ViewTypeProvider {
        fun doNavigation(menuUsageLogger: MenuUsageLogger)
    }
}