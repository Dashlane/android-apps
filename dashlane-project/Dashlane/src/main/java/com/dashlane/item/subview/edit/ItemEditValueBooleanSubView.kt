package com.dashlane.item.subview.edit

import com.dashlane.vault.model.VaultItem



class ItemEditValueBooleanSubView(
    val header: String,
    val description: String?,
    override var value: Boolean,
    valueUpdate: (VaultItem<*>, Boolean) -> VaultItem<*>?
) : ItemEditValueSubView<Boolean>(valueUpdate)