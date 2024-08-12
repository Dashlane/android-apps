package com.dashlane.item.subview.provider.personalinfo

import android.content.Context
import com.dashlane.R
import com.dashlane.design.component.compat.view.ThumbnailViewType
import com.dashlane.item.ItemEditViewContract
import com.dashlane.item.ScreenConfiguration
import com.dashlane.item.header.ItemHeader
import com.dashlane.item.subview.ItemScreenConfigurationProvider
import com.dashlane.item.subview.ItemSubView
import com.dashlane.item.subview.ValueChangeManager
import com.dashlane.item.subview.edit.ItemEditValueDateSubView
import com.dashlane.item.subview.provider.DateTimeFieldFactory
import com.dashlane.item.subview.provider.SubViewFactory
import com.dashlane.item.subview.readonly.ItemReadValueDateSubView
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.toIdentityFormat
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.model.getStringId
import com.dashlane.xml.domain.SyncObject
import java.time.LocalDate

class ItemScreenConfigurationIdentityProvider(
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
        item as VaultItem<SyncObject.Identity>
        return ScreenConfiguration(
            createSubViews(context, item, subViewFactory, editMode, canDelete, listener),
            createHeader(context, item)
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun hasEnoughDataToSave(itemToSave: VaultItem<*>): Boolean {
        itemToSave as VaultItem<SyncObject.Identity>
        return itemToSave.syncObject.firstName?.trim().isNotSemanticallyNull() ||
            itemToSave.syncObject.lastName?.trim().isNotSemanticallyNull() ||
            itemToSave.syncObject.middleName?.trim().isNotSemanticallyNull() ||
            itemToSave.syncObject.pseudo?.trim().isNotSemanticallyNull() ||
            itemToSave.syncObject.birthPlace?.trim().isNotSemanticallyNull()
    }

    private fun createHeader(
        context: Context,
        item: VaultItem<*>
    ): ItemHeader {
        return ItemHeader(
            menuActions = createMenus(),
            title = context.getString(R.string.identity),
            thumbnailType = ThumbnailViewType.VAULT_ITEM_OTHER_ICON.value,
            thumbnailIconRes = getHeaderIcon(item.syncObject),
        )
    }

    private fun createSubViews(
        context: Context,
        item: VaultItem<SyncObject.Identity>,
        subViewFactory: SubViewFactory,
        editMode: Boolean,
        canDelete: Boolean,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): List<ItemSubView<*>> {
        return listOfNotNull(
            
            createTitleField(context, item, subViewFactory),
            
            createFirstNameField(subViewFactory, context, item),
            
            createLastNameField(subViewFactory, context, item),
            
            createMiddleNameField(subViewFactory, context, item),
            
            createUsernameField(subViewFactory, context, item),
            
            createBirthDateField(item, editMode, context, listener),
            
            createBirthPlaceField(subViewFactory, context, item),
            
            createTeamspaceField(subViewFactory, item),
            
            subViewFactory.createSubviewAttachmentDetails(context, item),
            
            dateTimeFieldFactory.createCreationDateField(editMode = editMode, context = context, item = item),
            
            dateTimeFieldFactory.createLatestUpdateDateField(editMode = editMode, context = context, item = item),
            
            subViewFactory.createSubviewDelete(context, listener, canDelete)
        )
    }

    private fun createTeamspaceField(
        subViewFactory: SubViewFactory,
        item: VaultItem<SyncObject.Identity>
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

    private fun createBirthPlaceField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.Identity>
    ): ItemSubView<*>? {
        return subViewFactory.createSubViewString(
            context.getString(R.string.identity_hint_place_of_birth),
            item.syncObject.birthPlace,
            false,
            VaultItem<*>::copyForUpdatedBirthPlace
        )
    }

    private fun createBirthDateField(
        item: VaultItem<SyncObject.Identity>,
        editMode: Boolean,
        context: Context,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): ItemSubView<*> {
        val birthLocalDate = item.syncObject.birthDate
        val formattedString = birthLocalDate?.toIdentityFormat(context.resources)

        return if (editMode) {
            ItemEditValueDateSubView(
                context.getString(R.string.date_of_birth),
                birthLocalDate,
                formattedString ?: "",
                VaultItem<*>::copyForUpdatedDateOfBirth
            ).apply {
                addValueChangedListener(object : ValueChangeManager.Listener<LocalDate?> {
                    override fun onValueChanged(origin: Any, newValue: LocalDate?) {
                        val subView = this@apply
                        subView.formattedDate = newValue?.toIdentityFormat(context.resources)
                        subView.value = newValue
                        listener.notifySubViewChanged(this@apply)
                    }
                })
            }
        } else {
            ItemReadValueDateSubView(
                context.getString(R.string.date_of_birth),
                birthLocalDate,
                formattedString ?: ""
            )
        }
    }

    private fun createUsernameField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.Identity>
    ): ItemSubView<*>? {
        return subViewFactory.createSubViewString(
            context.getString(R.string.identity_hint_default_login),
            item.syncObject.pseudo,
            false,
            VaultItem<*>::copyForUpdatedPseudo
        )
    }

    private fun createMiddleNameField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.Identity>
    ): ItemSubView<*>? {
        return subViewFactory.createSubViewString(
            context.getString(R.string.identity_hint_middle_name),
            item.syncObject.middleName,
            false,
            VaultItem<*>::copyForUpdatedMiddleName
        )
    }

    private fun createLastNameField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.Identity>
    ): ItemSubView<*>? {
        return subViewFactory.createSubViewString(
            context.getString(R.string.identity_hint_last_name),
            item.syncObject.lastName,
            false,
            VaultItem<*>::copyForUpdatedLastName
        )
    }

    private fun createFirstNameField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.Identity>
    ): ItemSubView<*>? {
        return subViewFactory.createSubViewString(
            context.getString(R.string.identity_hint_first_name),
            item.syncObject.firstName,
            false,
            VaultItem<*>::copyForUpdatedFirstName
        )
    }

    private fun createTitleField(
        context: Context,
        item: VaultItem<SyncObject.Identity>,
        subViewFactory: SubViewFactory
    ): ItemSubView<*>? {
        
        
        val allTitleCategories =
            SyncObject.Identity.Title.values().map { context.getString(it.getStringId()) }.distinct()
        
        val selectedTitle = item.syncObject.title
        selectedTitle?.getStringId()?.let { titleStringId ->
            return subViewFactory.createSubviewListNonDefault(
                context.getString(R.string.type),
                context.getString(titleStringId),
                allTitleCategories
            ) { it, value ->
                it.copyForUpdatedTitle(context, value)
            }
        }
        return null
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedTeamspace(value: TeamSpace): VaultItem<*> {
    this as VaultItem<SyncObject.Identity>
    val identity = this.syncObject
    return if (value.teamId == identity.spaceId) {
        this
    } else {
        this.copyWithAttrs { teamSpaceId = value.teamId }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedBirthPlace(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.Identity>
    val identity = this.syncObject
    return if (value == identity.birthPlace.orEmpty()) {
        this
    } else {
        this.copySyncObject { birthPlace = value }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedDateOfBirth(value: LocalDate?): VaultItem<*> {
    this as VaultItem<SyncObject.Identity>
    val identity = this.syncObject
    return if (value == identity.birthDate) {
        this
    } else {
        this.copySyncObject { birthDate = value }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedPseudo(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.Identity>
    val identity = this.syncObject
    return if (value == identity.pseudo.orEmpty()) {
        this
    } else {
        this.copySyncObject { pseudo = value }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedMiddleName(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.Identity>
    val identity = this.syncObject
    return if (value == identity.middleName.orEmpty()) {
        this
    } else {
        this.copySyncObject { middleName = value }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedLastName(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.Identity>
    val identity = this.syncObject
    return if (value == identity.lastName.orEmpty()) {
        this
    } else {
        this.copySyncObject { lastName = value }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedFirstName(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.Identity>
    val identity = this.syncObject
    return if (value == identity.firstName.orEmpty()) {
        this
    } else {
        this.copySyncObject { firstName = value }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedTitle(context: Context, value: String): VaultItem<*> {
    this as VaultItem<SyncObject.Identity>
    val identity = this.syncObject

    val newTitle = SyncObject.Identity.Title.values().firstOrNull { context.getString(it.getStringId()) == value }
    return if (newTitle == identity.title) {
        this
    } else {
        this.copySyncObject { title = newTitle }
    }
}
