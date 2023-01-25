package com.dashlane.item.header

import android.graphics.drawable.Drawable
import com.dashlane.item.subview.action.MenuAction

data class ItemHeader(val menuActions: List<MenuAction>, val title: String? = null, val image: Drawable? = null)