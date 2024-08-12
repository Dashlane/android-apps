package com.dashlane.item.subview.action

import android.app.Activity
import android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM
import com.dashlane.R

class ItemHistoryAction(
    enabled: Boolean = true,
    action: (Activity) -> Unit = {},
) : MenuAction(
    text = R.string.password_history_menu_action_title,
    icon = R.drawable.ic_history,
    displayFlags = SHOW_AS_ACTION_IF_ROOM,
    checkable = false,
    checked = false,
    enabled = enabled,
    action = action
)
