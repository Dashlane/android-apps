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
import com.dashlane.item.subview.edit.ItemEditValueListSubView
import com.dashlane.item.subview.provider.DateTimeFieldFactory
import com.dashlane.item.subview.provider.SubViewFactory
import com.dashlane.item.subview.provider.createCountryField
import com.dashlane.item.subview.readonly.ItemReadValueListSubView
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.model.getStringId
import com.dashlane.vault.model.isNotSemanticallyNull
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject

class ItemScreenConfigurationPhoneProvider(
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
        item as VaultItem<SyncObject.Phone>
        return ScreenConfiguration(
            createSubViews(context, item, subViewFactory, editMode, canDelete, listener),
            createHeader(context, item)
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun hasEnoughDataToSave(itemToSave: VaultItem<*>): Boolean {
        itemToSave as VaultItem<SyncObject.Phone>
        return itemToSave.syncObject.number?.trim().isNotSemanticallyNull()
    }

    private fun createHeader(
        context: Context,
        item: VaultItem<*>
    ): ItemHeader {
        val phoneTitle = context.getString(R.string.phone)
        return ItemHeader(
            menuActions = createMenus(),
            title = phoneTitle,
            thumbnailType = ThumbnailViewType.VAULT_ITEM_OTHER_ICON.value,
            thumbnailIconRes = getHeaderIcon(item.syncObject),
        )
    }

    private fun createSubViews(
        context: Context,
        item: VaultItem<SyncObject.Phone>,
        subViewFactory: SubViewFactory,
        editMode: Boolean,
        canDelete: Boolean,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): List<ItemSubView<*>> {
        return listOfNotNull(
            
            createNameField(subViewFactory, context, item),
            
            createTypeField(context, item, editMode),
            
            createCountryField(context, item, editMode),
            
            createPhoneNumberField(subViewFactory, context, item, editMode),
            
            createTeamspaceField(subViewFactory, item),
            
            subViewFactory.createSubviewAttachmentDetails(context, item),
            
            dateTimeFieldFactory.createCreationDateField(editMode = editMode, context = context, item = item),
            
            dateTimeFieldFactory.createLatestUpdateDateField(editMode = editMode, context = context, item = item),
            
            subViewFactory.createSubviewDelete(context, listener, canDelete)
        )
    }

    private fun createTeamspaceField(
        subViewFactory: SubViewFactory,
        item: VaultItem<SyncObject.Phone>
    ): ItemSubView<*>? {
        return if (teamSpaceAccessor.canChangeTeamspace) {
            val spaceUpdate = VaultItem<*>::copyForUpdatedTeamspace
            subViewFactory.createSpaceSelector(item.syncObject.spaceId, teamSpaceAccessor, null, spaceUpdate)
        } else {
            null
        }
    }

    private fun createPhoneNumberField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.Phone>,
        editMode: Boolean
    ): ItemSubView<*>? {
        val number = item.syncObject.number
        val numberView = subViewFactory.createSubViewNumber(
            context.getString(R.string.phone_hint_number),
            number,
            SubViewFactory.INPUT_TYPE_PHONE,
            false,
            VaultItem<*>::copyForUpdatedPhoneNumber
        )
        return if (numberView == null || editMode) {
            numberView
        } else {
            ItemSubViewWithActionWrapper(
                numberView,
                CopyAction(
                    summaryObject = item.toSummary(),
                    copyField = CopyField.PhoneNumber,
                    action = {},
                    vaultItemCopy = vaultItemCopy
                )
            )
        }
    }

    private fun createTypeField(
        context: Context,
        item: VaultItem<SyncObject.Phone>,
        editMode: Boolean
    ): ItemSubView<*> {
        val phoneTypeHeader = context.getString(R.string.type)
        val phoneTypeList = SyncObject.Phone.Type.values().map { context.getString(it.getStringId()) }
        val selectedType = (
            item.syncObject.type
                ?: SyncObject.Phone.Type.PHONE_TYPE_MOBILE
            ).let { context.getString(it.getStringId()) }
        return when {
            editMode -> ItemEditValueListSubView(
                phoneTypeHeader,
                selectedType,
                phoneTypeList
            ) { it, value -> it.copyForUpdatedType(context, value) }
            else -> ItemReadValueListSubView(phoneTypeHeader, selectedType, phoneTypeList)
        }
    }

    private fun createNameField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.Phone>
    ): ItemSubView<*>? {
        return subViewFactory.createSubViewString(
            context.getString(R.string.phone_hint_name),
            item.syncObject.phoneName,
            false,
            VaultItem<*>::copyForUpdatedName
        )
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedPhoneNumber(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.Phone>
    val phone = this.syncObject
    return if (value == phone.number.orEmpty()) {
        this
    } else {
        this.copySyncObject { number = value }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedTeamspace(value: TeamSpace): VaultItem<*> {
    this as VaultItem<SyncObject.Phone>
    val phone = this.syncObject
    return if (value.teamId == phone.spaceId) {
        this
    } else {
        this.copyWithAttrs { teamSpaceId = value.teamId }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedType(context: Context, value: String): VaultItem<*> {
    this as VaultItem<SyncObject.Phone>
    val phone = this.syncObject.type
    return if (value == (
            phone
                ?: SyncObject.Phone.Type.PHONE_TYPE_MOBILE
            ).let { context.getString(it.getStringId()) }
    ) {
        this
    } else {
        val lPhoneType = SyncObject.Phone.Type.values().firstOrNull { value == context.getString(it.getStringId()) }
        this.copySyncObject { type = lPhoneType }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedName(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.Phone>
    val phone = this.syncObject
    return if (value == phone.phoneName.orEmpty()) {
        this
    } else {
        this.copySyncObject { phoneName = value }
    }
}
