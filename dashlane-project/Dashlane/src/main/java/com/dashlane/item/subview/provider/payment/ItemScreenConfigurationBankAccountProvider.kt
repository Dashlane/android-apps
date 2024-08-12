package com.dashlane.item.subview.provider.payment

import android.content.Context
import com.dashlane.R
import com.dashlane.design.component.compat.view.ThumbnailViewType
import com.dashlane.hermes.generated.definitions.Field
import com.dashlane.hermes.generated.definitions.ItemType
import com.dashlane.item.ItemEditViewContract
import com.dashlane.item.ScreenConfiguration
import com.dashlane.item.header.ItemHeader
import com.dashlane.item.subview.ItemScreenConfigurationProvider
import com.dashlane.item.subview.ItemSubView
import com.dashlane.item.subview.ItemSubViewWithActionWrapper
import com.dashlane.item.subview.ValueChangeManager
import com.dashlane.item.subview.action.CopyAction
import com.dashlane.item.subview.edit.ItemEditValueListSubView
import com.dashlane.item.subview.edit.ItemEditValueTextSubView
import com.dashlane.item.subview.provider.DateTimeFieldFactory
import com.dashlane.item.subview.provider.SubViewFactory
import com.dashlane.item.subview.provider.createCountryField
import com.dashlane.item.subview.readonly.ItemReadValueListSubView
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.util.BankDataProvider
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.obfuscated.matchesNullAsEmpty
import com.dashlane.util.obfuscated.toSyncObfuscatedValue
import com.dashlane.vault.VaultItemLogger
import com.dashlane.vault.model.CreditCardBank
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.model.forLabelOrDefault
import com.dashlane.vault.model.identityPartialOrFullNameNoLogin
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.utils.Country

