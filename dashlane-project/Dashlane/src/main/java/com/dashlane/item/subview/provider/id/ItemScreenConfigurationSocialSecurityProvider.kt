package com.dashlane.item.subview.provider.id

import android.content.Context
import com.dashlane.R
import com.dashlane.hermes.generated.definitions.Field
import com.dashlane.hermes.generated.definitions.ItemType
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
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.obfuscated.matchesNullAsEmpty
import com.dashlane.util.obfuscated.toSyncObfuscatedValue
import com.dashlane.vault.VaultItemLogger
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject
import java.time.LocalDate

class ItemScreenConfigurationSocialSecurityProvider(
    private val teamspaceAccessor: TeamspaceAccessor,
    private val mainDataAccessor: MainDataAccessor,
    private val vaultItemLogger: VaultItemLogger,
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
        item as VaultItem<SyncObject.SocialSecurityStatement>
        return ScreenConfiguration(
            createSubViews(context, item, subViewFactory, editMode, canDelete, listener),
            createHeader(context, item)
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun hasEnoughDataToSave(itemToSave: VaultItem<*>): Boolean {
        itemToSave as VaultItem<SyncObject.SocialSecurityStatement>
        return itemToSave.syncObject.socialSecurityNumber?.toString()?.trim().isNotSemanticallyNull()
    }

    private fun createHeader(
        context: Context,
        item: VaultItem<*>
    ): ItemHeader {
        val iconDrawable = createDefaultHeaderIcon(context, item.syncObject)
        val title = context.getString(R.string.social_security)
        return ItemHeader(createMenus(), title, iconDrawable)
    }

    private fun createSubViews(
        context: Context,
        item: VaultItem<SyncObject.SocialSecurityStatement>,
        subViewFactory: SubViewFactory,
        editMode: Boolean,
        canDelete: Boolean,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): List<ItemSubView<*>> {
        
        val identitySubviews = createIdentitySubviews(
            context,
            subViewFactory,
            mainDataAccessor,
            editMode,
            listener,
            item,
            SocialSecurityStatementIdentityAdapter
        )

        val subviews = listOfNotNull(
            
            createCountryField(context, item, editMode),
            
            createNumberField(subViewFactory, context, item, editMode),
            
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
        item: VaultItem<SyncObject.SocialSecurityStatement>
    ): ItemSubView<*>? {
        return if (teamspaceAccessor.canChangeTeamspace()) {
            subViewFactory.createSpaceSelector(
                item.syncObject.spaceId,
                teamspaceAccessor,
                null,
                valueUpdate = VaultItem<*>::copyForUpdatedTeamspace
            )
        } else {
            null
        }
    }

    private fun createNumberField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.SocialSecurityStatement>,
        editMode: Boolean
    ): ItemSubView<*>? {
        val socialSecurityNumber = item.syncObject.socialSecurityNumber?.toString()
        val itemSubView = subViewFactory.createSubViewString(
            context.getString(R.string.passport_hint_number),
            socialSecurityNumber,
            true,
            VaultItem<*>::copyForUpdatedNumber,
            protectedStateListener = { numberShown -> if (numberShown) logRevealNumber(item) }
        )
        return if (editMode) {
            itemSubView
        } else {
            itemSubView?.let {
                ItemSubViewWithActionWrapper(
                    it,
                    CopyAction(
                        summaryObject = item.toSummary(),
                        copyField = CopyField.SocialSecurityNumber,
                        action = {},
                        vaultItemCopy = vaultItemCopy
                    )
                )
            }
        }
    }

    private fun logRevealNumber(item: VaultItem<SyncObject.SocialSecurityStatement>) {
        vaultItemLogger.logRevealField(Field.SOCIAL_SECURITY_NUMBER, item.uid, ItemType.SOCIAL_SECURITY, null)
    }

    private object SocialSecurityStatementIdentityAdapter : IdentityAdapter<SyncObject.SocialSecurityStatement> {
        override fun fullName(item: VaultItem<SyncObject.SocialSecurityStatement>) =
            item.syncObject.socialSecurityFullname

        override fun withFullName(item: VaultItem<SyncObject.SocialSecurityStatement>, fullName: String?):
            VaultItem<SyncObject.SocialSecurityStatement> =
            item.copySyncObject { socialSecurityFullname = fullName }

        override fun gender(item: VaultItem<SyncObject.SocialSecurityStatement>): SyncObject.Gender? =
            item.syncObject.sex

        override fun withGender(
            item: VaultItem<SyncObject.SocialSecurityStatement>,
            gender: SyncObject.Gender?
        ): VaultItem<SyncObject.SocialSecurityStatement> =
            item.copySyncObject { sex = gender }

        override fun birthDate(item: VaultItem<SyncObject.SocialSecurityStatement>) = item.syncObject.dateOfBirth

        override fun withBirthDate(item: VaultItem<SyncObject.SocialSecurityStatement>, birthDate: LocalDate?):
            VaultItem<SyncObject.SocialSecurityStatement> = item.copySyncObject { dateOfBirth = birthDate }

        override fun linkedIdentity(item: VaultItem<SyncObject.SocialSecurityStatement>) =
            item.syncObject.linkedIdentity

        override fun withLinkedIdentity(item: VaultItem<SyncObject.SocialSecurityStatement>, identity: String?):
            VaultItem<SyncObject.SocialSecurityStatement> =
            item.copySyncObject { linkedIdentity = identity }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedNumber(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.SocialSecurityStatement>
    val socialSecurityStatement = this.syncObject
    return if (socialSecurityStatement.socialSecurityNumber.matchesNullAsEmpty(value)) {
        this
    } else {
        this.copySyncObject { socialSecurityNumber = value.toSyncObfuscatedValue() }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedTeamspace(value: Teamspace): VaultItem<*> {
    this as VaultItem<SyncObject.SocialSecurityStatement>
    val socialSecurityStatement = this.syncObject
    return if (value.teamId == socialSecurityStatement.spaceId) {
        this
    } else {
        this.copyWithAttrs { teamSpaceId = value.teamId }
    }
}
