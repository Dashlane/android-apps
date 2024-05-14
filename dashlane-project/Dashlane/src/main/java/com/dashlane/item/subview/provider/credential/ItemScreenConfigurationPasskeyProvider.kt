package com.dashlane.item.subview.provider.credential

import android.content.Context
import android.os.Build
import com.dashlane.R
import com.dashlane.design.theme.color.Mood
import com.dashlane.item.ItemEditViewContract
import com.dashlane.item.ScreenConfiguration
import com.dashlane.item.header.ItemHeader
import com.dashlane.item.subview.ItemScreenConfigurationProvider
import com.dashlane.item.subview.ItemSubView
import com.dashlane.item.subview.ItemSubViewWithActionWrapper
import com.dashlane.item.subview.action.CopyAction
import com.dashlane.item.subview.action.LoginAction
import com.dashlane.item.subview.action.LoginOpener
import com.dashlane.item.subview.action.MenuAction
import com.dashlane.item.subview.provider.DateTimeFieldFactory
import com.dashlane.item.subview.provider.SubViewFactory
import com.dashlane.item.subview.readonly.ItemInfoboxSubView
import com.dashlane.item.subview.readonly.ItemReadValueTextSubView
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.ui.VaultItemImageHelper
import com.dashlane.url.name
import com.dashlane.url.toUrlOrNull
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.VaultItemLogger
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.model.title
import com.dashlane.vault.model.urlForGoToWebsite
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject

