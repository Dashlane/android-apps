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
import com.dashlane.storage.userdata.EmailSuggestionProvider
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.model.getStringId
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject

class ItemScreenConfigurationEmailProvider(
    private val teamSpaceAccessor: TeamSpaceAccessor,
    private val emailSuggestionProvider: EmailSuggestionProvider,
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
        item as VaultItem<SyncObject.Email>
        return ScreenConfiguration(
            createSubViews(context, item, subViewFactory, canDelete, editMode, listener),
            createHeader(context, item)
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun hasEnoughDataToSave(itemToSave: VaultItem<*>): Boolean {
        itemToSave as VaultItem<SyncObject.Email>
        return itemToSave.syncObject.email?.trim().isNotSemanticallyNull()
    }

    private fun createHeader(
        context: Context,
        item: VaultItem<*>
    ): ItemHeader {
        val iconDrawable = createDefaultHeaderIcon(context, item.syncObject)
        val emailTitle = context.getString(R.string.email_address)
        return ItemHeader(createMenus(), emailTitle, iconDrawable)
    }

    private fun createSubViews(
        context: Context,
        item: VaultItem<SyncObject.Email>,
        subViewFactory: SubViewFactory,
        canDelete: Boolean,
        editMode: Boolean,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): List<ItemSubView<*>> {
        val emailView = createEmailField(subViewFactory, context, item, editMode)

        return listOfNotNull(
            
            createNameField(subViewFactory, context, item),
            
            createTypeField(context, item, subViewFactory),
            
            emailView,
            
            createTeamspaceField(subViewFactory, item, emailView),
            
            subViewFactory.createSubviewAttachmentDetails(context, item),
            
            dateTimeFieldFactory.createCreationDateField(editMode = editMode, context = context, item = item),
            
            dateTimeFieldFactory.createLatestUpdateDateField(editMode = editMode, context = context, item = item),
            
            subViewFactory.createSubviewDelete(context, listener, canDelete)
        )
    }

    private fun createTeamspaceField(
        subViewFactory: SubViewFactory,
        item: VaultItem<SyncObject.Email>,
        emailView: ItemSubView<String>?
    ): ItemSubView<*>? {
        return if (teamSpaceAccessor.canChangeTeamspace) {
            subViewFactory.createSpaceSelector(
                item.syncObject.spaceId,
                teamSpaceAccessor,
                listOfNotNull(emailView),
                VaultItem<*>::copyForUpdatedTeamspace
            )
        } else {
            null
        }
    }

    private fun createEmailField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.Email>,
        editMode: Boolean
    ): ItemSubView<String>? {
        val emailSuggestions = emailSuggestionProvider.getAllEmails()
        val email = item.syncObject.email
        val emailView = subViewFactory.createSubViewString(
            context.getString(R.string.email_hint_email),
            email,
            false,
            VaultItem<*>::copyForUpdatedEmail,
            emailSuggestions
        )
        return if (emailView == null || editMode) {
            emailView
        } else {
            ItemSubViewWithActionWrapper(
                emailView,
                CopyAction(
                    summaryObject = item.toSummary(),
                    copyField = CopyField.JustEmail,
                    vaultItemCopy = vaultItemCopy
                )
            )
        }
    }

    private fun createTypeField(
        context: Context,
        item: VaultItem<SyncObject.Email>,
        subViewFactory: SubViewFactory
    ): ItemSubView<*>? {

        if (teamSpaceAccessor.hasEnforcedTeamSpace) return null

        val allTypeCategories = listOf(SyncObject.Email.Type.PERSO, SyncObject.Email.Type.PRO)

        val typeUpdate = copyForUpdatedType(context, allTypeCategories)
        val selectedType = context.getString(item.syncObject.type.getStringId())

        return subViewFactory.createSubviewList(
            context.getString(R.string.type),
            selectedType,
            allTypeCategories.map { context.getString(it.getStringId()) },
            typeUpdate
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun copyForUpdatedType(
        context: Context,
        allTypeCategories: List<SyncObject.Email.Type>
    ): (Any, String) -> VaultItem<SyncObject.Email> {
        return { it, value ->
            val email = it as VaultItem<SyncObject.Email>
            val emailType = allTypeCategories.firstOrNull {
                it == email.syncObject.type
            }
            val emailTypeStringValue = emailType?.let { context.getString(it.getStringId()) } ?: ""
            if (value == emailTypeStringValue) {
                email
            } else {
                email.copySyncObject {
                    type = allTypeCategories.first {
                        context.getString(it.getStringId()) == value
                    }
                }
            }
        }
    }

    private fun createNameField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.Email>
    ): ItemSubView<*>? {
        return subViewFactory.createSubViewString(
            context.getString(R.string.email_hint_name),
            item.syncObject.emailName,
            false,
            VaultItem<*>::copyForUpdatedName
        )
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedTeamspace(value: TeamSpace): VaultItem<*> {
    this as VaultItem<SyncObject.Email>
    val email = this.syncObject
    return if (value.teamId == email.spaceId) {
        this
    } else {
        this.copyWithAttrs { teamSpaceId = value.teamId }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedEmail(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.Email>
    val lEmail = this.syncObject
    return if (value == lEmail.email.orEmpty()) {
        this
    } else {
        this.copySyncObject { email = value }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedName(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.Email>
    val email = this.syncObject
    return if (value == email.emailName.orEmpty()) {
        this
    } else {
        this.copySyncObject { emailName = value }
    }
}
