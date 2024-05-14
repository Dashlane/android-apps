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
import com.dashlane.item.subview.readonly.EmptyLinkedServicesSubView
import com.dashlane.item.subview.readonly.ItemLinkedServicesSubView
import com.dashlane.ui.VaultItemImageHelper
import com.dashlane.util.graphics.RoundRectDrawable
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObject.Authentifiant.LinkedServices
import com.dashlane.xml.domain.SyncObject.Authentifiant.LinkedServices.AssociatedAndroidApps
import com.dashlane.xml.domain.SyncObject.Authentifiant.LinkedServices.AssociatedDomains

abstract class ItemScreenConfigurationProvider {
    abstract fun createScreenConfiguration(
        context: Context,
        item: VaultItem<*>,
        subViewFactory: SubViewFactory,
        editMode: Boolean,
        canDelete: Boolean,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): ScreenConfiguration

    fun hasEnoughDataToSave(
        itemToSave: VaultItem<*>,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): Boolean {
        if (!hasEnoughDataToSave(itemToSave)) {
            when (itemToSave.syncObject) {
                is SyncObject.Authentifiant -> listener.notifyNotEnoughDataToSave(R.string.error_credential_must_have_at_least)
                else -> listener.notifyNotEnoughDataToSave(R.string.error_cannot_add_empty_item)
            }
            return false
        }
        return true
    }

    open fun gatherFromUi(
        item: VaultItem<*>,
        subViews: List<ItemSubView<*>>,
        header: ItemHeader?
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

    open fun gatherCollectionsFromUi(collectionSubView: ItemCollectionListSubView?) =
        collectionSubView?.value?.value ?: emptyList()

    private fun updateItemFromSubView(updatedItem: VaultItem<*>, subView: ItemSubView<*>): VaultItem<*>? {
        return when (subView) {
            is ItemEditValueSubView -> subView.updateValue(updatedItem)
            is ItemLinkedServicesSubView -> updateLinkedServices(
                updatedItem,
                subView.linkedWebsites,
                subView.linkedApps
            )

            is EmptyLinkedServicesSubView -> updateLinkedServices(updatedItem)
            is ItemSubViewWithActionWrapper -> updateItemFromSubView(
                updatedItem,
                subView.itemSubView
            )
            else -> null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun updateLinkedServices(
        item: VaultItem<*>,
        temporaryWebsites: List<AssociatedDomains>? = null,
        temporaryApps: List<AssociatedAndroidApps>? = null
    ): VaultItem<SyncObject.Authentifiant>? {
        item as? VaultItem<SyncObject.Authentifiant> ?: return null
        val linkedServices = if (temporaryApps == null && temporaryWebsites == null) {
            null
        } else {
            LinkedServices(temporaryApps, temporaryWebsites)
        }
        return when {
            linkedServices == null && (
                item.syncObject.linkedServices == null ||
                    item.syncObject.linkedServices == LinkedServices(null, null) ||
                    item.syncObject.linkedServices == LinkedServices(null, emptyList())
                ) -> item
            item.syncObject.linkedServices == linkedServices -> item
            else -> item.copySyncObject { this.linkedServices = linkedServices }
        }
    }

    open fun onSetupEnd(
        context: Context,
        item: VaultItem<*>,
        editMode: Boolean,
        listener: ItemEditViewContract.View.UiUpdateListener,
        hasPreviousState: Boolean
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

    fun createDefaultHeaderIcon(
        context: Context,
        item: SyncObject
    ): RoundRectDrawable? {
        return VaultItemImageHelper.getIconDrawableFromSyncObject(context, item)
    }
}