package com.dashlane.item.subview.provider.id

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
import com.dashlane.item.subview.provider.createCountryField
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject
import java.time.LocalDate

class ItemScreenConfigurationIdCardProvider(
    private val teamspaceAccessor: TeamSpaceAccessor,
    private val genericDataQuery: GenericDataQuery,
    private val vaultDataQuery: VaultDataQuery,
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
        item as VaultItem<SyncObject.IdCard>
        return ScreenConfiguration(
            createSubViews(context, item, subViewFactory, editMode, canDelete, listener),
            createHeader(context, item)
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun hasEnoughDataToSave(itemToSave: VaultItem<*>): Boolean {
        itemToSave as VaultItem<SyncObject.IdCard>
        return itemToSave.syncObject.number?.trim().isNotSemanticallyNull()
    }

    private fun createHeader(
        context: Context,
        item: VaultItem<*>
    ): ItemHeader {
        val title = context.getString(R.string.id_card)
        return ItemHeader(
            menuActions = createMenus(),
            title = title,
            thumbnailType = ThumbnailViewType.VAULT_ITEM_OTHER_ICON.value,
            thumbnailIconRes = getHeaderIcon(item.syncObject),
        )
    }

    private fun createSubViews(
        context: Context,
        item: VaultItem<SyncObject.IdCard>,
        subViewFactory: SubViewFactory,
        editMode: Boolean,
        canDelete: Boolean,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): List<ItemSubView<*>> {
        
        val identitySubviews = createIdentitySubviews(
            context = context,
            genericDataQuery = genericDataQuery,
            vaultDataQuery = vaultDataQuery,
            subViewFactory = subViewFactory,
            editMode = editMode,
            listener = listener,
            item = item,
            identityAdapter = IdCardIdentityAdapter
        )

        val subviews = listOfNotNull(
            
            createCountryField(context, item, editMode),
            
            createNumberField(subViewFactory, context, item, editMode),
            
            createIdDateField(
                context,
                item,
                editMode,
                listener,
                item.syncObject.deliveryDate,
                context.getString(R.string.issue_date),
                CopyField.IdsIssueDate,
                VaultItem<*>::copyForUpdatedDeliveryDate,
                vaultItemCopy
            ),
            
            createIdDateField(
                context,
                item,
                editMode,
                listener,
                item.syncObject.expireDate,
                context.getString(R.string.expiery_date),
                CopyField.IdsExpirationDate,
                VaultItem<*>::copyForUpdatedExpirationDate,
                vaultItemCopy
            ),
            
            createTeamspaceField(subViewFactory, item),
            
            subViewFactory.createSubviewAttachmentDetails(context, item),
            
            dateTimeFieldFactory.createCreationDateField(editMode = editMode, context = context, item = item),
            
            dateTimeFieldFactory.createLatestUpdateDateField(editMode = editMode, context = context, item = item),
            
            subViewFactory.createSubviewDelete(context, listener, canDelete)
        )

        return identitySubviews + subviews
    }

    private fun createTeamspaceField(
        subViewFactory: SubViewFactory,
        item: VaultItem<SyncObject.IdCard>
    ): ItemSubView<*>? {
        return if (teamspaceAccessor.canChangeTeamspace) {
            subViewFactory.createSpaceSelector(
                item.syncObject.spaceId,
                teamspaceAccessor,
                null,
                VaultItem<*>::copyForUpdatedTeamspace
            )
        } else {
            null
        }
    }

    private fun createNumberField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.IdCard>,
        editMode: Boolean
    ): ItemSubView<*>? {
        val number = item.syncObject.number
        val numberView = subViewFactory.createSubViewString(
            context.getString(R.string.passport_hint_number),
            number,
            false,
            VaultItem<*>::copyForUpdatedNumber
        )
        return if (numberView == null || editMode) {
            numberView
        } else {
            ItemSubViewWithActionWrapper(
                numberView,
                CopyAction(
                    summaryObject = item.toSummary(),
                    copyField = CopyField.IdsNumber,
                    action = {},
                    vaultItemCopy = vaultItemCopy
                )
            )
        }
    }

    private object IdCardIdentityAdapter : IdentityAdapter<SyncObject.IdCard> {
        override fun fullName(item: VaultItem<SyncObject.IdCard>) = item.syncObject.fullname

        override fun withFullName(item: VaultItem<SyncObject.IdCard>, fullName: String?):
            VaultItem<SyncObject.IdCard> = item.copySyncObject { fullname = fullName }

        override fun gender(item: VaultItem<SyncObject.IdCard>): SyncObject.Gender? = item.syncObject.sex

        override fun withGender(
            item: VaultItem<SyncObject.IdCard>,
            gender: SyncObject.Gender?
        ): VaultItem<SyncObject.IdCard> =
            item.copySyncObject { sex = gender }

        override fun birthDate(item: VaultItem<SyncObject.IdCard>) = item.syncObject.dateOfBirth

        override fun withBirthDate(item: VaultItem<SyncObject.IdCard>, birthDate: LocalDate?):
            VaultItem<SyncObject.IdCard> = item.copySyncObject { dateOfBirth = birthDate }

        override fun linkedIdentity(item: VaultItem<SyncObject.IdCard>) = item.syncObject.linkedIdentity

        override fun withLinkedIdentity(item: VaultItem<SyncObject.IdCard>, identity: String?):
            VaultItem<SyncObject.IdCard> =
            item.copySyncObject { linkedIdentity = identity }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedTeamspace(value: TeamSpace): VaultItem<*> {
    this as VaultItem<SyncObject.IdCard>
    val idCard = this.syncObject
    return if (value.teamId == idCard.spaceId) {
        this
    } else {
        this.copyWithAttrs { teamSpaceId = value.teamId }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedExpirationDate(value: LocalDate?): VaultItem<*> {
    this as VaultItem<SyncObject.IdCard>
    val idCard = this.syncObject
    return if (value == idCard.expireDate) {
        this
    } else {
        this.copySyncObject { expireDate = value }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedDeliveryDate(value: LocalDate?): VaultItem<*> {
    this as VaultItem<SyncObject.IdCard>
    val idCard = this.syncObject
    return if (value == idCard.deliveryDate) {
        this
    } else {
        this.copySyncObject { deliveryDate = value }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedNumber(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.IdCard>
    val idCard = this.syncObject
    return if (value == idCard.number.orEmpty()) {
        this
    } else {
        this.copySyncObject { number = value }
    }
}
