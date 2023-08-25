package com.dashlane.item.subview.edit

import com.dashlane.vault.model.VaultItem

open class ItemEditValueTextSubView(
    var hint: String,
    override var value: String,
    val protected: Boolean,
    valueUpdate: (VaultItem<*>, String) -> VaultItem<*>?,
    val suggestions: List<String>? = null,
    val allowReveal: Boolean = true,
    val multiline: Boolean = false,
    val coloredCharacter: Boolean = false,
    val protectedStateListener: (Boolean) -> Unit = {}
) : ItemEditValueSubView<String>(valueUpdate) {
    var invisible: Boolean = false
}