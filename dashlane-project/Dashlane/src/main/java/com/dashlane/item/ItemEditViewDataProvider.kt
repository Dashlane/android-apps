package com.dashlane.item

import android.content.Context
import android.os.Bundle
import com.dashlane.R
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.hermes.generated.definitions.Action
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.item.linkedwebsites.getRemovedLinkedApps
import com.dashlane.item.linkedwebsites.getUpdatedLinkedWebsites
import com.dashlane.item.subview.ItemScreenConfigurationProvider
import com.dashlane.item.subview.ItemSubView
import com.dashlane.item.subview.provider.DateTimeFieldFactory
import com.dashlane.item.subview.provider.EditableSubViewFactory
import com.dashlane.item.subview.provider.ReadOnlySubViewFactory
import com.dashlane.item.subview.provider.SubViewFactory
import com.dashlane.item.subview.provider.credential.ItemScreenConfigurationPasskeyProvider
import com.dashlane.item.subview.provider.id.ItemScreenConfigurationDriverLicenseProvider
import com.dashlane.item.subview.provider.id.ItemScreenConfigurationFiscalStatementProvider
import com.dashlane.item.subview.provider.id.ItemScreenConfigurationIdCardProvider
import com.dashlane.item.subview.provider.id.ItemScreenConfigurationPassportProvider
import com.dashlane.item.subview.provider.id.ItemScreenConfigurationSocialSecurityProvider
import com.dashlane.item.subview.provider.payment.ItemScreenConfigurationBankAccountProvider
import com.dashlane.item.subview.provider.personalinfo.ItemScreenConfigurationAddressProvider
import com.dashlane.item.subview.provider.personalinfo.ItemScreenConfigurationCompanyProvider
import com.dashlane.item.subview.provider.personalinfo.ItemScreenConfigurationEmailProvider
import com.dashlane.item.subview.provider.personalinfo.ItemScreenConfigurationIdentityProvider
import com.dashlane.item.subview.provider.personalinfo.ItemScreenConfigurationPhoneProvider
import com.dashlane.item.subview.provider.personalinfo.ItemScreenConfigurationWebsiteProvider
import com.dashlane.lock.LockManager
import com.dashlane.sharingpolicy.SharingPolicyDataProvider
import com.dashlane.storage.userdata.EmailSuggestionProvider
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.copyWithDefaultValue
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.sync.DataSync
import com.dashlane.teamspaces.getTeamSpaceLog
import com.dashlane.teamspaces.manager.TeamSpaceAccessorProvider
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.util.date.RelativeDateFormatter
import com.dashlane.util.date.RelativeDateFormatterImpl
import com.dashlane.util.isSemanticallyNull
import com.dashlane.util.tryOrNull
import com.dashlane.vault.VaultItemLogger
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.createAddress
import com.dashlane.vault.model.createBankStatement
import com.dashlane.vault.model.createCompany
import com.dashlane.vault.model.createDriverLicence
import com.dashlane.vault.model.createEmail
import com.dashlane.vault.model.createFiscalStatement
import com.dashlane.vault.model.createIdCard
import com.dashlane.vault.model.createIdentity
import com.dashlane.vault.model.createPassport
import com.dashlane.vault.model.createPersonalWebsite
import com.dashlane.vault.model.createPhone
import com.dashlane.vault.model.createSocialSecurityStatement
import com.dashlane.vault.model.getDefaultCountry
import com.dashlane.vault.model.hasBeenSaved
import com.dashlane.vault.toItemType
import com.dashlane.vault.util.BankDataProvider
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.skocken.presentation.provider.BaseDataProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

