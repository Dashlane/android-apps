package com.dashlane.item.subview.action

import android.view.MenuItem
import com.dashlane.R
import com.dashlane.item.ItemEditViewContract

class DeleteMenuAction(uiListener: ItemEditViewContract.View.UiUpdateListener) : MenuAction(
    R.string.delete,
    R
    .drawable.action_bar_menu_delete,
    MenuItem.SHOW_AS_ACTION_NEVER,
    action = {
        uiListener.notifyDeleteClicked()
    }
)