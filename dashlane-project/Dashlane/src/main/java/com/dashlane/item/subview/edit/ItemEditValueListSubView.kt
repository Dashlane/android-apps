package com.dashlane.item.subview.edit

import com.dashlane.vault.model.VaultItem



class ItemEditValueListSubView(
    var title: String,
    override var value: String,
    var values: List<String>,
    valueUpdate: (VaultItem<*>, String) -> VaultItem<*>?
) : ItemEditValueSubView<String>(valueUpdate) {
    var invisible: Boolean = false
}