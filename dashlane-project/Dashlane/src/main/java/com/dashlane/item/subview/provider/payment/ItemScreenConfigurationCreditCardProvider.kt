package com.dashlane.item.subview.provider.payment

import android.content.Context
import androidx.core.content.ContextCompat
import com.dashlane.R
import com.dashlane.design.component.compat.view.ThumbnailViewType
import com.dashlane.hermes.generated.definitions.Field
import com.dashlane.hermes.generated.definitions.ItemType
import com.dashlane.item.ItemEditViewContract
import com.dashlane.item.ScreenConfiguration
import com.dashlane.item.header.ItemHeader
import com.dashlane.item.nfc.NfcHelper
import com.dashlane.item.subview.ItemScreenConfigurationProvider
import com.dashlane.item.subview.ItemSubView
import com.dashlane.item.subview.ItemSubViewWithActionWrapper
import com.dashlane.item.subview.ValueChangeManager
import com.dashlane.item.subview.action.CopyAction
import com.dashlane.item.subview.action.MenuAction
import com.dashlane.item.subview.action.payment.CreditCardColorMenuAction
import com.dashlane.item.subview.edit.ItemEditValueDateSubView
import com.dashlane.item.subview.edit.ItemEditValueListSubView
import com.dashlane.item.subview.edit.ItemEditValueTextSubView
import com.dashlane.item.subview.provider.DateTimeFieldFactory
import com.dashlane.item.subview.provider.SubViewFactory
import com.dashlane.item.subview.provider.createCountryField
import com.dashlane.item.subview.readonly.ItemReadValueDateSubView
import com.dashlane.item.subview.readonly.ItemReadValueListSubView
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.util.BankDataProvider
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.isSemanticallyNull
import com.dashlane.util.obfuscated.matchesNullAsEmpty
import com.dashlane.util.obfuscated.toSyncObfuscatedValue
import com.dashlane.util.time.atFirstDayOfMonth
import com.dashlane.util.time.yearMonth
import com.dashlane.util.tryOrNull
import com.dashlane.vault.VaultItemLogger
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.model.expireDate
import com.dashlane.vault.model.forLabelOrDefault
import com.dashlane.vault.model.getColorResource
import com.dashlane.vault.model.hasBeenSaved
import com.dashlane.vault.model.identityPartialOrFullNameNoLogin
import com.dashlane.vault.model.issueDate
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.vault.util.getFullAddress
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.utils.Country
import com.github.devnied.emvnfccard.model.EmvCard
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneOffset

