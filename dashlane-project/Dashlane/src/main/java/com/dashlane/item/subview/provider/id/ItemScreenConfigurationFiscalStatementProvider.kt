package com.dashlane.item.subview.provider.id

import android.content.Context
import com.dashlane.R
import com.dashlane.inapplogin.UsageLogCode35Action
import com.dashlane.item.ItemEditViewContract
import com.dashlane.item.ScreenConfiguration
import com.dashlane.item.header.ItemHeader
import com.dashlane.item.subview.ItemScreenConfigurationProvider
import com.dashlane.item.subview.ItemSubView
import com.dashlane.item.subview.ItemSubViewWithActionWrapper
import com.dashlane.item.subview.ValueChangeManager
import com.dashlane.item.subview.action.CopyAction
import com.dashlane.item.subview.edit.ItemEditValueTextSubView
import com.dashlane.item.subview.provider.DateTimeFieldFactory
import com.dashlane.item.subview.provider.SubViewFactory
import com.dashlane.item.subview.provider.createCountryField
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.DataCounter
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.useractivity.log.usage.UsageLogCode11
import com.dashlane.useractivity.log.usage.UsageLogCode35
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.model.forLabelOrDefault
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.utils.Country
import java.time.LocalDate

class ItemScreenConfigurationFiscalStatementProvider(
    private val teamspaceAccessor: TeamspaceAccessor,
    private val mainDataAccessor: MainDataAccessor,
    dataCounter: DataCounter,
    sessionManager: SessionManager,
    bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val dateTimeFieldFactory: DateTimeFieldFactory
) : ItemScreenConfigurationProvider(
    teamspaceAccessor,
    dataCounter,
    sessionManager,
    bySessionUsageLogRepository
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
        item as VaultItem<SyncObject.FiscalStatement>
        return ScreenConfiguration(
            createSubViews(context, item, subViewFactory, editMode, canDelete, listener),
            createHeader(context, item)
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun hasEnoughDataToSave(itemToSave: VaultItem<*>): Boolean {
        itemToSave as VaultItem<SyncObject.FiscalStatement>
        return itemToSave.syncObject.fiscalNumber?.trim().isNotSemanticallyNull() ||
                itemToSave.syncObject.teledeclarantNumber?.trim().isNotSemanticallyNull()
    }

    private fun createHeader(
        context: Context,
        item: VaultItem<*>
    ): ItemHeader {
        val iconDrawable = createDefaultHeaderIcon(context, item.syncObject)
        val fiscalTitle = context.getString(R.string.fiscal_statement)
        return ItemHeader(createMenus(), fiscalTitle, iconDrawable)
    }

    private fun createSubViews(
        context: Context,
        item: VaultItem<SyncObject.FiscalStatement>,
        subViewFactory: SubViewFactory,
        editMode: Boolean,
        canDelete: Boolean,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): List<ItemSubView<*>> {
        
        val onlineNumberView = createOnlineNumberField(context, item, editMode, subViewFactory)

        
        val identitySubviews = createIdentitySubviews(
            context,
            subViewFactory,
            mainDataAccessor,
            editMode,
            listener,
            item,
            FiscalStatementIdentityAdapter,
            true
        )

        return identitySubviews + listOfNotNull(
            
            createCountryField(context, item, editMode, onlineNumberView, listener),
            
            createTaxNumberField(subViewFactory, context, item, editMode),
            onlineNumberView,
            
            createTeamspaceField(subViewFactory, item),
            
            subViewFactory.createSubviewAttachmentDetails(context, item),
            
            dateTimeFieldFactory.createCreationDateField(editMode = editMode, context = context, item = item),
            
            dateTimeFieldFactory.createLatestUpdateDateField(editMode = editMode, context = context, item = item),
            
            subViewFactory.createSubviewDelete(context, listener, canDelete)
        )
    }

    private fun createTeamspaceField(
        subViewFactory: SubViewFactory,
        item: VaultItem<SyncObject.FiscalStatement>
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

    private fun createTaxNumberField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.FiscalStatement>,
        editMode: Boolean
    ): ItemSubView<*>? {
        val number = item.syncObject.fiscalNumber
        val numberView = subViewFactory.createSubViewString(
            context.getString(R.string.tax_statement_hint_number),
            number,
            false,
            VaultItem<*>::copyForUpdatedFiscalNumber
        )
        return if (numberView == null || editMode) {
            numberView
        } else {
            ItemSubViewWithActionWrapper(
                numberView,
                CopyAction(item.toSummary(), CopyField.TaxNumber, action = {
                    logger.log(
                        UsageLogCode35(
                            type = UsageLogCode11.Type.FISCAL.code,
                            action = UsageLogCode35Action.COPY_NUMBER
                        )
                    )
                })
            )
        }
    }

    private fun createOnlineNumberField(
        context: Context,
        item: VaultItem<SyncObject.FiscalStatement>,
        editMode: Boolean,
        subViewFactory: SubViewFactory
    ): ItemSubView<String>? {
        
        val number = item.syncObject.teledeclarantNumber
        val numberView = subViewFactory.createSubViewString(
            context.getString(R.string.tax_statement_hint_online_number),
            number,
            false,
            VaultItem<*>::copyForUpdatedOnlineNumber
        )?.apply {
            if (editMode) {
                val view = this as ItemEditValueTextSubView
                
                view.invisible =
                    value.isSemanticallyNull() && item.syncObject.localeFormat != Country.France
            }
        }
        return if (numberView == null || editMode) {
            numberView
        } else {
            ItemSubViewWithActionWrapper(
                numberView,
                CopyAction(item.toSummary(), CopyField.TaxNumber, action = {
                    logger.log(
                        UsageLogCode35(
                            type = UsageLogCode11.Type.FISCAL.code,
                            action = UsageLogCode35Action.COPY_NUMBER
                        )
                    )
                })
            )
        }
    }

    private fun createCountryField(
        context: Context,
        item: VaultItem<SyncObject.FiscalStatement>,
        editMode: Boolean,
        onlineNumberView: ItemSubView<String>?,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): ItemSubView<*> =
        createCountryField(
            context,
            item,
            editMode,
            object : ValueChangeManager.Listener<String> {
                override fun onValueChanged(origin: Any, newValue: String) {
                    onlineNumberView?.apply {
                        val view = this as ItemEditValueTextSubView
                        val country = Country.forLabelOrDefault(context, newValue)
                        
                        view.invisible =
                            value.isSemanticallyNull() && country != Country.France
                        listener.notifySubViewChanged(onlineNumberView)
                    }
                }
            }
        )

    private object FiscalStatementIdentityAdapter : IdentityAdapter<SyncObject.FiscalStatement> {
        override fun fullName(item: VaultItem<SyncObject.FiscalStatement>) = item.syncObject.fullname

        override fun withFullName(item: VaultItem<SyncObject.FiscalStatement>, fullName: String?):
                VaultItem<SyncObject.FiscalStatement> = item.copySyncObject { fullname = fullName }

        override fun gender(item: VaultItem<SyncObject.FiscalStatement>): SyncObject.Gender? = null

        override fun withGender(
            item: VaultItem<SyncObject.FiscalStatement>,
            gender: SyncObject.Gender?
        ): VaultItem<SyncObject.FiscalStatement> = item.copySyncObject { }

        override fun birthDate(item: VaultItem<SyncObject.FiscalStatement>) = null

        override fun withBirthDate(item: VaultItem<SyncObject.FiscalStatement>, birthDate: LocalDate?):
                VaultItem<SyncObject.FiscalStatement> = item.copySyncObject { }

        override fun linkedIdentity(item: VaultItem<SyncObject.FiscalStatement>) = item.syncObject.linkedIdentity

        override fun withLinkedIdentity(item: VaultItem<SyncObject.FiscalStatement>, identity: String?):
                VaultItem<SyncObject.FiscalStatement> =
            item.copySyncObject { linkedIdentity = identity }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedTeamspace(value: Teamspace): VaultItem<*> {
    this as VaultItem<SyncObject.FiscalStatement>
    val fiscalStatement = this.syncObject
    return if (value.teamId == fiscalStatement.spaceId) {
        this
    } else {
        this.copyWithAttrs { teamSpaceId = value.teamId }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedFiscalNumber(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.FiscalStatement>
    val fiscalStatement = this.syncObject
    return if (value == fiscalStatement.fiscalNumber.orEmpty()) {
        this
    } else {
        this.copySyncObject { fiscalNumber = value }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedOnlineNumber(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.FiscalStatement>
    val fiscalStatement = this.syncObject
    return if (value == fiscalStatement.teledeclarantNumber.orEmpty()) {
        this
    } else {
        this.copySyncObject { teledeclarantNumber = value }
    }
}
