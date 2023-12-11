package com.dashlane.ui.menu

import com.dashlane.ui.menu.domain.MenuItemModel

sealed class MenuState(open val items: List<MenuItemModel>) {
    object Init : MenuState(emptyList())
    data class Loaded(
        override val items: List<MenuItemModel>
    ) : MenuState(items = items)
}