class ItemScreenConfigurationCreditCardProvider(
    private val teamSpaceAccessor: TeamSpaceAccessor,
    private val genericDataQuery: GenericDataQuery,
    private val vaultItemLogger: VaultItemLogger,
    private val dateTimeFieldFactory: DateTimeFieldFactory,
    private val vaultItemCopy: VaultItemCopyService
) : ItemScreenConfigurationProvider() {

    private var selectedColor: SyncObject.PaymentCreditCard.Color? = null

    
    private var numberSubView: ItemSubView<String>? = null
    private var expirationDateSubView: ItemSubView<LocalDate?>? = null
    private var nameSubView: ItemSubView<String>? = null
    private var accountHolderSubView: ItemSubView<String>? = null

    
    private var securityCodeSubView: ItemSubView<String>? = null

    @Suppress("UNCHECKED_CAST")
    override fun createScreenConfiguration(
        context: Context,
        item: VaultItem<*>,
        subViewFactory: SubViewFactory,
        editMode: Boolean,
        canDelete: Boolean,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): ScreenConfiguration {
        item as VaultItem<SyncObject.PaymentCreditCard>
        return ScreenConfiguration(
            createSubViews(context, item, subViewFactory, editMode, canDelete, listener),
            createHeader(
                context,
                item,
                item.syncObject.color.getColorResource(),
                editMode,
                listener
            )
        )
    }

    fun updateCreditCardInfoWithEmvCard(card: EmvCard, listener: ItemEditViewContract.View.UiUpdateListener) {
        
        numberSubView?.apply {
            value = card.cardNumber ?: ""
            listener.notifySubViewChanged(this)
        }
        
        nameSubView?.apply {
            value = card.type?.getName() ?: ""
            listener.notifySubViewChanged(this)
        }
        
        card.expireDate?.let {
            
            val localDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(it.time), ZoneOffset.UTC)
                .withDayOfMonth(1)
                .plusMonths(1)
            expirationDateSubView?.notifyValueChanged(localDate.toLocalDate())
        }
        
        accountHolderSubView?.apply {
            val holderName = buildString {
                card.holderFirstname?.let {
                    append(it)
                }
                card.holderLastname?.let {
                    if (isNotEmpty()) {
                        append(" ")
                    }
                    append(it)
                }
            }

            if (holderName.isNotEmpty()) {
                notifyValueChanged(holderName)
            }
        }

        if (card.cardNumber.isSemanticallyNull()) {
            
            listener.showNfcErrorDialog()
        } else {
            listener.showNfcSuccessDialog(securityCodeSubView)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onSetupEnd(
        context: Context,
        item: VaultItem<*>,
        editMode: Boolean,
        listener: ItemEditViewContract.View.UiUpdateListener,
        hasPreviousState: Boolean
    ) {
        item as VaultItem<SyncObject.PaymentCreditCard>
        super.onSetupEnd(context, item, editMode, listener, hasPreviousState)
        
        if (editMode) mayAskForNfc(context, item, listener)

        if (hasPreviousState) {
            selectedColor?.let {
                
                listener.notifyHeaderChanged(
                    createHeader(context, item, it.getColorResource(), editMode, listener),
                    editMode
                )
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun hasEnoughDataToSave(itemToSave: VaultItem<*>): Boolean {
        itemToSave as VaultItem<SyncObject.PaymentCreditCard>
        return itemToSave.syncObject.cardNumber?.toString()?.trim().isNotSemanticallyNull()
    }

    private fun createHeader(
        context: Context,
        item: VaultItem<SyncObject.PaymentCreditCard>,
        colorResource: Int,
        editMode: Boolean,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): ItemHeader {
        val menuActions = mutableListOf<MenuAction>()
        if (editMode) {
            
            val colorSelectAction: (SyncObject.PaymentCreditCard.Color) -> Unit = {
                selectedColor = it
                
                listener.notifyHeaderChanged(
                    createHeader(context, item, it.getColorResource(), true, listener),
                    true
                )
            }
            val colorMenuUpdate = copyForUpdatedColor()
            menuActions.add(CreditCardColorMenuAction(item.toSummary(), colorSelectAction, colorMenuUpdate))
        }
        menuActions.addAll(createMenus())

        val creditCardTitle = if (item.syncObject.name.isNullOrBlank()) {
            context.getString(R.string.creditcard_new_label)
        } else {
            item.syncObject.name
        }
        return ItemHeader(
            menuActions = menuActions,
            title = creditCardTitle,
            thumbnailType = ThumbnailViewType.VAULT_ITEM_LEGACY_OTHER_ICON.value,
            thumbnailIconRes = getHeaderIcon(item.syncObject),
            thumbnailColor = ContextCompat.getColor(context, colorResource)
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun copyForUpdatedColor(): (VaultItem<*>) -> VaultItem<SyncObject.PaymentCreditCard> {
        return {
            it as VaultItem<SyncObject.PaymentCreditCard>
            if (selectedColor == null || selectedColor == it.syncObject.color) {
                it
            } else {
                it.copySyncObject { color = selectedColor }
            }
        }
    }

    private fun createSubViews(
        context: Context,
        item: VaultItem<SyncObject.PaymentCreditCard>,
        subViewFactory: SubViewFactory,
        editMode: Boolean,
        canDelete: Boolean,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): List<ItemSubView<*>> {
        
        val bankDataProvider = BankDataProvider.instance
        val otherLabel = context.getString(R.string.other)
        val bankView = createBankSpinnerField(context, item, bankDataProvider, otherLabel, editMode)
        
        val issueDateView = createIssueDateField(item, editMode, context, listener)
        
        val issueNumberView = createIssueNumberField(editMode, item, subViewFactory, context)

        accountHolderSubView = createAccountHolderField(subViewFactory, context, item)
        numberSubView = createNumberField(subViewFactory, context, item, editMode)
        expirationDateSubView = createExpirationDateField(item, editMode, context, listener)
        nameSubView = createNameField(editMode, subViewFactory, context, item)
        securityCodeSubView = createSecurityCodeField(subViewFactory, context, item, editMode)

        return listOfNotNull(
            
            accountHolderSubView,
            
            numberSubView,
            
            securityCodeSubView,
            
            expirationDateSubView,
            issueNumberView,
            issueDateView,
            
            createCountryField(
                context,
                item,
                editMode,
                issueDateView,
                issueNumberView,
                bankView,
                bankDataProvider,
                otherLabel,
                listener
            ),
            bankView,
            
            nameSubView,
            
            createAddressSpinnerField(context, item, editMode),
            
            createTeamspaceField(subViewFactory, item),
            
            createNoteField(subViewFactory, context, item),
            
            subViewFactory.createSubviewAttachmentDetails(context, item),
            
            dateTimeFieldFactory.createCreationDateField(editMode = editMode, context = context, item = item),
            
            dateTimeFieldFactory.createLatestUpdateDateField(editMode = editMode, context = context, item = item),
            
            subViewFactory.createSubviewDelete(context, listener, canDelete)
        )
    }

    private fun createNoteField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.PaymentCreditCard>
    ): ItemSubView<*>? {
        return subViewFactory.createSubViewString(
            context.getString(R.string.credit_card_hint_pin_code),
            item.syncObject.cCNote?.toString(),
            true,
            VaultItem<*>::copyForUpdatedNote
        ) { notesField ->
            if (notesField) logRevealNotes(item)
        }
    }

    private fun createTeamspaceField(
        subViewFactory: SubViewFactory,
        item: VaultItem<SyncObject.PaymentCreditCard>
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

    private fun createAddressSpinnerField(
        context: Context,
        item: VaultItem<SyncObject.PaymentCreditCard>,
        editMode: Boolean
    ): ItemSubView<*>? {
        val noneLabel = context.getString(R.string.none)
        val addressList = initializeAddressList(context, noneLabel)

        return if (addressList.isNotEmpty()) {
            val selectedAddress = addressList.firstOrNull { it.second == item.syncObject.linkedBillingAddress }?.first
                ?: noneLabel
            val address = context.getString(R.string.address)
            when {
                editMode -> ItemEditValueListSubView(
                    address,
                    selectedAddress,
                    addressList.map { it.first }
                ) { it, value -> it.copyForUpdatedBillingAddress(addressList, value) }
                else -> ItemReadValueListSubView(address, selectedAddress, addressList.map { it.first })
            }
        } else {
            null
        }
    }

    private fun createNameField(
        editMode: Boolean,
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.PaymentCreditCard>
    ): ItemSubView<String>? {
        return if (editMode) {
            subViewFactory.createSubViewString(
                context.getString(R.string.credit_card_hint_name),
                item.syncObject.name,
                false,
                VaultItem<*>::copyForUpdatedName
            )
        } else {
            null
        }
    }

    private fun createCountryField(
        context: Context,
        item: VaultItem<SyncObject.PaymentCreditCard>,
        editMode: Boolean,
        issueDateView: ItemSubView<*>?,
        issueNumberView: ItemSubView<*>?,
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
                    issueDateView?.apply {
                        val view = this as ItemEditValueDateSubView
                        view.invisible =
                            value == null && newCountry !in COUNTRIES_WITH_ADVANCED_FIELDS
                        listener.notifySubViewChanged(this)
                    }
                    issueNumberView?.apply {
                        val view = this as ItemEditValueTextSubView
                        view.invisible =
                            value.isSemanticallyNull() && newCountry !in COUNTRIES_WITH_ADVANCED_FIELDS
                        listener.notifySubViewChanged(this)
                    }
                    bankView?.apply {
                        val view = this as ItemEditValueListSubView
                        view.invisible = !bankDataProvider.isCountrySupported(newCountry)
                        view.values =
                            bankDataProvider.getCreditCardBankList(newCountry, otherLabel)
                                .map { it.first }
                        view.value = otherLabel
                        listener.notifySubViewChanged(this)
                    }
                }
            }
        )

    private fun createBankSpinnerField(
        context: Context,
        item: VaultItem<SyncObject.PaymentCreditCard>,
        bankDataProvider: BankDataProvider,
        otherLabel: String,
        editMode: Boolean
    ): ItemSubView<*>? {
        val bankHeader = context.getString(R.string.bank)
        val country = item.syncObject.localeFormat ?: Country.UnitedStates
        val bankList = bankDataProvider.getCreditCardBankList(country, otherLabel)
        val selectedBank = item.syncObject.bank?.let {
            tryOrNull { bankDataProvider.getBankName(it, otherLabel) }
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
            else -> ItemReadValueListSubView(bankHeader, selectedBank, bankList.map { it.first })
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun copyForUpdatedBank(
        bankDataProvider: BankDataProvider,
        otherLabel: String
    ): (Any, String) -> VaultItem<SyncObject.PaymentCreditCard> {
        return { it, value ->
            val vaultItem = it as VaultItem<SyncObject.PaymentCreditCard>
            val creditCard = vaultItem.syncObject
            
            val newBankDescriptor =
                bankDataProvider.getCreditCardBankListCurrentCountry(vaultItem, otherLabel)
                    .firstOrNull { value == it.first }?.second
            if (newBankDescriptor == creditCard.bank) {
                vaultItem
            } else {
                vaultItem.copySyncObject { bank = newBankDescriptor }
            }
        }
    }

    private fun createIssueDateField(
        item: VaultItem<SyncObject.PaymentCreditCard>,
        editMode: Boolean,
        context: Context,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): ItemSubView<*>? {
        val issueDate = item.syncObject.issueDate

        val needAdvancedFields = item.syncObject.localeFormat in COUNTRIES_WITH_ADVANCED_FIELDS

        return when {
            editMode -> {
                ItemEditValueDateSubView(
                    context.getString(R.string.issue_date),
                    issueDate?.atFirstDayOfMonth(),
                    issueDate?.formatToShortDate(),
                    VaultItem<*>::copyForUpdatedIssueDate
                ).apply {
                    addValueChangedListener(object : ValueChangeManager.Listener<LocalDate?> {
                        override fun onValueChanged(origin: Any, newValue: LocalDate?) {
                            val subView = this@apply
                            subView.formattedDate = newValue?.yearMonth()?.formatToShortDate()
                            subView.value = newValue
                            listener.notifySubViewChanged(this@apply)
                        }
                    })
                    
                    val view = this
                    view.invisible = issueDate == null && !needAdvancedFields
                }
            }
            needAdvancedFields || issueDate != null -> ItemReadValueDateSubView(
                context.getString(R.string.issue_date),
                issueDate?.atFirstDayOfMonth(),
                issueDate?.formatToShortDate()
            )
            else -> null
        }
    }

    private fun createIssueNumberField(
        editMode: Boolean,
        item: VaultItem<SyncObject.PaymentCreditCard>,
        subViewFactory: SubViewFactory,
        context: Context
    ): ItemSubView<*>? {
        val needAdvancedFields = item.syncObject.localeFormat in COUNTRIES_WITH_ADVANCED_FIELDS

        return if (editMode || needAdvancedFields || item.syncObject.issueNumber.isNotSemanticallyNull()) {
            subViewFactory.createSubViewString(
                context.getString(R.string.credit_card_hint_issue_number),
                item.syncObject.issueNumber,
                false,
                VaultItem<*>::copyForUpdatedIssueNumber
            ).apply {
                if (editMode) {
                    
                    val view = this as ItemEditValueTextSubView
                    view.invisible =
                        item.syncObject.issueNumber.isSemanticallyNull() && !needAdvancedFields
                }
            }
        } else {
            null
        }
    }

    private fun createExpirationDateField(
        item: VaultItem<SyncObject.PaymentCreditCard>,
        editMode: Boolean,
        context: Context,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): ItemSubView<LocalDate?>? {
        val expirationDate = item.syncObject.expireDate
        return when {
            editMode -> ItemEditValueDateSubView(
                context.getString(R.string.expiery_date),
                expirationDate?.atFirstDayOfMonth(),
                expirationDate?.formatToShortDate(),
                VaultItem<*>::copyForUpdatedExpirationDate
            ).apply {
                addValueChangedListener(object : ValueChangeManager.Listener<LocalDate?> {
                    override fun onValueChanged(origin: Any, newValue: LocalDate?) {
                        val subView = this@apply
                        subView.formattedDate = newValue?.yearMonth()?.formatToShortDate()
                        subView.value = newValue
                        listener.notifySubViewChanged(this@apply)
                    }
                })
            }
            expirationDate != null -> createExpirationDateFieldWithCopyAction(context, item, expirationDate)
            else -> null
        }
    }

    private fun createExpirationDateFieldWithCopyAction(
        context: Context,
        item: VaultItem<SyncObject.PaymentCreditCard>,
        expirationDate: YearMonth
    ): ItemSubView<LocalDate?> {
        val view = ItemReadValueDateSubView(
            context.getString(R.string.expiery_date),
            expirationDate.atFirstDayOfMonth(),
            expirationDate.formatToShortDate()
        )

        return ItemSubViewWithActionWrapper(
            view,
            CopyAction(
                summaryObject = item.toSummary(),
                copyField = CopyField.PaymentsExpirationDate,
                vaultItemCopy = vaultItemCopy
            )
        )
    }

    private fun createSecurityCodeField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.PaymentCreditCard>,
        editMode: Boolean
    ): ItemSubView<String>? {
        val securityCode = item.syncObject.securityCode?.toString()
        val securityCodeView = subViewFactory.createSubViewNumber(
            context.getString(R.string.credit_card_hint_security_code),
            securityCode,
            SubViewFactory.INPUT_TYPE_NUMBER,
            true,
            VaultItem<*>::copyForUpdatedSecurityCode
        ) { securityCodeShown ->
            if (securityCodeShown) logRevealCVV(item)
        }
        return if (securityCodeView == null || editMode) {
            securityCodeView
        } else {
            ItemSubViewWithActionWrapper(
                securityCodeView,
                CopyAction(
                    summaryObject = item.toSummary(),
                    copyField = CopyField.PaymentsSecurityCode,
                    action = {},
                    vaultItemCopy = vaultItemCopy
                )
            )
        }
    }

    private fun createNumberField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.PaymentCreditCard>,
        editMode: Boolean
    ): ItemSubView<String>? {
        val cardNumberView = subViewFactory.createSubViewNumber(
            context.getString(R.string.credit_card_hint_number),
            item.syncObject.cardNumber?.toString(),
            SubViewFactory.INPUT_TYPE_NUMBER,
            true,
            VaultItem<*>::copyForUpdatedCardNumber
        ) { numberShown ->
            if (numberShown) logRevealCardNumber(item)
        }
        return if (cardNumberView == null || editMode) {
            cardNumberView
        } else {
            ItemSubViewWithActionWrapper(
                cardNumberView,
                CopyAction(
                    summaryObject = item.toSummary(),
                    copyField = CopyField.PaymentsNumber,
                    action = {},
                    vaultItemCopy = vaultItemCopy
                )
            )
        }
    }

    private fun createAccountHolderField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.PaymentCreditCard>
    ): ItemSubView<String>? {
        val identities = genericDataQuery
            .queryAll(genericFilter { specificDataType(SyncObjectType.IDENTITY) })
            .mapNotNull { (it as? SummaryObject.Identity)?.identityPartialOrFullNameNoLogin }
        return subViewFactory.createSubViewString(
            context.getString(R.string.credit_card_hint_account_holder),
            item.syncObject.ownerName,
            false,
            VaultItem<*>::copyForUpdatedOwner,
            identities
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun initializeAddressList(context: Context, defaultLabel: String): List<Pair<String, String?>> {
        val addressList = arrayListOf<Pair<String, String?>>()
        addressList.add(Pair(defaultLabel, null))

        val addressDataIdentifierList = genericDataQuery.queryAll(
            genericFilter { specificDataType(SyncObjectType.ADDRESS) }
        )
        addressDataIdentifierList.forEach { summaryObject ->
            summaryObject as SummaryObject.Address
            summaryObject.let {
                addressList.add(Pair(it.getFullAddress(context), it.id))
            }
        }
        return addressList
    }

    private fun mayAskForNfc(
        context: Context,
        creditCard: VaultItem<SyncObject.PaymentCreditCard>,
        listener: ItemEditViewContract.View.UiUpdateListener
    ) {
        val isNew = !creditCard.hasBeenSaved
        if (numberSubView?.value.isNullOrEmpty() && isNew) {
            val isNfcAvailable =
                NfcHelper.isNfcAvailable(context) && NfcHelper.isNfcEnabled(context)
            if (isNfcAvailable) {
                listener.showNfcPromptDialog()
            }
        }
    }

    private fun logRevealNotes(item: VaultItem<SyncObject.PaymentCreditCard>) {
        vaultItemLogger.logRevealField(Field.NOTE, item.uid, ItemType.CREDIT_CARD, null)
    }

    private fun logRevealCVV(item: VaultItem<SyncObject.PaymentCreditCard>) {
        vaultItemLogger.logRevealField(Field.SECURITY_CODE, item.uid, ItemType.CREDIT_CARD, null)
    }

    private fun logRevealCardNumber(item: VaultItem<SyncObject.PaymentCreditCard>) {
        vaultItemLogger.logRevealField(Field.CARD_NUMBER, item.uid, ItemType.CREDIT_CARD, null)
    }

    companion object {
        
        val COUNTRIES_WITH_ADVANCED_FIELDS = setOf(
            Country.Argentina,
            Country.Brazil,
            Country.Chile,
            Country.Colombia,
            Country.UnitedKingdom,
            Country.Japan,
            Country.Mexico,
            Country.Norway,
            Country.Peru,
            Country.Portugal,
            Country.Sweden
        )
    }
}

private fun YearMonth.formatToShortDate() =
    "%02d - %02d".format(monthValue, year % 100)

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedNote(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.PaymentCreditCard>
    val creditCard = this.syncObject
    return if (creditCard.cCNote.matchesNullAsEmpty(value)) {
        this
    } else {
        this.copySyncObject { cCNote = value.toSyncObfuscatedValue() }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedTeamspace(value: TeamSpace): VaultItem<*> {
    this as VaultItem<SyncObject.PaymentCreditCard>
    val creditCard = this.syncObject
    return if (value.teamId == creditCard.spaceId) {
        this
    } else {
        this.copyWithAttrs { teamSpaceId = value.teamId }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedBillingAddress(
    addressList: List<Pair<String, String?>>,
    value: String
): VaultItem<*> {
    this as VaultItem<SyncObject.PaymentCreditCard>
    val creditCard = this.syncObject
    val addressUid = addressList.firstOrNull { it.first == value }?.second
    return if (addressUid == creditCard.linkedBillingAddress) {
        this
    } else {
        this.copySyncObject { linkedBillingAddress = addressUid.toString() }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedName(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.PaymentCreditCard>
    val creditCard = this.syncObject
    return if (value == creditCard.name.orEmpty()) {
        this
    } else {
        this.copySyncObject { name = value }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedIssueDate(value: LocalDate?): VaultItem<*> {
    this as VaultItem<SyncObject.PaymentCreditCard>
    val issueDate = value?.yearMonth()
    return if (issueDate != syncObject.issueDate) {
        copySyncObject { this.issueDate = issueDate }
    } else {
        this
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedIssueNumber(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.PaymentCreditCard>
    val creditCard = this.syncObject
    return if (value == creditCard.issueNumber.orEmpty()) {
        this
    } else {
        this.copySyncObject { issueNumber = value }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedExpirationDate(value: LocalDate?): VaultItem<*> {
    this as VaultItem<SyncObject.PaymentCreditCard>
    val expireDate = value?.yearMonth()
    return if (expireDate != syncObject.expireDate) {
        copySyncObject { this.expireDate = expireDate }
    } else {
        this
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedSecurityCode(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.PaymentCreditCard>
    val creditCard = this.syncObject
    return if (creditCard.securityCode.matchesNullAsEmpty(value)) {
        this
    } else {
        this.copySyncObject { securityCode = value.toSyncObfuscatedValue() }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedCardNumber(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.PaymentCreditCard>
    val creditCard = this.syncObject
    return if (creditCard.cardNumber.matchesNullAsEmpty(value)) {
        this
    } else {
        this.copySyncObject { cardNumber = value.toSyncObfuscatedValue() }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedOwner(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.PaymentCreditCard>
    val creditCard = this.syncObject
    return if (value == creditCard.ownerName.orEmpty()) {
        this
    } else {
        this.copySyncObject { ownerName = value }
    }
}
