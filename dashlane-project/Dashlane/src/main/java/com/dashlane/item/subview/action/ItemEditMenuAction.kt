package com.dashlane.item.subview.action

import android.app.Activity
import com.dashlane.vault.model.VaultItem

open class ItemEditMenuAction(
    title: Int,
    icon: Int,
    displayFlags: Int,
    checkable: Boolean = false,
    checked: Boolean = false,
    action: (Activity) -> Unit = {},
    val valueUpdate: (VaultItem<*>) -> VaultItem<*>?
) : MenuAction(title, icon, displayFlags, checkable, checked, action) {

    fun updateValue(item: VaultItem<*>): VaultItem<*> {
        return valueUpdate(item) ?: item
    }
}