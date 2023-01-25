package com.dashlane.item.subview.edit

import com.dashlane.vault.model.VaultItem



class ItemEditPasswordWithStrengthSubView(
    var title: String,
    override var value: String,
    valueUpdate: (VaultItem<*>, String) -> VaultItem<*>?,
    protectedStateListener: (Boolean) -> Unit = {}
) : ItemEditValueTextSubView(
    title, value, true, valueUpdate, coloredCharacter = true,
    protectedStateListener = protectedStateListener
)