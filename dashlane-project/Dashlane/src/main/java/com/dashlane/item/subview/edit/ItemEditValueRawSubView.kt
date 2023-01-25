package com.dashlane.item.subview.edit

import com.dashlane.vault.model.VaultItem



open class ItemEditValueRawSubView(
    val hint: String,
    override var value: String,
    val textSize: Float,
    valueUpdate: (VaultItem<*>, String) -> VaultItem<*>?
) : ItemEditValueSubView<String>(valueUpdate)