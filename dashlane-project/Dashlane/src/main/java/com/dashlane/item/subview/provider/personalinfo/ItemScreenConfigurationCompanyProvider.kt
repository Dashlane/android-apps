package com.dashlane.item.subview.provider.personalinfo

import android.content.Context
import com.dashlane.R
import com.dashlane.design.component.compat.view.ThumbnailViewType
import com.dashlane.item.ItemEditViewContract
import com.dashlane.item.ScreenConfiguration
import com.dashlane.item.header.ItemHeader
import com.dashlane.item.subview.ItemScreenConfigurationProvider
import com.dashlane.item.subview.ItemSubView
import com.dashlane.item.subview.provider.DateTimeFieldFactory
import com.dashlane.item.subview.provider.SubViewFactory
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.xml.domain.SyncObject

class ItemScreenConfigurationCompanyProvider(
    private val teamSpaceAccessor: TeamSpaceAccessor,
    private val dateTimeFieldFactory: DateTimeFieldFactory
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
        item as VaultItem<SyncObject.Company>
        return ScreenConfiguration(
            createSubViews(
                context = context,
                item = item,
                subViewFactory = subViewFactory,
                canDelete = canDelete,
                listener = listener,
                editMode = editMode
            ),
            createHeader(context, item)
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun hasEnoughDataToSave(itemToSave: VaultItem<*>): Boolean {
        itemToSave as VaultItem<SyncObject.Company>
        return itemToSave.syncObject.jobTitle?.trim().isNotSemanticallyNull() ||
            itemToSave.syncObject.name?.trim().isNotSemanticallyNull()
    }

    private fun createHeader(
        context: Context,
        item: VaultItem<*>
    ): ItemHeader {
        val companyTitle = context.getString(R.string.company)
        return ItemHeader(
            menuActions = createMenus(),
            title = companyTitle,
            thumbnailType = ThumbnailViewType.VAULT_ITEM_OTHER_ICON.value,
            thumbnailIconRes = getHeaderIcon(item.syncObject),
        )
    }

    private fun createSubViews(
        context: Context,
        item: VaultItem<SyncObject.Company>,
        subViewFactory: SubViewFactory,
        canDelete: Boolean,
        listener: ItemEditViewContract.View.UiUpdateListener,
        editMode: Boolean
    ): List<ItemSubView<*>> {
        return listOfNotNull(
            
            createCompanyNameField(subViewFactory, context, item),
            
            createJobTitleField(subViewFactory, context, item),
            
            createTeamspaceField(subViewFactory, item),
            
            subViewFactory.createSubviewAttachmentDetails(context, item),
            
            dateTimeFieldFactory.createCreationDateField(editMode = editMode, context = context, item = item),
            
            dateTimeFieldFactory.createLatestUpdateDateField(editMode = editMode, context = context, item = item),
            
            subViewFactory.createSubviewDelete(context, listener, canDelete)
        )
    }

    private fun createTeamspaceField(
        subViewFactory: SubViewFactory,
        item: VaultItem<SyncObject.Company>
    ): ItemSubView<*>? {
        return if (teamSpaceAccessor.canChangeTeamspace) {
            subViewFactory.createSpaceSelector(
                item.syncObject.spaceId,
                teamSpaceAccessor,
                null,
                VaultItem<*>::copyForUpdatedTeamspace
            )
        } else {
            null
        }
    }

    private fun createJobTitleField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.Company>
    ): ItemSubView<*>? {
        return subViewFactory.createSubViewString(
            context.getString(R.string.company_hint_job),
            item.syncObject.jobTitle,
            false,
            VaultItem<*>::copyForUpdatedTitle
        )
    }

    private fun createCompanyNameField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.Company>
    ): ItemSubView<*>? {
        return subViewFactory.createSubViewString(
            context.getString(R.string.company_hint_company),
            item.syncObject.name,
            false,
            VaultItem<*>::copyForUpdatedName
        )
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedTeamspace(value: TeamSpace): VaultItem<*> {
    this as VaultItem<SyncObject.Company>
    val company = this.syncObject
    return if (value.teamId == company.spaceId) {
        this
    } else {
        this.copyWithAttrs { teamSpaceId = value.teamId }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedTitle(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.Company>
    val company = this.syncObject
    return if (value == company.jobTitle.orEmpty()) {
        this
    } else {
        this.copySyncObject { jobTitle = value }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedName(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.Company>
    val company = this.syncObject
    return if (value == company.name.orEmpty()) {
        this
    } else {
        this.copySyncObject { name = value }
    }
}
