package com.dashlane.item.subview.provider.personalinfo

import android.content.Context
import com.dashlane.R
import com.dashlane.item.ItemEditViewContract
import com.dashlane.item.ScreenConfiguration
import com.dashlane.item.header.ItemHeader
import com.dashlane.item.subview.ItemScreenConfigurationProvider
import com.dashlane.item.subview.ItemSubView
import com.dashlane.item.subview.ItemSubViewWithActionWrapper
import com.dashlane.item.subview.action.CopyAction
import com.dashlane.item.subview.provider.DateTimeFieldFactory
import com.dashlane.item.subview.provider.SubViewFactory
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.DataCounter
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject



class ItemScreenConfigurationWebsiteProvider(
    private val teamspaceAccessor: TeamspaceAccessor,
    dataCounter: DataCounter,
    sessionManager: SessionManager,
    bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val dateTimeFieldFactory: DateTimeFieldFactory
) : ItemScreenConfigurationProvider(
    teamspaceAccessor, dataCounter,
    sessionManager, bySessionUsageLogRepository
) {

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
        val iconDrawable = createDefaultHeaderIcon(context, item.syncObject)
        val websiteTitle = context.getString(R.string.personal_website)
        return ItemHeader(createMenus(), websiteTitle, iconDrawable)
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
        return if (teamspaceAccessor.canChangeTeamspace()) {
            subViewFactory.createSpaceSelector(
                item.syncObject.spaceId, teamspaceAccessor, listOfNotNull(websiteView),
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
            context.getString(R.string.personal_website_hint_url), website,
            false, VaultItem<*>::copyForUpdatedWebsite
        )
        return if (websiteView == null || editMode) {
            websiteView
        } else {
            ItemSubViewWithActionWrapper(
                websiteView,
                CopyAction(item.toSummary(), CopyField.PersonalWebsite)
            )
        }
    }

    private fun createNameField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.PersonalWebsite>
    ): ItemSubView<*>? {
        return subViewFactory.createSubViewString(
            context.getString(R.string.personal_website_hint_name), item.syncObject.name,
            false,
            VaultItem<*>::copyForUpdatedName
        )
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedTeamspace(value: Teamspace): VaultItem<*> {
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