@Suppress("LargeClass")
class ItemScreenConfigurationBankAccountProvider(
    private val teamSpaceAccessor: TeamSpaceAccessor,
    private val genericDataQuery: GenericDataQuery,
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
        item as VaultItem<SyncObject.BankStatement>
        return ScreenConfiguration(
            createSubViews(context, item, subViewFactory, editMode, canDelete, listener),
            createHeader(context, item)
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun hasEnoughDataToSave(itemToSave: VaultItem<*>): Boolean {
        itemToSave as VaultItem<SyncObject.BankStatement>
        return itemToSave.syncObject.bankAccountBIC?.toString()?.trim().isNotSemanticallyNull() ||
            itemToSave.syncObject.bankAccountIBAN?.toString()?.trim().isNotSemanticallyNull()
    }

    private fun createHeader(
        context: Context,
        item: VaultItem<SyncObject.BankStatement>
    ): ItemHeader {
        val bankTitle = if (item.syncObject.bankAccountName.isNullOrBlank()) {
            context.getString(R.string.bank_statement)
        } else {
            item.syncObject.bankAccountName
        }
        return ItemHeader(
            menuActions = createMenus(),
            title = bankTitle,
            thumbnailType = ThumbnailViewType.VAULT_ITEM_OTHER_ICON.value,
            thumbnailIconRes = getHeaderIcon(item.syncObject),
        )
    }

    private fun createSubViews(
        context: Context,
        item: VaultItem<SyncObject.BankStatement>,
        subViewFactory: SubViewFactory,
        editMode: Boolean,
        canDelete: Boolean,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): List<ItemSubView<*>> {
        val bankDataProvider = BankDataProvider.instance
        val otherLabel = context.getString(R.string.other)
        val bankView = createBankSpinnerField(context, item, editMode, bankDataProvider, otherLabel)
        val bicView = createBicField(subViewFactory, context, item, editMode)
        val ibanView = createIbanField(subViewFactory, context, item, editMode)

        return listOfNotNull(
            
            createNameField(editMode, subViewFactory, context, item),
            
            createCountryField(
                context, item, editMode, bicView, ibanView, bankView,
                bankDataProvider,
                otherLabel,
                listener
            ),
            
            bankView,
            
            createAccountHolderField(subViewFactory, context, item),
            
            bicView,
            
            ibanView,
            
            createTeamspaceField(subViewFactory, item),
            
            subViewFactory.createSubviewAttachmentDetails(context, item),
            
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

    private fun createTeamspaceField(
        subViewFactory: SubViewFactory,
        item: VaultItem<SyncObject.BankStatement>
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

    private fun createIbanField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.BankStatement>,
        editMode: Boolean
    ): ItemSubView<*>? {
        val iban = item.syncObject.bankAccountIBAN?.toString()
        val subview = subViewFactory.createSubViewString(
            context.getString(ibanHeaderRes(item.syncObject.localeFormat ?: Country.UnitedStates)),
            iban,
            true,
            VaultItem<*>::copyForUpdatedIban
        ) { ibanShown -> if (ibanShown) logRevealIban(item) }
        return if (editMode) {
            subview
        } else {
            subview?.let {
                ItemSubViewWithActionWrapper(
                    it,
                    CopyAction(
                        summaryObject = item.syncObject.toSummary(),
                        copyField = ibanCopyField(item.syncObject.localeFormat ?: Country.UnitedStates),
                        action = {},
                        vaultItemCopy = vaultItemCopy
                    )
                )
            }
        }
    }

    private fun createBicField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.BankStatement>,
        editMode: Boolean
    ): ItemSubView<*>? {
        val bic = item.syncObject.bankAccountBIC?.toString() ?: ""
        val bicHeaderRes = bicHeaderRes(item.syncObject.localeFormat ?: Country.UnitedStates)
        val subview = subViewFactory.createSubViewString(
            context.getString(bicHeaderRes),
            bic,
            true,
            VaultItem<*>::copyForUpdatedBic
        ) { bicShown -> if (bicShown) logRevealBic(item) }
        return if (editMode) {
            subview
        } else {
            subview?.let {
                ItemSubViewWithActionWrapper(
                    it,
                    CopyAction(
                        summaryObject = item.syncObject.toSummary(),
                        copyField = bicCopyField(
                            item.syncObject.localeFormat ?: Country.UnitedStates
                        ),
                        action = {},
                        vaultItemCopy = vaultItemCopy
                    )
                )
            }
        }
    }

    private fun createAccountHolderField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.BankStatement>
    ): ItemSubView<*>? {
        val identities = genericDataQuery
            .queryAll(genericFilter { specificDataType(SyncObjectType.IDENTITY) })
            .mapNotNull { (it as? SummaryObject.Identity)?.identityPartialOrFullNameNoLogin }
        return subViewFactory.createSubViewString(
            context.getString(R.string.bank_account_hint_account_holder),
            item.syncObject.bankAccountOwner,
            false,
            VaultItem<*>::copyForUpdatedOwner,
            identities
        )
    }

    private fun createCountryField(
        context: Context,
        item: VaultItem<SyncObject.BankStatement>,
        editMode: Boolean,
        bicView: ItemSubView<*>?,
        ibanView: ItemSubView<*>?,
        bankView: ItemSubView<*>?,
        bankDataProvider: BankDataProvider,
        otherLabel: String,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): ItemSubView<*> =
        createCountryField(
            context,
            item,
            editMode,
            object : ValueChangeManager.Listener<String> {
                override fun onValueChanged(origin: Any, newValue: String) {
                    
                    val newCountry = Country.forLabelOrDefault(context, newValue)
                    bankView?.apply {
                        val view = this as ItemEditValueListSubView
                        view.invisible = !bankDataProvider.isCountrySupported(newCountry)
                        view.values =
                            bankDataProvider.getCreditCardBankList(newCountry, otherLabel).map { it.first }
                        view.value = otherLabel
                        listener.notifySubViewChanged(this)
                    }
                    bicView?.apply {
                        val view = this as ItemEditValueTextSubView
                        view.hint = context.getString(bicHeaderRes(newCountry))
                        listener.notifySubViewChanged(this)
                    }
                    ibanView?.apply {
                        val view = this as ItemEditValueTextSubView
                        view.hint = context.getString(ibanHeaderRes(newCountry))
                        listener.notifySubViewChanged(this)
                    }
                }
            }
        )

    private fun createBankSpinnerField(
        context: Context,
        item: VaultItem<SyncObject.BankStatement>,
        editMode: Boolean,
        bankDataProvider: BankDataProvider,
        otherLabel: String
    ): ItemSubView<*>? {
        val bankHeader = context.getString(R.string.bank)
        val country = item.syncObject.localeFormat ?: Country.UnitedStates
        val bankList = bankDataProvider.getCreditCardBankList(country, otherLabel)
        val selectedBank = item.syncObject.bankAccountBank?.let {
            bankDataProvider.getBankName(it, otherLabel)
        } ?: otherLabel
        val bankUpdate = copyForUpdatedBank(bankDataProvider, otherLabel)
        return when {
            editMode -> ItemEditValueListSubView(
                bankHeader,
                selectedBank,
                bankList.map { it.first },
                bankUpdate
            ).also {
                it.invisible = !bankDataProvider.isCountrySupported(country)
            }
            !bankDataProvider.isCountrySupported(country) -> null
            else -> createBankSpinnerFieldWithCopyAction(
                bankHeader,
                selectedBank,
                bankList.map { it.first },
                item
            )
        }
    }

    private fun createBankSpinnerFieldWithCopyAction(
        bankHeader: String,
        selectedBank: String,
        bankList: List<String>,
        vaultItem: VaultItem<SyncObject.BankStatement>
    ): ItemSubView<*> {
        val itemReadValueListSubView = ItemReadValueListSubView(bankHeader, selectedBank, bankList)
        return ItemSubViewWithActionWrapper(
            itemReadValueListSubView,
            CopyAction(
                summaryObject = vaultItem.toSummary(),
                copyField = CopyField.BankAccountBank,
                vaultItemCopy = vaultItemCopy
            )
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun copyForUpdatedBank(
        bankDataProvider: BankDataProvider,
        otherLabel: String
    ): (Any, String) -> VaultItem<SyncObject.BankStatement> {
        return { it, value ->
            it as VaultItem<SyncObject.BankStatement>
            val bankStatement = it.syncObject
            val newBankDescriptor =
                bankDataProvider.getCreditCardBankListCurrentCountry(it, otherLabel)
                    .firstOrNull { value == it.first }?.second
            if (newBankDescriptor == bankStatement.bankAccountBank) {
                it
            } else {
                it.copySyncObject {
                    bankAccountBank = CreditCardBank(newBankDescriptor).bankDescriptor
                }
            }
        }
    }

    private fun createNameField(
        editMode: Boolean,
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.BankStatement>
    ): ItemSubView<*>? {
        return if (editMode) {
            subViewFactory.createSubViewString(
                context.getString(R.string.bank_account_hint_name),
                item.syncObject.bankAccountName,
                false,
                VaultItem<*>::copyForUpdatedName
            )
        } else {
            null
        }
    }

    private fun logRevealIban(item: VaultItem<SyncObject.BankStatement>) {
        vaultItemLogger.logRevealField(Field.IBAN, item.uid, ItemType.BANK_STATEMENT, null)
    }

    private fun logRevealBic(item: VaultItem<SyncObject.BankStatement>) {
        vaultItemLogger.logRevealField(Field.BIC, item.uid, ItemType.BANK_STATEMENT, null)
    }

    private fun bicHeaderRes(country: Country): Int = when (country) {
        Country.UnitedStates -> R.string.routing_number
        Country.UnitedKingdom -> R.string.sort_code
        else -> R.string.bic
    }

    private fun ibanHeaderRes(country: Country): Int = when (country) {
        Country.UnitedStates, Country.UnitedKingdom -> R.string.account_number
        Country.Mexico -> R.string.clabe
        else -> R.string.iban
    }

    private fun bicCopyField(country: Country): CopyField = when (country) {
        Country.UnitedStates -> CopyField.BankAccountRoutingNumber
        Country.UnitedKingdom -> CopyField.BankAccountSortCode
        else -> CopyField.BankAccountBicSwift
    }

    private fun ibanCopyField(country: Country): CopyField = when (country) {
        Country.UnitedStates, Country.UnitedKingdom -> CopyField.BankAccountAccountNumber
        Country.Mexico -> CopyField.BankAccountClabe
        else -> CopyField.BankAccountIban
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedOwner(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.BankStatement>
    val bankStatement = this.syncObject
    return if (value == bankStatement.bankAccountOwner.orEmpty()) {
        this
    } else {
        this.copySyncObject({ bankAccountOwner = value })
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedTeamspace(value: TeamSpace): VaultItem<*> {
    this as VaultItem<SyncObject.BankStatement>
    val bankStatement = this.syncObject
    return if (value.teamId == bankStatement.spaceId) {
        this
    } else {
        this.copyWithAttrs { teamSpaceId = value.teamId }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedIban(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.BankStatement>
    val bankStatement = this.syncObject
    return if (bankStatement.bankAccountIBAN.matchesNullAsEmpty(value)) {
        this
    } else {
        this.copySyncObject { bankAccountIBAN = value.toSyncObfuscatedValue() }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedBic(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.BankStatement>
    val bankStatement = this.syncObject
    return if (bankStatement.bankAccountBIC.matchesNullAsEmpty(value)) {
        this
    } else {
        this.copySyncObject { bankAccountBIC = value.toSyncObfuscatedValue() }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedName(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.BankStatement>
    val bankStatement = this.syncObject
    return if (value == bankStatement.bankAccountName.orEmpty()) {
        this
    } else {
        this.copySyncObject({ bankAccountName = value })
    }
}
