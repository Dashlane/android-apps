package com.dashlane.item.subview.edit

import com.dashlane.item.subview.ItemSubViewImpl
import com.dashlane.item.subview.ValueChangeManager
import com.dashlane.item.subview.ValueChangeManagerImpl
import com.dashlane.vault.model.VaultItem

abstract class ItemEditValueSubView<T>(
    val valueUpdate: (VaultItem<*>, T) -> VaultItem<*>?,
    private val valueChangeManager: ValueChangeManager<T> = ValueChangeManagerImpl()
) : ItemSubViewImpl<T>(), ValueChangeManager<T> by valueChangeManager {

    fun updateValue(item: VaultItem<*>): VaultItem<*> {
        return valueUpdate(item, value) ?: item
    }

    override fun onItemValueHasChanged(previousValue: T, newValue: T) {
        super.onItemValueHasChanged(previousValue, newValue)
        valueChangeManager.notifyListeners(this, newValue)
    }
}