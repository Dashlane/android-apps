package com.dashlane.item.subview.edit

import com.dashlane.vault.model.VaultItem
import java.time.LocalDate



open class ItemEditValueDateSubView(
    val hint: String,
    override var value: LocalDate?,
    var formattedDate: String?,
    valueUpdate: (VaultItem<*>, LocalDate?) -> VaultItem<*>?
) : ItemEditValueSubView<LocalDate?>(valueUpdate) {
    var invisible: Boolean = false
}