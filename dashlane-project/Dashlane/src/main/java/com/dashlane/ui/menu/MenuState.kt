package com.dashlane.ui.menu

import com.dashlane.ui.menu.domain.MenuItemModel

sealed class MenuState(
    open val items: List<MenuItemModel>,
    open val hasUnReadActionItems: Boolean = false,
) {
    object Init : MenuState(emptyList())

    data class Loaded(
        override val items: List<MenuItemModel>,
        override val hasUnReadActionItems: Boolean = false,
    ) : MenuState(items = items)
}