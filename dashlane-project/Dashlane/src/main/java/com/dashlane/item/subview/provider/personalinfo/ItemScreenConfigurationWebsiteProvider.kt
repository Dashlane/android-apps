package com.dashlane.item.subview.provider.personalinfo

import android.content.Context
import com.dashlane.R
import com.dashlane.design.component.compat.view.ThumbnailViewType
import com.dashlane.item.ItemEditViewContract
import com.dashlane.item.ScreenConfiguration
import com.dashlane.item.header.ItemHeader
import com.dashlane.item.subview.ItemScreenConfigurationProvider
import com.dashlane.item.subview.ItemSubView
import com.dashlane.item.subview.ItemSubViewWithActionWrapper
import com.dashlane.item.subview.action.CopyAction
import com.dashlane.item.subview.provider.DateTimeFieldFactory
import com.dashlane.item.subview.provider.SubViewFactory
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject

class ItemScreenConfigurationWebsiteProvider(
    private val teamSpaceAccessor: TeamSpaceAccessor,
    private val dateTimeFieldFactory: DateTimeFieldFactory,
    private val vaultItemCopy: VaultItemCopyService
) : ItemScreenConfigurationProvider() {

    @Suppress("UNCHECKED_CAST")
    override fun createScreenConfiguration(
        context: Context,
        item: VaultItem<*>,
        subViewFactory: SubViewFactory,
        editMode: Boolean,
        canDelete: Boolean,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): ScreenConfiguration {
        item as VaultItem<SyncObject.PersonalWebsite>
        return ScreenConfiguration(
            createSubViews(context, item, subViewFactory, canDelete, editMode, listener),
            createHeader(context, item)
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun hasEnoughDataToSave(itemToSave: VaultItem<*>): Boolean {
        itemToSave as VaultItem<SyncObject.PersonalWebsite>
        return itemToSave.syncObject.website?.trim().isNotSemanticallyNull()
    }

    private fun createHeader(
        context: Context,
        item: VaultItem<*>
    ): ItemHeader {
        val websiteTitle = context.getString(R.string.personal_website)
        return ItemHeader(
            menuActions = createMenus(),
            title = websiteTitle,
            thumbnailType = ThumbnailViewType.VAULT_ITEM_DOMAIN_ICON.value,
            thumbnailIconRes = getHeaderIcon(item.syncObject),
        )
    }

    private fun createSubViews(
        context: Context,
        item: VaultItem<SyncObject.PersonalWebsite>,
        subViewFactory: SubViewFactory,
        canDelete: Boolean,
        editMode: Boolean,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): List<ItemSubView<*>> {
        val websiteView = createWebsiteField(subViewFactory, context, item, editMode)
        return listOfNotNull(
            
            createNameField(subViewFactory, context, item),
            
            websiteView,
            
            createTeamspaceField(subViewFactory, item, websiteView),
            
            subViewFactory.createSubviewAttachmentDetails(context, item),
            
            dateTimeFieldFactory.createCreationDateField(editMode = editMode, context = context, item = item),
            
            dateTimeFieldFactory.createLatestUpdateDateField(editMode = editMode, context = context, item = item),
            
            subViewFactory.createSubviewDelete(context, listener, canDelete)
        )
    }

    private fun createTeamspaceField(
        subViewFactory: SubViewFactory,
        item: VaultItem<SyncObject.PersonalWebsite>,
        websiteView: ItemSubView<String>?
    ): ItemSubView<*>? {
        return if (teamSpaceAccessor.canChangeTeamspace) {
            subViewFactory.createSpaceSelector(
                item.syncObject.spaceId,
                teamSpaceAccessor,
                listOfNotNull(websiteView),
                VaultItem<*>::copyForUpdatedTeamspace
            )
        } else {
            null
        }
    }

    private fun createWebsiteField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.PersonalWebsite>,
        editMode: Boolean
    ): ItemSubView<String>? {
        val website = item.syncObject.website
        val websiteView = subViewFactory.createSubViewString(
            context.getString(R.string.personal_website_hint_url),
            website,
            false,
            VaultItem<*>::copyForUpdatedWebsite
        )
        return if (websiteView == null || editMode) {
            websiteView
        } else {
            ItemSubViewWithActionWrapper(
                websiteView,
                CopyAction(
                    summaryObject = item.toSummary(),
                    copyField = CopyField.PersonalWebsite,
                    vaultItemCopy = vaultItemCopy
                )
            )
        }
    }

    private fun createNameField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.PersonalWebsite>
    ): ItemSubView<*>? {
        return subViewFactory.createSubViewString(
            context.getString(R.string.personal_website_hint_name),
            item.syncObject.name,
            false,
            VaultItem<*>::copyForUpdatedName
        )
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedTeamspace(value: TeamSpace): VaultItem<*> {
    this as VaultItem<SyncObject.PersonalWebsite>
    val website = this.syncObject
    return if (value.teamId == website.spaceId) {
        this
    } else {
        this.copyWithAttrs { teamSpaceId = value.teamId }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedName(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.PersonalWebsite>
    val website = this.syncObject
    return if (value == website.name.orEmpty()) {
        this
    } else {
        this.copySyncObject { name = value }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedWebsite(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.PersonalWebsite>
    val lWebsite = this.syncObject
    return if (value == lWebsite.website.orEmpty()) {
        this
    } else {
        this.copySyncObject { website = value }
    }
}