class ItemScreenConfigurationPasskeyProvider(
    private val teamSpaceAccessor: TeamSpaceAccessor,
    private val vaultItemLogger: VaultItemLogger,
    private val dateTimeFieldFactory: DateTimeFieldFactory,
    private val vaultItemCopy: VaultItemCopyService
) : ItemScreenConfigurationProvider() {

    private var isLoginCopied = false

    @Suppress("UNCHECKED_CAST")
    override fun createScreenConfiguration(
        context: Context,
        item: VaultItem<*>,
        subViewFactory: SubViewFactory,
        editMode: Boolean,
        canDelete: Boolean,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): ScreenConfiguration {
        item as VaultItem<SyncObject.Passkey>
        return ScreenConfiguration(
            createSubViews(context, item, subViewFactory, editMode, canDelete, listener),
            createHeader(context, item)
        )
    }

    private fun createHeader(
        context: Context,
        item: VaultItem<SyncObject.Passkey>
    ): ItemHeader {
        val passkeySyncObject = item.syncObject
        val urlIconDrawable = VaultItemImageHelper.getIconDrawableFromSyncObject(
            context,
            item.syncObject
        )
        val menuActions = mutableListOf<MenuAction>().apply {
            addAll(createMenus())
        }
        return ItemHeader(menuActions, passkeySyncObject.title, urlIconDrawable)
    }

    private fun createSubViews(
        context: Context,
        item: VaultItem<SyncObject.Passkey>,
        subViewFactory: SubViewFactory,
        editMode: Boolean,
        canDelete: Boolean,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): List<ItemSubView<*>> {
        val websiteView = createWebsiteField(editMode, item, context)
        val loginView = createLoginFieldReadOnly(
            editMode,
            context.getString(R.string.authentifiant_hint_login),
            item.syncObject.userDisplayName,
            item
        )
        val teamspaceView = createTeamspaceField(
            subViewFactory = subViewFactory,
            item = item,
            views = listOfNotNull(loginView, websiteView)
        )

        return listOfNotNull(
            
            createPasskeyNotUsableInfobox(editMode, context),
            
            loginView,
            
            websiteView,
            
            createItemNameField(editMode, subViewFactory, context, item),
            
            createNoteField(editMode, subViewFactory, context, item),
            
            teamspaceView,
            
            dateTimeFieldFactory.createCreationDateField(
                editMode = editMode,
                context = context,
                item = item
            ),
            
            dateTimeFieldFactory.createLatestUpdateDateField(
                editMode = editMode,
                context = context,
                item = item
            ),
            
            subViewFactory.createSubviewDelete(context, listener, canDelete)
        )
    }

    private fun createPasskeyNotUsableInfobox(
        editMode: Boolean,
        context: Context,
    ): ItemInfoboxSubView? {
        if (!editMode && Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return ItemInfoboxSubView(
                value = context.getString(R.string.infobox_passkey_not_usable),
                mood = Mood.Brand
            )
        }
        return null
    }

    private fun createItemNameField(
        editMode: Boolean,
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.Passkey>
    ): ItemSubView<*>? {
        return if (editMode) {
            subViewFactory.createSubViewString(
                context.getString(R.string.authentifiant_hint_name),
                item.syncObject.title,
                false,
                ::copyForUpdatedItemName
            )
        } else {
            null
        }
    }

    private fun createNoteField(
        editMode: Boolean,
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.Passkey>
    ): ItemSubView<*>? {
        return if (editMode) {
            subViewFactory.createSubViewString(
                header = context.getString(R.string.authentifiant_hint_note),
                value = item.syncObject.note,
                valueUpdate = ::copyForUpdatedNote,
                multiline = true
            )
        } else {
            
            if (item.syncObject.note.isNotSemanticallyNull()) {
                subViewFactory.createSubViewString(
                    header = context.getString(R.string.authentifiant_hint_note),
                    value = item.syncObject.note,
                    multiline = true
                )
            } else {
                null
            }
        }
    }

    private fun createLoginFieldReadOnly(
        editMode: Boolean,
        header: String,
        value: String?,
        item: VaultItem<SyncObject.Passkey>
    ): ItemSubView<String> {
        return ItemReadValueTextSubView(header, value ?: "").let {
            if (editMode) {
                it
            } else {
                ItemSubViewWithActionWrapper(
                    it,
                    CopyAction(
                        summaryObject = item.toSummary(),
                        copyField = CopyField.PasskeyDisplayName,
                        action = {
                            isLoginCopied = true
                        },
                        vaultItemCopy = vaultItemCopy
                    )
                )
            }
        }
    }

    private fun createWebsiteField(
        editMode: Boolean,
        item: VaultItem<SyncObject.Passkey>,
        context: Context
    ): ItemSubView<String> {
        val loginListener = object : LoginOpener.Listener {
            override fun onShowOption() = Unit

            override fun onLogin(packageName: String) {
                vaultItemLogger.logOpenExternalLink(
                    itemId = item.uid,
                    packageName = packageName,
                    url = item.syncObject.urlForGoToWebsite
                )
            }
        }
        val loginAction = LoginAction(item.syncObject.urlForGoToWebsite ?: "", null, loginListener)
        val urlToDisplay = item.syncObject.rpId?.let { fullUrl ->
            fullUrl.toUrlOrNull()?.name ?: fullUrl 
        }
        return ItemReadValueTextSubView(context.getString(R.string.authentifiant_hint_url), urlToDisplay ?: "").let {
            if (editMode) {
                it
            } else {
                ItemSubViewWithActionWrapper(it, loginAction)
            }
        }
    }

    private fun createTeamspaceField(
        subViewFactory: SubViewFactory,
        item: VaultItem<SyncObject.Passkey>,
        views: List<ItemSubView<String>>
    ): ItemSubView<*>? {
        return if (teamSpaceAccessor.canChangeTeamspace) {
            subViewFactory.createSpaceSelector(
                item.syncObject.spaceId,
                teamSpaceAccessor,
                views,
                ::copyForUpdatedTeamspace
            )
        } else {
            null
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun copyForUpdatedTeamspace(
    item: VaultItem<*>,
    value: TeamSpace
): VaultItem<SyncObject.Passkey> {
    item as VaultItem<SyncObject.Passkey>
    val authentifiant = item.syncObject
    val spaceId = authentifiant.spaceId ?: TeamSpace.Personal.teamId
    return if (value.teamId == spaceId) {
        item
    } else {
        item.copySyncObject { this.spaceId = value.teamId }
    }
}

@Suppress("UNCHECKED_CAST")
private fun ItemScreenConfigurationPasskeyProvider.copyForUpdatedNote(
    item: VaultItem<*>,
    value: String
): VaultItem<SyncObject.Passkey> {
    item as VaultItem<SyncObject.Passkey>
    val passkey = item.syncObject
    return if (value == passkey.note.orEmpty()) {
        item
    } else {
        item.copySyncObject { note = value }
    }
}

@Suppress("UNCHECKED_CAST")
private fun ItemScreenConfigurationPasskeyProvider.copyForUpdatedItemName(
    item: VaultItem<*>,
    value: String
): VaultItem<SyncObject.Passkey> {
    item as VaultItem<SyncObject.Passkey>
    val authentifiant = item.syncObject
    return if (value == authentifiant.title.orEmpty()) {
        item
    } else {
        item.copySyncObject { itemName = value }
    }
}