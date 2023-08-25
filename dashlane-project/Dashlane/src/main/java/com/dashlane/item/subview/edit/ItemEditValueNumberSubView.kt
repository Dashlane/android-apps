package com.dashlane.item.subview.edit

import com.dashlane.item.subview.provider.SubViewFactory
import com.dashlane.vault.model.VaultItem

open class ItemEditValueNumberSubView(
    val hint: String,
    override var value: String,
    val protected: Boolean,
    @SubViewFactory.TYPE val inputType: Int,
    valueUpdate: (VaultItem<*>, String) -> VaultItem<*>?,
    val suggestions: List<String>? = null,
    val protectedStateListener: (Boolean) -> Unit = {}
) : ItemEditValueSubView<String>(valueUpdate) {
    var invisible: Boolean = false
}