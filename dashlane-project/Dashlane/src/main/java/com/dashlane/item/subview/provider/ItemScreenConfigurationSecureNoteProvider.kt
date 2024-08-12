package com.dashlane.item.subview.provider

import android.app.Activity
import android.content.Context
import androidx.core.content.ContextCompat
import com.dashlane.R
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.item.ItemEditViewContract
import com.dashlane.item.ScreenConfiguration
import com.dashlane.item.header.ItemHeader
import com.dashlane.item.subview.ItemScreenConfigurationProvider
import com.dashlane.item.subview.ItemSubView
import com.dashlane.item.subview.action.DeleteMenuAction
import com.dashlane.item.subview.action.MenuAction
import com.dashlane.item.subview.action.NewShareMenuAction
import com.dashlane.item.subview.action.note.SecureNoteCategoryMenuAction
import com.dashlane.item.subview.action.note.SecureNoteColorMenuAction
import com.dashlane.item.subview.action.note.SecureNoteLockMenuAction
import com.dashlane.item.subview.edit.ItemEditValueRawSubView
import com.dashlane.item.subview.readonly.ItemReadValueRawSubView
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.teamspaces.ui.TeamSpaceRestrictionNotificator
import com.dashlane.ui.screens.fragments.SharingPolicyDataProvider
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.featureflipping.canUseSecureNotes
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.model.getColorId
import com.dashlane.vault.model.hasBeenSaved
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.vault.util.hasAttachments
import com.dashlane.xml.domain.SyncObject