class ItemEditViewDataProvider @Inject constructor(
    private val teamSpaceAccessorProvider: TeamSpaceAccessorProvider,
    private val currentTeamSpaceFilter: CurrentTeamSpaceUiFilter,
    private val vaultDataQuery: VaultDataQuery,
    private val dataSaver: DataSaver,
    private val genericDataQuery: GenericDataQuery,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val emailSuggestionProvider: EmailSuggestionProvider,
    private val lockManager: LockManager,
    private val vaultItemLogger: VaultItemLogger,
    private val sharingPolicy: SharingPolicyDataProvider,
    private val dataSync: DataSync,
    private val vaultItemCopy: VaultItemCopyService,
    private val bankDataProvider: BankDataProvider,
    clock: Clock,
) : BaseDataProvider<ItemEditViewContract.Presenter>(), ItemEditViewContract.DataProvider {
    private val relativeDateFormatter: RelativeDateFormatter by lazy {
        RelativeDateFormatterImpl(clock = clock)
    }
    private val dateTimeFieldFactory: DateTimeFieldFactory by lazy {
        DateTimeFieldFactory(
            clock = clock,
            relativeDateFormatter = relativeDateFormatter,
        )
    }

    private var state: State? = null

    private var logDisplayPendingSetup = false

    override val vaultItem: VaultItem<*>
        get() = state!!.item

    override val isEditMode: Boolean
        get() = state!!.editMode

    override val isSetup: Boolean
        get() = state != null

    @Suppress("LongMethod")
    override suspend fun setup(
        context: Context,
        options: ItemEditViewSetupOptions,
        listener: ItemEditViewContract.View.UiUpdateListener,
    ): Boolean {
        lockManager.waitUnlock()

        val teamSpaceAccessor = teamSpaceAccessorProvider.get()
        
        teamSpaceAccessor ?: return false
        val provider: ItemScreenConfigurationProvider?
        var item: VaultItem<*>?
        val canDelete: Boolean
        if (options.uid.isSemanticallyNull()) {
            when (options.dataType) {
                SyncObjectType.AUTHENTIFIANT ->
                    error(
                        "Authentifiant have been removed from old item edit",
                    )
                SyncObjectType.IDENTITY -> item = createIdentity(title = SyncObject.Identity.Title.MR)
                SyncObjectType.EMAIL -> {
                    val mailType = if (teamSpaceAccessor.hasEnforcedTeamSpace) {
                        SyncObject.Email.Type.PRO
                    } else {
                        SyncObject.Email.Type.PERSO
                    }
                    item = createEmail(type = mailType)
                }
                SyncObjectType.PERSONAL_WEBSITE -> item = createPersonalWebsite()
                SyncObjectType.COMPANY -> item = createCompany()
                SyncObjectType.FISCAL_STATEMENT -> item = createFiscalStatement()
                SyncObjectType.SECURE_NOTE ->
                    error(
                        "Secure Note have been removed from old item edit",
                    )
                SyncObjectType.ADDRESS -> {
                    val addressCountry = context.getDefaultCountry()
                    val state = com.dashlane.core.domain.State.getStatesForCountry(addressCountry)
                        .firstOrNull()?.stateDescriptor
                    item = createAddress(
                        name = context.getString(R.string.address),
                        state = state,
                        addressCountry = addressCountry,
                    )
                }

                SyncObjectType.PHONE -> item = createPhone(
                    type = SyncObject.Phone.Type.PHONE_TYPE_MOBILE,
                    phoneName = context.getString(R.string.phone),
                )

                SyncObjectType.PAYMENT_CREDIT_CARD ->
                    error(
                        "Payment Credit Card have been removed from old item edit",
                    )
                SyncObjectType.BANK_STATEMENT -> item = createBankStatement()
                SyncObjectType.ID_CARD -> item = createIdCard()
                SyncObjectType.PASSPORT -> item = createPassport()
                SyncObjectType.DRIVER_LICENCE -> item = createDriverLicence()
                SyncObjectType.SOCIAL_SECURITY_STATEMENT -> item = createSocialSecurityStatement()
                else -> return false
            }
            
            when (item.syncObject) {
                is SyncObject.BankStatement -> item = item.copyWithDefaultValue(context)
                else -> item.copyWithAttrs { formatLang = context.getDefaultCountry() }
            }
            canDelete = sharingPolicy.isDeleteAllowed(true, item)
        } else {
            item = getItem(options.dataType, options.uid!!)
            if (item == null) {
                
                return false
            }
            canDelete = sharingPolicy.isDeleteAllowed(false, item)
        }

        when (options.dataType) {
            SyncObjectType.IDENTITY ->
                provider = ItemScreenConfigurationIdentityProvider(
                    teamSpaceAccessor = teamSpaceAccessor,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                )

            SyncObjectType.EMAIL ->
                provider = ItemScreenConfigurationEmailProvider(
                    teamSpaceAccessor = teamSpaceAccessor,
                    emailSuggestionProvider = emailSuggestionProvider,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy,
                )

            SyncObjectType.PERSONAL_WEBSITE ->
                provider = ItemScreenConfigurationWebsiteProvider(
                    teamSpaceAccessor = teamSpaceAccessor,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy,
                )

            SyncObjectType.COMPANY ->
                provider = ItemScreenConfigurationCompanyProvider(
                    teamSpaceAccessor = teamSpaceAccessor,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                )

            SyncObjectType.FISCAL_STATEMENT ->
                provider = ItemScreenConfigurationFiscalStatementProvider(
                    teamSpaceAccessor = teamSpaceAccessor,
                    vaultDataQuery = vaultDataQuery,
                    genericDataQuery = genericDataQuery,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy,
                )

            SyncObjectType.SECURE_NOTE ->
                error(
                    "Secure Note have been removed from old item edit",
                )

            SyncObjectType.ADDRESS ->
                provider = ItemScreenConfigurationAddressProvider(
                    genericDataQuery = genericDataQuery,
                    teamSpaceAccessor = teamSpaceAccessor,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy,
                )

            SyncObjectType.PHONE ->
                provider = ItemScreenConfigurationPhoneProvider(
                    teamSpaceAccessor = teamSpaceAccessor,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy,
                )

            SyncObjectType.PAYMENT_CREDIT_CARD ->
                error(
                    "Payment Credit Card have been removed from old item edit",
                )

            SyncObjectType.BANK_STATEMENT ->
                provider = ItemScreenConfigurationBankAccountProvider(
                    teamSpaceAccessor = teamSpaceAccessor,
                    genericDataQuery = genericDataQuery,
                    vaultItemLogger = vaultItemLogger,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy,
                    bankDataProvider = bankDataProvider,
                )

            SyncObjectType.ID_CARD ->
                provider = ItemScreenConfigurationIdCardProvider(
                    teamspaceAccessor = teamSpaceAccessor,
                    genericDataQuery = genericDataQuery,
                    vaultDataQuery = vaultDataQuery,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy,
                )

            SyncObjectType.PASSPORT ->
                provider = ItemScreenConfigurationPassportProvider(
                    teamSpaceAccessor = teamSpaceAccessor,
                    genericDataQuery = genericDataQuery,
                    vaultDataQuery = vaultDataQuery,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy,
                )

            SyncObjectType.DRIVER_LICENCE ->
                provider = ItemScreenConfigurationDriverLicenseProvider(
                    teamSpaceAccessor = teamSpaceAccessor,
                    genericDataQuery = genericDataQuery,
                    vaultDataQuery = vaultDataQuery,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy,
                )

            SyncObjectType.SOCIAL_SECURITY_STATEMENT ->
                provider = ItemScreenConfigurationSocialSecurityProvider(
                    teamSpaceAccessor = teamSpaceAccessor,
                    genericDataQuery = genericDataQuery,
                    vaultDataQuery = vaultDataQuery,
                    vaultItemLogger = vaultItemLogger,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy,
                )

            SyncObjectType.PASSKEY ->
                provider = ItemScreenConfigurationPasskeyProvider(
                    teamSpaceAccessor = teamSpaceAccessor,
                    vaultItemLogger = vaultItemLogger,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy,
                )

            else -> return false
        }

        
        options.savedAdditionalData?.let {
            provider.restoreAdditionalData(it)
        }

        
        val screenConfiguration = provider.createScreenConfiguration(
            context,
            item,
            getSubViewFactory(options.editMode),
            options.editMode,
            canDelete,
            listener,
        ).apply {
            
            options.savedScreenConfiguration?.let {
                
                if (options.editMode) restoreState(it, teamSpaceAccessor)
            }
        }
        state = State(
            options.dataType,
            item,
            options.editMode,
            canDelete,
            provider,
            screenConfiguration,
            listener,
        )
        if (logDisplayPendingSetup) {
            logViewDisplay()
        }
        return true
    }

    override suspend fun onSetupEnd(
        context: Context,
        options: ItemEditViewSetupOptions,
        listener: ItemEditViewContract.View.UiUpdateListener,
    ) {
        state!!.itemScreenConfigurationProvider.onSetupEnd(
            context,
            state!!.item,
            options.editMode,
            listener,
            options.savedScreenConfiguration != null,
        )
        onItemViewed()
    }

    override fun logViewDisplay() {
        val state = state
        logDisplayPendingSetup = state == null
    }

    override fun getAdditionalData(): Bundle = state!!.itemScreenConfigurationProvider.saveAdditionalData()

    override fun changeMode(context: Context, editMode: Boolean, listener: ItemEditViewContract.View.UiUpdateListener) {
        if (state == null) {
            return
        }
        val canDelete = sharingPolicy.isDeleteAllowed(false, state!!.item)
        state = state!!.copy(
            editMode = editMode,
            screenConfiguration = state!!.itemScreenConfigurationProvider.createScreenConfiguration(
                context,
                state!!.item,
                getSubViewFactory(editMode),
                editMode,
                canDelete,
                listener,
            ),
            canDelete = canDelete,
        )
    }

    override fun getScreenConfiguration(): ScreenConfiguration {
        return state!!.screenConfiguration
    }

    @Suppress("UNCHECKED_CAST", "LongMethod")
    override suspend fun save(context: Context, subViews: List<ItemSubView<*>>, newAttachments: String?): Boolean {
        val state = state ?: return false
        var itemToSave = state.itemScreenConfigurationProvider.gatherFromUi(
            state.item,
            state.screenConfiguration.itemSubViews,
            state.screenConfiguration.itemHeader,
        )

        if (newAttachments != null) {
            itemToSave = itemToSave.copyWithAttrs { attachments = newAttachments }
        }

        val isNew = !state.item.hasBeenSaved
        if (isNew || state.item != itemToSave || newAttachments != null) {
            
            if (newAttachments == null &&
                !state.itemScreenConfigurationProvider.hasEnoughDataToSave(itemToSave, state.listener)
            ) {
                return false
            }
            itemToSave = prepareItemForSaving(context, isNew, itemToSave)

            
            val updatedLinkedWebsites = itemToSave.getUpdatedLinkedWebsites(state.item)

            
            val removedLinkedApps = itemToSave.getRemovedLinkedApps(state.item)

            
            saveItem(itemToSave)
            this.state = state.copy(
                item = itemToSave,
                canDelete = sharingPolicy.isDeleteAllowed(false, itemToSave),
            )

            if (isNew) {
                when (itemToSave.syncObject) {
                    is SyncObject.Authentifiant -> {
                        error(
                            "Authentifiant have been removed from old item edit",
                        )
                    }
                    else -> {
                        
                    }
                }

                logSavedItem(
                    action = Action.ADD,
                    itemToSave = itemToSave,
                    updatedLinkedWebsites = updatedLinkedWebsites,
                    removedLinkedApps = removedLinkedApps,
                )
            } else {
                logSavedItem(
                    action = Action.EDIT,
                    itemToSave = itemToSave,
                    updatedLinkedWebsites = updatedLinkedWebsites,
                    removedLinkedApps = removedLinkedApps,
                )
            }
        }

        
        dataSync.sync(Trigger.SAVE)

        return true
    }

    private fun logSavedItem(
        action: Action,
        itemToSave: VaultItem<*>,
        updatedLinkedWebsites: Pair<List<String>, List<String>>?,
        removedLinkedApps: List<String>?,
    ) {
        logItemUpdate(
            item = itemToSave,
            action = action,
            addedWebsites = updatedLinkedWebsites?.first,
            removedWebsites = updatedLinkedWebsites?.second,
            removedApps = removedLinkedApps,
        )
    }

    override fun hasUnsavedChanges(): Boolean {
        val state = state ?: return false
        return state.item != state.itemScreenConfigurationProvider.gatherFromUi(
            state.item,
            state.screenConfiguration.itemSubViews,
            state.screenConfiguration.itemHeader,
        )
    }

    private suspend fun onItemViewed() {
        val state = state ?: return
        val item = state.item
        if (item.hasBeenSaved && (!state.editMode || state.dataType == SyncObjectType.SECURE_NOTE)) {
            saveItem(
                item.copyWithAttrs {
                    locallyViewedDate = Instant.now()
                    locallyUsedCount = item.locallyUsedCount + 1
                },
            )
        }
    }

    private fun getSubViewFactory(editMode: Boolean): SubViewFactory {
        return if (editMode) {
            EditableSubViewFactory(userFeaturesChecker, currentTeamSpaceFilter)
        } else {
            ReadOnlySubViewFactory(userFeaturesChecker, currentTeamSpaceFilter)
        }
    }

    private suspend fun getItem(type: SyncObjectType, uid: String): VaultItem<*>? = withContext(Dispatchers.Default) {
        tryOrNull {
            vaultDataQuery.query(
                vaultFilter {
                    specificUid(uid)
                    specificDataType(type)
                },
            )
        }
    }

    private fun prepareItemForSaving(context: Context, isNew: Boolean, originalItem: VaultItem<*>): VaultItem<*> {
        val itemToSave = if (isNew) {
            originalItem.copyWithDefaultValue(context)
        } else {
            originalItem
        }
        return itemToSave.copyWithAttrs {
            creationDate = itemToSave.syncObject.creationDatetime ?: Instant.now()
            userModificationDate = Instant.now()
            setStateModifiedIfNotDeleted()
        }
    }

    private suspend fun saveItem(data: VaultItem<*>) =
        withContext(Dispatchers.Default) { tryOrNull { dataSaver.save(data) } ?: false }

    private fun logItemUpdate(
        item: VaultItem<*>,
        action: Action,
        addedWebsites: List<String>?,
        removedWebsites: List<String>?,
        removedApps: List<String>?,
    ) {
        vaultItemLogger.logUpdate(
            action = action,
            editedFields = null,
            itemId = item.uid,
            itemType = item.syncObjectType.toItemType(),
            space = item.getTeamSpaceLog(),
            url = null,
            addedWebsites = addedWebsites,
            removedWebsites = removedWebsites,
            removedApps = removedApps,
        )
    }

    private data class State(
        val dataType: SyncObjectType,
        val item: VaultItem<*>,
        val editMode: Boolean,
        val canDelete: Boolean,
        val itemScreenConfigurationProvider: ItemScreenConfigurationProvider,
        val screenConfiguration: ScreenConfiguration,
        val listener: ItemEditViewContract.View.UiUpdateListener,
    )
}