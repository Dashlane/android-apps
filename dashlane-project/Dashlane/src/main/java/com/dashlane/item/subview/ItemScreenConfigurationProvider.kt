package com.dashlane.item.subview

import android.content.Context
import android.os.Bundle
import androidx.core.os.bundleOf
import com.dashlane.R
import com.dashlane.item.ItemEditViewContract
import com.dashlane.item.ScreenConfiguration
import com.dashlane.item.header.ItemHeader
import com.dashlane.item.subview.action.ItemEditMenuAction
import com.dashlane.item.subview.action.MenuAction
import com.dashlane.item.subview.edit.ItemEditValueSubView
import com.dashlane.item.subview.provider.SubViewFactory
import com.dashlane.ui.VaultItemImageHelper
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.objectType

abstract class ItemScreenConfigurationProvider {
    abstract fun createScreenConfiguration(
        context: Context,
        item: VaultItem<*>,
        subViewFactory: SubViewFactory,
        editMode: Boolean,
        canDelete: Boolean,
        listener: ItemEditViewContract.View.UiUpdateListener,
    ): ScreenConfiguration

    fun hasEnoughDataToSave(
        itemToSave: VaultItem<*>,
        listener: ItemEditViewContract.View.UiUpdateListener,
    ): Boolean {
        if (!hasEnoughDataToSave(itemToSave)) {
            listener.notifyNotEnoughDataToSave(R.string.error_cannot_add_empty_item)
            return false
        }
        return true
    }

    open fun gatherFromUi(
        item: VaultItem<*>,
        subViews: List<ItemSubView<*>>,
        header: ItemHeader?,
    ): VaultItem<*> {
        var updatedItem = item
        subViews.forEach { subView ->
            updateItemFromSubView(updatedItem, subView)?.let {
                updatedItem = it
            }
        }
        header?.menuActions?.filterIsInstance<ItemEditMenuAction>()?.forEach {
            updatedItem = it.updateValue(updatedItem)
        }
        return updatedItem
    }

    private fun updateItemFromSubView(updatedItem: VaultItem<*>, subView: ItemSubView<*>): VaultItem<*>? {
        return when (subView) {
            is ItemEditValueSubView -> subView.updateValue(updatedItem)
            is ItemSubViewWithActionWrapper -> updateItemFromSubView(
                updatedItem,
                subView.itemSubView,
            )
            else -> null
        }
    }

    open fun onSetupEnd(
        context: Context,
        item: VaultItem<*>,
        editMode: Boolean,
        listener: ItemEditViewContract.View.UiUpdateListener,
        hasPreviousState: Boolean,
    ) {
        
    }

    protected open fun hasEnoughDataToSave(itemToSave: VaultItem<*>): Boolean {
        return true
    }

    open fun saveAdditionalData() = bundleOf()

    open fun restoreAdditionalData(data: Bundle) = Unit 

    fun createMenus(): List<MenuAction> {
        return mutableListOf()
    }

    fun getHeaderIcon(
        item: SyncObject,
    ): Int? = with(VaultItemImageHelper) { item.objectType.getThumbnailIcon() }
}