class ItemScreenConfigurationSecureNoteProvider(
    private val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>,
    private val genericDataQuery: GenericDataQuery,
    private val sharingPolicy: SharingPolicyDataProvider,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val dateTimeFieldFactory: DateTimeFieldFactory,
    private val restrictionNotificator: TeamSpaceRestrictionNotificator,
    private val frozenStateManager: FrozenStateManager,
) : ItemScreenConfigurationProvider() {

    private val secureNoteDisabled
        get() = !userFeaturesChecker.canUseSecureNotes()

    private val teamSpaceAccessor: TeamSpaceAccessor?
        get() = teamSpaceAccessorProvider.get()

    private var isLockMenuClicked = false
    private var isSecured = false
    private var selectedNoteType: SyncObject.SecureNoteType? = null
    private var selectedCategoryUid: String? = null

    @Suppress("UNCHECKED_CAST")
    override fun createScreenConfiguration(
        context: Context,
        item: VaultItem<*>,
        subViewFactory: SubViewFactory,
        editMode: Boolean,
        canDelete: Boolean,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): ScreenConfiguration {
        val secureNoteItem = item as VaultItem<SyncObject.SecureNote>
        val canEdit = canEdit(secureNoteItem)
        return ScreenConfiguration(
            createSubViews(context, secureNoteItem, subViewFactory, canEdit),
            createHeader(context, secureNoteItem, canEdit, canDelete, listener)
        )
    }

    private fun createHeader(
        context: Context,
        item: VaultItem<SyncObject.SecureNote>,
        canEdit: Boolean,
        canDelete: Boolean,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): ItemHeader {
        val menuActions = mutableListOf<MenuAction>()
        menuActions.addAll(createMenus())

        
        if (!isLockMenuClicked) {
            isSecured = item.syncObject.secured ?: false
        }

        if (canEdit) {
            
            val lockMenuAction: (Activity) -> Unit = {
                isLockMenuClicked = true
                isSecured = !isSecured
            }
            
            val lockMenuUpdate = copyForUpdatedSecure()
            val lockMenu = SecureNoteLockMenuAction(isSecured, lockMenuAction, lockMenuUpdate)

            
            if (teamSpaceAccessor?.isSsoUser != true) {
                menuActions.add(lockMenu)
            }

            
            selectedCategoryUid = item.syncObject.category

            val categorySelectAction: (String?) -> Unit = {
                selectedCategoryUid = it
            }
            val categoryMenuUpdate = copyForUpdatedCategory()
            menuActions.add(
                SecureNoteCategoryMenuAction(
                    context = context,
                    genericDataQuery = genericDataQuery,
                    item = item.syncObject,
                    categorySelectAction = categorySelectAction,
                    updateAction = categoryMenuUpdate
                )
            )

            
            val colorSelectAction: (SyncObject.SecureNoteType) -> Unit = {
                selectedNoteType = it
                listener.notifyColorChanged(ContextCompat.getColor(context, it.getColorId()))
            }
            val colorMenuUpdate = copyForUpdatedType()
            menuActions.add(SecureNoteColorMenuAction(item, colorSelectAction, colorMenuUpdate))
        }

        if (canDelete) {
            menuActions.add(DeleteMenuAction(listener))
        }

        
        if (canShare(item)) {
            menuActions.add(NewShareMenuAction(item.toSummary(), restrictionNotificator))
        }

        listener.notifyColorChanged(
            ContextCompat.getColor(
                context,
                (item.syncObject.type ?: SyncObject.SecureNoteType.NO_TYPE).getColorId()
            )
        )
        return ItemHeader(menuActions)
    }

    @Suppress("UNCHECKED_CAST")
    private fun copyForUpdatedType(): (VaultItem<*>) -> VaultItem<*> {
        return {
            it as VaultItem<SyncObject.SecureNote>
            val secureNote = it.syncObject
            if (selectedNoteType == null || selectedNoteType == secureNote.type) {
                it
            } else {
                it.copySyncObject { type = selectedNoteType }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun copyForUpdatedCategory(): (VaultItem<*>) -> VaultItem<*> {
        return {
            it as VaultItem<SyncObject.SecureNote>
            val secureNote = it.syncObject
            if (selectedCategoryUid == secureNote.category) {
                it
            } else {
                it.copySyncObject { category = selectedCategoryUid }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun copyForUpdatedSecure(): (VaultItem<*>) -> VaultItem<*> {
        return {
            it as VaultItem<SyncObject.SecureNote>
            val secureNote = it.syncObject
            if (isSecured == secureNote.secured) {
                it
            } else {
                it.copySyncObject { secured = isSecured }
            }
        }
    }

    private fun createSubViews(
        context: Context,
        item: VaultItem<SyncObject.SecureNote>,
        subViewFactory: SubViewFactory,
        canEdit: Boolean
    ): List<ItemSubView<*>> {
        return listOfNotNull(
            
            createTitleField(context, item, canEdit),
            
            createContentField(context, item, canEdit),
            
            createTeamspaceField(subViewFactory, item),
            
            subViewFactory.createSubviewSharingDetails(context, item, sharingPolicy),
            
            subViewFactory.createSubviewAttachmentDetails(context, item),
            
            dateTimeFieldFactory.createCreationDateField(editMode = false, context = context, item = item),
            
            dateTimeFieldFactory.createLatestUpdateDateField(
                editMode = false,
                shared = item.isShared(),
                context = context,
                item = item
            ),
        )
    }

    private fun createTeamspaceField(
        subViewFactory: SubViewFactory,
        item: VaultItem<SyncObject.SecureNote>
    ): ItemSubView<*>? {
        val teamSpaceAccessor = this.teamSpaceAccessor ?: return null

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

    private fun createContentField(
        context: Context,
        item: VaultItem<SyncObject.SecureNote>,
        canEdit: Boolean
    ): ItemSubView<*> {
        val content = item.syncObject.content ?: ""
        return if (canEdit) {
            ItemEditValueRawSubView(
                context.getString(R.string.secure_note_hint_content),
                content,
                context.resources.getDimension(R.dimen.dashlane_font_size_medium),
                VaultItem<*>::copyForUpdatedContent
            )
        } else {
            ItemReadValueRawSubView(
                context.getString(R.string.secure_note_hint_content),
                content,
                context.resources.getDimension(R.dimen.dashlane_font_size_medium)
            )
        }
    }

    private fun createTitleField(
        context: Context,
        item: VaultItem<SyncObject.SecureNote>,
        canEdit: Boolean
    ): ItemSubView<*> {
        val title = item.syncObject.title ?: ""
        return if (canEdit) {
            ItemEditValueRawSubView(
                context.getString(R.string.secure_note_hint_title),
                title,
                context.resources.getDimension(R.dimen.dashlane_font_size_huge),
                VaultItem<*>::copyForUpdatedTitle
            )
        } else {
            ItemReadValueRawSubView(
                context.getString(R.string.secure_note_hint_title),
                title,
                context.resources.getDimension(R.dimen.dashlane_font_size_huge)
            )
        }
    }

    private fun canEdit(item: VaultItem<SyncObject.SecureNote>) =
        !secureNoteDisabled && sharingPolicy.canEditItem(item.toSummary(), !item.hasBeenSaved) && !frozenStateManager.isAccountFrozen

    private fun canShare(item: VaultItem<SyncObject.SecureNote>) =
        !secureNoteDisabled && item.hasBeenSaved &&
            sharingPolicy.canShareItem(item.toSummary()) &&
            !item.toSummary<SummaryObject.SecureNote>().hasAttachments() &&
            !frozenStateManager.isAccountFrozen
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedTeamspace(value: TeamSpace): VaultItem<SyncObject.SecureNote> {
    val dataIdentifier = this as VaultItem<SyncObject.SecureNote>
    val secureNote = dataIdentifier.syncObject
    return if (value.teamId == secureNote.spaceId) {
        dataIdentifier
    } else {
        dataIdentifier.copySyncObject { spaceId = value.teamId }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedContent(value: String): VaultItem<SyncObject.SecureNote> {
    this as VaultItem<SyncObject.SecureNote>
    val secureNote = this.syncObject
    return if (value == secureNote.content.orEmpty()) {
        this
    } else {
        this.copySyncObject { content = value }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedTitle(value: String): VaultItem<SyncObject.SecureNote> {
    this as VaultItem<SyncObject.SecureNote>
    val secureNote = this.syncObject
    return if (value == secureNote.title.orEmpty()) {
        this
    } else {
        this.copySyncObject { title = value }
    }
}
