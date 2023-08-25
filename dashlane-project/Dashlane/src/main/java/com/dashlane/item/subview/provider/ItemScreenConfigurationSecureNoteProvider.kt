package com.dashlane.item.subview.provider

import android.app.Activity
import android.content.Context
import androidx.core.content.ContextCompat
import com.dashlane.R
import com.dashlane.item.ItemEditViewContract
import com.dashlane.item.ScreenConfiguration
import com.dashlane.item.header.ItemHeader
import com.dashlane.item.logger.BaseLogger
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
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.DataCounter
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.teamspaces.manager.isSsoUser
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.ui.screens.fragments.SharingPolicyDataProvider
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.model.getColorId
import com.dashlane.vault.model.hasBeenSaved
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.vault.util.hasAttachments
import com.dashlane.xml.domain.SyncObject

class ItemScreenConfigurationSecureNoteProvider(
    private val teamspaceAccessor: TeamspaceAccessor,
    dataCounter: DataCounter,
    private val mainDataAccessor: MainDataAccessor,
    private val sharingPolicy: SharingPolicyDataProvider,
    private val userFeaturesChecker: UserFeaturesChecker,
    sessionManager: SessionManager,
    bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val dateTimeFieldFactory: DateTimeFieldFactory
) : ItemScreenConfigurationProvider(
    teamspaceAccessor,
    dataCounter,
    sessionManager,
    bySessionUsageLogRepository
) {

    override val logger = BaseLogger(
        teamspaceAccessor,
        dataCounter,
        sessionManager,
        bySessionUsageLogRepository
    )
    private val secureNoteDisabled
        get() = userFeaturesChecker.has(UserFeaturesChecker.FeatureFlip.DISABLE_SECURE_NOTES) ||
                !teamspaceAccessor.isFeatureEnabled(Teamspace.Feature.SECURE_NOTES_DISABLED)
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

            
            if (!teamspaceAccessor.isSsoUser) {
                menuActions.add(lockMenu)
            }

            
            selectedCategoryUid = item.syncObject.category

            val categorySelectAction: (String?) -> Unit = {
                selectedCategoryUid = it
            }
            val categoryMenuUpdate = copyForUpdatedCategory()
            menuActions.add(
                SecureNoteCategoryMenuAction(
                    context,
                    mainDataAccessor.getGenericDataQuery(),
                    item.syncObject,
                    categorySelectAction,
                    categoryMenuUpdate
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
            menuActions.add(NewShareMenuAction(item))
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
        return if (teamspaceAccessor.canChangeTeamspace()) {
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
        !secureNoteDisabled && sharingPolicy.canEditItem(item.toSummary(), !item.hasBeenSaved)

    private fun canShare(item: VaultItem<SyncObject.SecureNote>) = !secureNoteDisabled && item
        .hasBeenSaved &&
            sharingPolicy.canShareItem(item.toSummary()) && !item.toSummary<SummaryObject.SecureNote>().hasAttachments()
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedTeamspace(value: Teamspace): VaultItem<SyncObject.SecureNote> {
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
