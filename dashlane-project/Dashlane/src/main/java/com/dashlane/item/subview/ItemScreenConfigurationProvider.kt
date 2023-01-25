package com.dashlane.item.subview

import android.content.Context
import android.os.Bundle
import androidx.core.os.bundleOf
import com.dashlane.R
import com.dashlane.item.ItemEditViewContract
import com.dashlane.item.ScreenConfiguration
import com.dashlane.item.header.ItemHeader
import com.dashlane.item.logger.BaseLogger
import com.dashlane.item.logger.Logger
import com.dashlane.item.subview.action.ItemEditMenuAction
import com.dashlane.item.subview.action.MenuAction
import com.dashlane.item.subview.edit.ItemEditValueSubView
import com.dashlane.item.subview.provider.SubViewFactory
import com.dashlane.item.subview.readonly.EmptyLinkedServicesSubView
import com.dashlane.item.subview.readonly.ItemLinkedServicesSubView
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.DataCounter
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.ui.VaultItemImageHelper
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.util.graphics.RoundRectDrawable
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType



abstract class ItemScreenConfigurationProvider(
    teamspaceAccessor: TeamspaceAccessor,
    dataCounter: DataCounter,
    sessionManager: SessionManager,
    bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) {

    

    open val logger: Logger = BaseLogger(teamspaceAccessor, dataCounter, sessionManager, bySessionUsageLogRepository)

    

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

    @Suppress("UNCHECKED_CAST")
    private fun updateItemFromSubView(updatedItem: VaultItem<*>, subView: ItemSubView<*>): VaultItem<*>? {
        return when (subView) {
            is ItemEditValueSubView -> subView.updateValue(updatedItem)
            is ItemLinkedServicesSubView -> updateLinkedServices(
                updatedItem,
                subView.linkedWebsites,
                subView.linkedApps
            )
            is EmptyLinkedServicesSubView -> updateLinkedServices(updatedItem, emptyList(), emptyList())
            is ItemSubViewWithActionWrapper -> updateItemFromSubView(updatedItem, subView.itemSubView)
            else -> null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun updateLinkedServices(
        item: VaultItem<*>,
        temporaryWebsites: List<SyncObject.Authentifiant.LinkedServices.AssociatedDomains>?,
        temporaryApps: List<SyncObject.Authentifiant.LinkedServices.AssociatedAndroidApps>?,
    ): VaultItem<SyncObject.Authentifiant>? {
        if (item.syncObjectType == SyncObjectType.AUTHENTIFIANT) {
            item as VaultItem<SyncObject.Authentifiant>
            val linkedServices = SyncObject.Authentifiant.LinkedServices(temporaryApps, temporaryWebsites)
            return item.copySyncObject {
                this.linkedServices = linkedServices
            }
        }
        return null
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