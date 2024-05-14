package com.dashlane.item.subview.provider.payment

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
import com.dashlane.storage.userdata.EmailSuggestionProvider
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.util.graphics.getDominantColor
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.obfuscated.matchesNullAsEmpty
import com.dashlane.util.obfuscated.toSyncObfuscatedValue
import com.dashlane.vault.VaultItemLogger
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject

class ItemScreenConfigurationPaypalProvider(
    private val teamSpaceAccessor: TeamSpaceAccessor,
    private val emailSuggestionProvider: EmailSuggestionProvider,
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
        item as VaultItem<SyncObject.PaymentPaypal>
        return ScreenConfiguration(
            createSubViews(context, item, subViewFactory, editMode, canDelete, listener),
            createHeader(context, item)
        )
    }

    private fun createHeader(
        context: Context,
        item: VaultItem<SyncObject.PaymentPaypal>
    ): ItemHeader {
        val iconDrawable = createDefaultHeaderIcon(context, item.syncObject)?.apply {
            val dominantColor = getDominantColor(image)
            isWithBorder = false
            backgroundColor = dominantColor
        }

        val paypalTitle = item.syncObject.name
        return ItemHeader(createMenus(), paypalTitle, iconDrawable)
    }

    private fun createSubViews(
        context: Context,
        item: VaultItem<SyncObject.PaymentPaypal>,
        subViewFactory: SubViewFactory,
        editMode: Boolean,
        canDelete: Boolean,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): List<ItemSubView<*>> {
        val loginView = createLoginField(subViewFactory, context, item)
        return listOfNotNull(
            
            createNameField(editMode, subViewFactory, context, item),
            
            loginView,
            
            createPasswordField(editMode, subViewFactory, context, item),
            
            createTeamspaceField(subViewFactory, item, loginView),
            
            subViewFactory.createSubviewAttachmentDetails(context, item),
            
            dateTimeFieldFactory.createCreationDateField(editMode = editMode, context = context, item = item),
            
            dateTimeFieldFactory.createLatestUpdateDateField(editMode = editMode, context = context, item = item),
            
            subViewFactory.createSubviewDelete(context, listener, canDelete)
        )
    }

    private fun createPasswordField(
        editMode: Boolean,
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.PaymentPaypal>
    ): ItemSubView<*>? {
        val itemSubView = subViewFactory.createSubViewString(
            context.getString(R.string.paypal_hint_password),
            item.syncObject.password?.toString(),
            true,
            VaultItem<*>::copyForUpdatedPassword,
            protectedStateListener = { passwordShown -> if (passwordShown) logRevealPassword(item) }
        )
        return if (editMode) {
            itemSubView
        } else {
            itemSubView?.let {
                ItemSubViewWithActionWrapper(
                    it,
                    CopyAction(
                        summaryObject = item.toSummary(),
                        copyField = CopyField.PayPalPassword,
                        action = {},
                        vaultItemCopy = vaultItemCopy
                    )
                )
            }
        }
    }

    private fun createLoginField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.PaymentPaypal>
    ): ItemSubView<String>? {
        val loginSuggestions = emailSuggestionProvider.getAllEmails()
        return subViewFactory.createSubViewString(
            context.getString(R.string.paypal_hint_login),
            item.syncObject.login,
            false,
            VaultItem<*>::copyForUpdatedLogin,
            loginSuggestions
        )
    }

    private fun createNameField(
        editMode: Boolean,
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.PaymentPaypal>
    ): ItemSubView<*>? {
        return if (editMode) {
            subViewFactory.createSubViewString(
                context.getString(R.string.paypal_hint_name),
                item.syncObject.name,
                false,
                VaultItem<*>::copyForUpdatedName
            )
        } else {
            null
        }
    }

    private fun createTeamspaceField(
        subViewFactory: SubViewFactory,
        item: VaultItem<SyncObject.PaymentPaypal>,
        loginView: ItemSubView<String>?
    ): ItemSubView<*>? {
        return if (teamSpaceAccessor.canChangeTeamspace) {
            subViewFactory.createSpaceSelector(
                item.syncObject.spaceId,
                teamSpaceAccessor,
                listOfNotNull(loginView),
                VaultItem<*>::copyForUpdatedTeamspace
            )
        } else {
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun hasEnoughDataToSave(itemToSave: VaultItem<*>): Boolean {
        itemToSave as VaultItem<SyncObject.PaymentPaypal>
        return itemToSave.syncObject.login.isNotSemanticallyNull() ||
            itemToSave.syncObject.password?.toString().isNotSemanticallyNull()
    }

    private fun logRevealPassword(item: VaultItem<SyncObject.PaymentPaypal>) {
        vaultItemLogger.logRevealField(Field.PASSWORD, item.uid, ItemType.PAYPAL, null)
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedLogin(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.PaymentPaypal>
    val paypal = this.syncObject
    return if (value == paypal.login.orEmpty()) {
        this
    } else {
        this.copySyncObject { login = value }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedName(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.PaymentPaypal>
    val paypal = this.syncObject
    return if (value == paypal.name.orEmpty()) {
        this
    } else {
        this.copySyncObject { name = value }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedTeamspace(value: TeamSpace): VaultItem<*> {
    this as VaultItem<SyncObject.PaymentPaypal>
    val paypal = this.syncObject
    return if (value.teamId == paypal.spaceId) {
        this
    } else {
        this.copyWithAttrs { teamSpaceId = value.teamId }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedPassword(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.PaymentPaypal>
    val paypal = this.syncObject
    return if (paypal.password.matchesNullAsEmpty(value)) {
        this
    } else {
        this.copySyncObject { password = value.toSyncObfuscatedValue() }
    }
}
