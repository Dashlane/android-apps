package com.dashlane.item.subview.action

import android.app.Activity
import android.view.MenuItem
import com.dashlane.R

class ShowAttachmentsMenuAction(
    enabled: Boolean = true,
    action: (Activity) -> Unit
) : MenuAction(
    text = R.string.attachments_activity_title,
    icon = R.drawable.action_bar_menu_attach_file,
    displayFlags = MenuItem.SHOW_AS_ACTION_ALWAYS,
    enabled = enabled,
    action = action
)