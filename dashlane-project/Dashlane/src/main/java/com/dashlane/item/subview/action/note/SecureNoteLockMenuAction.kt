package com.dashlane.item.subview.action.note

import android.app.Activity
import android.view.MenuItem
import com.dashlane.R
import com.dashlane.item.subview.action.ItemEditMenuAction
import com.dashlane.vault.model.VaultItem

class SecureNoteLockMenuAction(
    isSecured: Boolean,
    lockMenuAction: (Activity) -> Unit,
    lockMenuUpdate: (VaultItem<*>) -> VaultItem<*>?
) : ItemEditMenuAction(
    R.string.toolbar_menu_title_secure_note_lock,
    R.drawable.menu_item_lock_unlock,
        MenuItem.SHOW_AS_ACTION_ALWAYS,
    true,
    isSecured,
    lockMenuAction,
    lockMenuUpdate
)