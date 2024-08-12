package com.dashlane.item.header

import com.dashlane.item.subview.action.MenuAction

data class ItemHeader(
    val menuActions: List<MenuAction>,
    val title: String? = null,
    val thumbnailType: Int? = null,
    val thumbnailUrl: String? = null,
    val thumbnailIconRes: Int? = null,
    val thumbnailColor: Int? = null,
)