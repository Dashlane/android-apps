package com.dashlane.ui.menu

import com.dashlane.navigation.Navigator
import com.dashlane.ui.menu.header.MenuHeaderItem
import com.dashlane.ui.menu.teamspace.TeamspaceAdapterItem
import com.skocken.presentation.presenter.BasePresenter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuPresenter @Inject constructor(
    navigator: Navigator,
) : BasePresenter<MenuDef.IDataProvider?, MenuDef.IView?>(), MenuDef.IPresenter {
    val headerItem = MenuHeaderItem()
    private val menuItemProvider = MenuItemProvider(navigator)

    override var mode: Int = MenuDef.Mode.DEFAULT
        set(value) {
            if (field == value) return
            field = value
            refreshMenuList()
        }

    override fun onTeamspaceSelected(item: TeamspaceAdapterItem) {
        provider.onTeamspaceSelected(item.teamspace)
        mode = MenuDef.Mode.DEFAULT
        refreshMenuList()
    }

    override fun refreshMenuList() {
        val items: MutableList<MenuDef.Item> = ArrayList()
        if (mode == MenuDef.Mode.TEAMSPACE) {
            fillItemsModeTeamspace(items)
        } else if (mode == MenuDef.Mode.DEFAULT) {
            fillItemsModeDefault(items)
        }
        view.setItems(items)
    }

    private fun fillItemsModeTeamspace(items: MutableList<MenuDef.Item>) {
        val provider = provider
        items.add(headerItem)
        val teamspaces = provider.teamspaces
        if (teamspaces == null || teamspaces.size == 0) {
            mode = MenuDef.Mode.DEFAULT
        } else {
            for (teamspace in teamspaces) {
                items.add(TeamspaceAdapterItem(teamspace))
            }
        }
    }

    private fun fillItemsModeDefault(items: MutableList<MenuDef.Item>) {
        val provider = provider
        items.add(headerItem)
        items.addAll(menuItemProvider.fullMenu!!)
        if (!provider.isVPNVisible) {
            items.remove(menuItemProvider.menuItemVpn)
        }
        if (!provider.isPersonalPlanVisible) {
            items.remove(menuItemProvider.menuItemPersonalPlan)
        }
    }
}