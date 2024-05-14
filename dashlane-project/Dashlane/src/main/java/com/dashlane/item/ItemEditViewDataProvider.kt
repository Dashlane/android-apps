package com.dashlane.item

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.dashlane.R
import com.dashlane.applinkfetcher.AuthentifiantAppLinkDownloader
import com.dashlane.authenticator.AuthenticatorLogger
import com.dashlane.authenticator.Otp
import com.dashlane.autofill.LinkedServicesHelper
import com.dashlane.collections.sharing.item.CollectionSharingItemDataProvider
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.Action
import com.dashlane.hermes.generated.definitions.CollectionAction
import com.dashlane.hermes.generated.definitions.Field
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.hermes.generated.events.user.UpdateCollection
import com.dashlane.item.collection.getAllCollections
import com.dashlane.item.linkedwebsites.getRemovedLinkedApps
import com.dashlane.item.linkedwebsites.getUpdatedLinkedWebsites
import com.dashlane.item.nfc.NfcCreditCardReader
import com.dashlane.item.subview.ItemCollectionListSubView
import com.dashlane.item.subview.ItemCollectionListSubView.Collection
import com.dashlane.item.subview.ItemScreenConfigurationProvider
import com.dashlane.item.subview.ItemSubView
import com.dashlane.item.subview.provider.DateTimeFieldFactory
import com.dashlane.item.subview.provider.EditableSubViewFactory
import com.dashlane.item.subview.provider.ItemScreenConfigurationSecureNoteProvider
import com.dashlane.item.subview.provider.ReadOnlySubViewFactory
import com.dashlane.item.subview.provider.SubViewFactory
import com.dashlane.item.subview.provider.credential.ItemScreenConfigurationAuthentifiantProvider
import com.dashlane.item.subview.provider.credential.ItemScreenConfigurationPasskeyProvider
import com.dashlane.item.subview.provider.credential.PasswordChangedInfoBoxDataProvider
import com.dashlane.item.subview.provider.credential.getPreviousPassword
import com.dashlane.item.subview.provider.id.ItemScreenConfigurationDriverLicenseProvider
import com.dashlane.item.subview.provider.id.ItemScreenConfigurationFiscalStatementProvider
import com.dashlane.item.subview.provider.id.ItemScreenConfigurationIdCardProvider
import com.dashlane.item.subview.provider.id.ItemScreenConfigurationPassportProvider
import com.dashlane.item.subview.provider.id.ItemScreenConfigurationSocialSecurityProvider
import com.dashlane.item.subview.provider.payment.ItemScreenConfigurationBankAccountProvider
import com.dashlane.item.subview.provider.payment.ItemScreenConfigurationCreditCardProvider
import com.dashlane.item.subview.provider.payment.ItemScreenConfigurationPaypalProvider
import com.dashlane.item.subview.provider.personalinfo.ItemScreenConfigurationAddressProvider
import com.dashlane.item.subview.provider.personalinfo.ItemScreenConfigurationCompanyProvider
import com.dashlane.item.subview.provider.personalinfo.ItemScreenConfigurationEmailProvider
import com.dashlane.item.subview.provider.personalinfo.ItemScreenConfigurationIdentityProvider
import com.dashlane.item.subview.provider.personalinfo.ItemScreenConfigurationPhoneProvider
import com.dashlane.item.subview.provider.personalinfo.ItemScreenConfigurationWebsiteProvider
import com.dashlane.login.lock.LockManager
import com.dashlane.navigation.Navigator
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.EmailSuggestionProvider
import com.dashlane.storage.userdata.accessor.CollectionDataQuery
import com.dashlane.storage.userdata.accessor.DataChangeHistoryQuery
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.GeneratedPasswordQuery
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.CollectionFilter
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.sync.DataSync
import com.dashlane.teamspaces.getTeamSpaceLog
import com.dashlane.teamspaces.manager.TeamSpaceAccessorProvider
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter
import com.dashlane.teamspaces.ui.TeamSpaceRestrictionNotificator
import com.dashlane.ui.screens.fragments.SharingPolicyDataProvider
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProvider
import com.dashlane.userfeatures.UserFeaturesChecker
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.util.date.RelativeDateFormatter
import com.dashlane.util.date.RelativeDateFormatterImpl
import com.dashlane.util.isSemanticallyNull
import com.dashlane.util.isUrlFormat
import com.dashlane.util.obfuscated.toSyncObfuscatedValue
import com.dashlane.util.tryOrNull
import com.dashlane.vault.VaultActivityLogger
import com.dashlane.vault.VaultItemLogger
import com.dashlane.vault.model.CommonDataIdentifierAttrsImpl
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.addAuthId
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.model.createAddress
import com.dashlane.vault.model.createAuthentifiant
import com.dashlane.vault.model.createBankStatement
import com.dashlane.vault.model.createCollection
import com.dashlane.vault.model.createCompany
import com.dashlane.vault.model.createDriverLicence
import com.dashlane.vault.model.createEmail
import com.dashlane.vault.model.createFiscalStatement
import com.dashlane.vault.model.createIdCard
import com.dashlane.vault.model.createIdentity
import com.dashlane.vault.model.createPassport
import com.dashlane.vault.model.createPaymentCreditCard
import com.dashlane.vault.model.createPaymentPaypal
import com.dashlane.vault.model.createPersonalWebsite
import com.dashlane.vault.model.createPhone
import com.dashlane.vault.model.createSecureNote
import com.dashlane.vault.model.createSocialSecurityStatement
import com.dashlane.vault.model.getDefaultCountry
import com.dashlane.vault.model.getDefaultName
import com.dashlane.vault.model.hasBeenSaved
import com.dashlane.vault.model.toCollectionDataType
import com.dashlane.vault.model.toCollectionVaultItem
import com.dashlane.vault.model.urlForGoToWebsite
import com.dashlane.vault.summary.CollectionVaultItems
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.vault.toItemType
import com.dashlane.vault.util.SecureNoteCategoryUtils
import com.dashlane.vault.util.copyWithDefaultValue
import com.dashlane.vault.util.toAuthentifiant
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.skocken.presentation.provider.BaseDataProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

class ItemEditViewDataProvider @Inject constructor(
    private val teamSpaceAccessorProvider: TeamSpaceAccessorProvider,
    private val currentTeamSpaceFilter: CurrentTeamSpaceUiFilter,
    private val vaultDataQuery: VaultDataQuery,
    private val collectionDataQuery: CollectionDataQuery,
    private val dataSaver: DataSaver,
    private val generatedPasswordQuery: GeneratedPasswordQuery,
    private val genericDataQuery: GenericDataQuery,
    private val dataChangeHistoryQuery: DataChangeHistoryQuery,
    private val sessionManager: SessionManager,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val appLinkDownloader: AuthentifiantAppLinkDownloader,
    private val emailSuggestionProvider: EmailSuggestionProvider,
    private val lockManager: LockManager,
    private val navigator: Navigator,
    private val vaultItemLogger: VaultItemLogger,
    private val activityLogger: VaultActivityLogger,
    private val sharingPolicy: SharingPolicyDataProvider,
    private val sharingDataProvider: SharingDataProvider,
    private val linkedServicesHelper: LinkedServicesHelper,
    private val dataSync: DataSync,
    private val authenticatorLogger: AuthenticatorLogger,
    private val hermesLogRepository: LogRepository,
    private val vaultItemCopy: VaultItemCopyService,
    private val collectionSharingItemDataProvider: CollectionSharingItemDataProvider,
    private val passwordChangedInfoBoxDataProvider: PasswordChangedInfoBoxDataProvider,
    private val teamspaceRestrictionNotificator: TeamSpaceRestrictionNotificator,
    clock: Clock
) : BaseDataProvider<ItemEditViewContract.Presenter>(), ItemEditViewContract.DataProvider {

    private val session: Session?
        get() = sessionManager.session
    private val relativeDateFormatter: RelativeDateFormatter by lazy {
        RelativeDateFormatterImpl(clock = clock)
    }
    private val dateTimeFieldFactory: DateTimeFieldFactory by lazy {
        DateTimeFieldFactory(
            clock = clock,
            relativeDateFormatter = relativeDateFormatter
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
        listener: ItemEditViewContract.View.UiUpdateListener
    ): Boolean {
        lockManager.waitUnlock()

        val teamSpaceAccessor = teamSpaceAccessorProvider.get()
        
        teamSpaceAccessor ?: return false
        createCategoriesIfNeeded(context, options.dataType)
        val provider: ItemScreenConfigurationProvider?
        var item: VaultItem<*>?
        val canDelete: Boolean
        if (options.uid.isSemanticallyNull()) {
            when (options.dataType) {
                SyncObjectType.AUTHENTIFIANT -> item = createAuthentifiant(options)
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
                SyncObjectType.SECURE_NOTE -> item = createSecureNote(category = "")
                SyncObjectType.ADDRESS -> {
                    val addressCountry = context.getDefaultCountry()
                    val state = com.dashlane.core.domain.State.getStatesForCountry(addressCountry)
                        .firstOrNull()?.stateDescriptor
                    item = createAddress(
                        name = context.getString(R.string.address),
                        state = state,
                        addressCountry = addressCountry
                    )
                }

                SyncObjectType.PHONE -> item = createPhone(
                    type = SyncObject.Phone.Type.PHONE_TYPE_MOBILE,
                    phoneName = context.getString(R.string.phone)
                )

                SyncObjectType.PAYMENT_PAYPAL -> item = createPaymentPaypal()
                SyncObjectType.PAYMENT_CREDIT_CARD -> item = createPaymentCreditCard()
                SyncObjectType.BANK_STATEMENT -> item = createBankStatement()
                SyncObjectType.ID_CARD -> item = createIdCard()
                SyncObjectType.PASSPORT -> item = createPassport()
                SyncObjectType.DRIVER_LICENCE -> item = createDriverLicence()
                SyncObjectType.SOCIAL_SECURITY_STATEMENT -> item = createSocialSecurityStatement()
                else -> return false
            }
            
            when (item.syncObject) {
                is SyncObject.PaymentCreditCard -> item = item.copyWithDefaultValue(context, session)
                is SyncObject.BankStatement -> item = item.copyWithDefaultValue(context, session)
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
            SyncObjectType.AUTHENTIFIANT -> {
                provider = ItemScreenConfigurationAuthentifiantProvider(
                    teamSpaceAccessor = teamSpaceAccessor,
                    sharingPolicy = sharingPolicy,
                    emailSuggestionProvider = emailSuggestionProvider,
                    navigator = navigator,
                    vaultItemLogger = vaultItemLogger,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    scannedOtp = options.scannedOtp,
                    linkedServicesHelper = linkedServicesHelper,
                    userFeaturesChecker = userFeaturesChecker,
                    authenticatorLogger = authenticatorLogger,
                    vaultItemCopy = vaultItemCopy,
                    sharingDataProvider = sharingDataProvider,
                    relativeDateFormatter = relativeDateFormatter,
                    passwordChangedInfoBoxDataProvider = passwordChangedInfoBoxDataProvider,
                    vaultDataQuery = vaultDataQuery,
                    collectionDataQuery = collectionDataQuery
                )
            }

            SyncObjectType.IDENTITY ->
                provider = ItemScreenConfigurationIdentityProvider(
                    teamSpaceAccessor = teamSpaceAccessor,
                    dateTimeFieldFactory = dateTimeFieldFactory
                )

            SyncObjectType.EMAIL ->
                provider = ItemScreenConfigurationEmailProvider(
                    teamSpaceAccessor = teamSpaceAccessor,
                    emailSuggestionProvider = emailSuggestionProvider,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy
                )

            SyncObjectType.PERSONAL_WEBSITE ->
                provider = ItemScreenConfigurationWebsiteProvider(
                    teamSpaceAccessor = teamSpaceAccessor,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy
                )

            SyncObjectType.COMPANY ->
                provider = ItemScreenConfigurationCompanyProvider(
                    teamSpaceAccessor = teamSpaceAccessor,
                    dateTimeFieldFactory = dateTimeFieldFactory
                )

            SyncObjectType.FISCAL_STATEMENT ->
                provider = ItemScreenConfigurationFiscalStatementProvider(
                    teamSpaceAccessor = teamSpaceAccessor,
                    vaultDataQuery = vaultDataQuery,
                    genericDataQuery = genericDataQuery,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy
                )

            SyncObjectType.SECURE_NOTE ->
                provider = ItemScreenConfigurationSecureNoteProvider(
                    teamSpaceAccessorProvider = teamSpaceAccessorProvider,
                    genericDataQuery = genericDataQuery,
                    sharingPolicy = sharingPolicy,
                    userFeaturesChecker = userFeaturesChecker,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    restrictionNotificator = teamspaceRestrictionNotificator
                )

            SyncObjectType.ADDRESS ->
                provider = ItemScreenConfigurationAddressProvider(
                    genericDataQuery = genericDataQuery,
                    teamSpaceAccessor = teamSpaceAccessor,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy
                )

            SyncObjectType.PHONE ->
                provider = ItemScreenConfigurationPhoneProvider(
                    teamSpaceAccessor = teamSpaceAccessor,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy
                )

            SyncObjectType.PAYMENT_PAYPAL ->
                provider = ItemScreenConfigurationPaypalProvider(
                    teamSpaceAccessor = teamSpaceAccessor,
                    emailSuggestionProvider = emailSuggestionProvider,
                    vaultItemLogger = vaultItemLogger,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy
                )

            SyncObjectType.PAYMENT_CREDIT_CARD ->
                provider = ItemScreenConfigurationCreditCardProvider(
                    genericDataQuery = genericDataQuery,
                    teamSpaceAccessor = teamSpaceAccessor,
                    vaultItemLogger = vaultItemLogger,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy
                )

            SyncObjectType.BANK_STATEMENT ->
                provider = ItemScreenConfigurationBankAccountProvider(
                    teamSpaceAccessor = teamSpaceAccessor,
                    genericDataQuery = genericDataQuery,
                    vaultItemLogger = vaultItemLogger,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy
                )

            SyncObjectType.ID_CARD ->
                provider = ItemScreenConfigurationIdCardProvider(
                    teamspaceAccessor = teamSpaceAccessor,
                    genericDataQuery = genericDataQuery,
                    vaultDataQuery = vaultDataQuery,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy
                )

            SyncObjectType.PASSPORT ->
                provider = ItemScreenConfigurationPassportProvider(
                    teamSpaceAccessor = teamSpaceAccessor,
                    genericDataQuery = genericDataQuery,
                    vaultDataQuery = vaultDataQuery,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy
                )

            SyncObjectType.DRIVER_LICENCE ->
                provider = ItemScreenConfigurationDriverLicenseProvider(
                    teamSpaceAccessor = teamSpaceAccessor,
                    genericDataQuery = genericDataQuery,
                    vaultDataQuery = vaultDataQuery,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy
                )

            SyncObjectType.SOCIAL_SECURITY_STATEMENT ->
                provider = ItemScreenConfigurationSocialSecurityProvider(
                    teamSpaceAccessor = teamSpaceAccessor,
                    genericDataQuery = genericDataQuery,
                    vaultDataQuery = vaultDataQuery,
                    vaultItemLogger = vaultItemLogger,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy
                )

            SyncObjectType.PASSKEY ->
                provider = ItemScreenConfigurationPasskeyProvider(
                    teamSpaceAccessor = teamSpaceAccessor,
                    vaultItemLogger = vaultItemLogger,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy
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
            listener
        ).apply {
            
            options.savedScreenConfiguration?.let {
                
                if (options.editMode) restoreState(it, teamSpaceAccessor)
            }
        }
        val collections = teamSpaceAccessor.getAllCollections(
            item,
            collectionDataQuery,
            sharingDataProvider,
            userFeaturesChecker
        )
        state = State(
            options.dataType,
            item,
            options.editMode,
            canDelete,
            provider,
            screenConfiguration,
            listener,
            collections
        )
        
        options.savedScreenConfiguration?.let {
            if (options.editMode) screenConfiguration.restoreMenuActions(it)
        }

        if (logDisplayPendingSetup) {
            logViewDisplay()
        }
        return true
    }

    override suspend fun onSetupEnd(
        context: Context,
        options: ItemEditViewSetupOptions,
        listener: ItemEditViewContract.View.UiUpdateListener
    ) {
        state!!.itemScreenConfigurationProvider.onSetupEnd(
            context,
            state!!.item,
            options.editMode,
            listener,
            options.savedScreenConfiguration != null
        )
        onItemViewed()
    }

    override fun logViewDisplay() {
        val state = state
        logDisplayPendingSetup = state == null
    }

    override fun setTemporaryLinkedServices(
        context: Context,
        listener: ItemEditViewContract.View.UiUpdateListener,
        temporaryWebsites: List<String>?,
        temporaryApps: List<String>?
    ) {
        val itemScreenProvider = state?.itemScreenConfigurationProvider
        if (itemScreenProvider is ItemScreenConfigurationAuthentifiantProvider) {
            itemScreenProvider.temporaryLinkedWebsites = temporaryWebsites
            itemScreenProvider.temporaryLinkedApps = temporaryApps

            val canDelete = sharingPolicy.isDeleteAllowed(false, state!!.item)

            
            if (temporaryWebsites != null) {
                itemScreenProvider.editedFields += Field.ASSOCIATED_WEBSITES_LIST
            } else {
                itemScreenProvider.editedFields -= Field.ASSOCIATED_WEBSITES_LIST
            }
            if (temporaryApps != null) {
                itemScreenProvider.editedFields += Field.ASSOCIATED_APPS_LIST
            } else {
                itemScreenProvider.editedFields -= Field.ASSOCIATED_APPS_LIST
            }

            
            val restoreItemFromUI = state!!.itemScreenConfigurationProvider.gatherFromUi(
                state!!.item,
                state!!.screenConfiguration.itemSubViews,
                state!!.screenConfiguration.itemHeader
            )
            state = state!!.copy(
                screenConfiguration = state!!.itemScreenConfigurationProvider.createScreenConfiguration(
                    context,
                    restoreItemFromUI,
                    getSubViewFactory(isEditMode),
                    isEditMode,
                    canDelete,
                    listener
                )
            )
        }
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
                listener
            ),
            canDelete = canDelete
        )
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun restorePassword(): Boolean {
        val item = state!!.item as VaultItem<SyncObject.Authentifiant>
        val (lastChangeDate, previousPassword) = item.getPreviousPassword(
            currentPassword = item.syncObject.password?.toString(),
            dataChangeHistoryQuery = dataChangeHistoryQuery
        ) ?: return false
        val itemToSave = item.copySyncObject {
            password = previousPassword.toSyncObfuscatedValue()
            modificationDatetime = lastChangeDate
        }.copyWithAttrs {
            userModificationDate = Instant.now()
            setStateModifiedIfNotDeleted()
        }
        val isRestored = saveItem(itemToSave)
        vaultItemLogger.logPasswordRestored(itemToSave.uid, itemToSave.syncObject.urlForGoToWebsite)
        return isRestored
    }

    override suspend fun closeRestorePassword() {
        passwordChangedInfoBoxDataProvider.setInfoBoxClosed(state!!.item)
    }

    override fun getScreenConfiguration(): ScreenConfiguration {
        return state!!.screenConfiguration
    }

    override fun onNewIntent(intent: Intent, coroutineScope: CoroutineScope) {
        if (!isEditMode) return
        val provider = state!!.itemScreenConfigurationProvider
        if (provider is ItemScreenConfigurationCreditCardProvider) {
            coroutineScope.launch(Dispatchers.Main) {
                NfcCreditCardReader.readCard(intent)?.let {
                    provider.updateCreditCardInfoWithEmvCard(it, state!!.listener)
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST", "LongMethod")
    override suspend fun save(context: Context, subViews: List<ItemSubView<*>>, newAttachments: String?): Boolean {
        val state = state ?: return false
        var itemToSave = state.itemScreenConfigurationProvider.gatherFromUi(
            state.item,
            state.screenConfiguration.itemSubViews,
            state.screenConfiguration.itemHeader
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

            
            updateGeneratedPassword(itemToSave.uid)

            
            saveItem(itemToSave)
            this.state = state.copy(
                item = itemToSave,
                canDelete = sharingPolicy.isDeleteAllowed(false, itemToSave)
            )

            if (isNew) {
                when (itemToSave.syncObject) {
                    is SyncObject.Authentifiant -> {
                        itemToSave as VaultItem<SyncObject.Authentifiant>
                        appLinkDownloader.fetch(itemToSave.toSummary())
                    }

                    is SyncObject.PaymentPaypal -> {
                        itemToSave as VaultItem<SyncObject.PaymentPaypal>
                        
                        val credential = itemToSave.toAuthentifiant()
                        saveItem(credential)
                    }

                    else -> {
                        
                    }
                }

                val collectionCount = state.screenConfiguration.itemSubViews
                    .filterIsInstance<ItemCollectionListSubView>().firstOrNull()?.value?.value?.count() ?: 0

                logSavedItem(
                    action = Action.ADD,
                    itemToSave = itemToSave,
                    updatedLinkedWebsites = updatedLinkedWebsites,
                    removedLinkedApps = removedLinkedApps,
                    collectionCount = collectionCount
                )
            } else {
                logSavedItem(
                    action = Action.EDIT,
                    itemToSave = itemToSave,
                    updatedLinkedWebsites = updatedLinkedWebsites,
                    removedLinkedApps = removedLinkedApps,
                    collectionCount = null 
                )
            }
        }

        saveCollectionState(this.state, itemToSave)
        saveSharedCollectionChanges(this.state, itemToSave)

        
        dataSync.sync(Trigger.SAVE)

        return true
    }

    private suspend fun saveSharedCollectionChanges(state: State?, itemToSave: VaultItem<*>) {
        state ?: return
        val collectionListSubView =
            state.screenConfiguration.itemSubViews.filterIsInstance<ItemCollectionListSubView>()
                .firstOrNull()
        if (collectionListSubView != null) { 
            val sharedCollectionsFromUi = collectionListSubView.value.value.filter { it.shared }
            val sharedCollectionsForItem =
                sharingDataProvider.getCollections(itemToSave.uid, needsAdminRights = false)
            val sharedCollectionToAdd = sharedCollectionsFromUi.filter {
                it.id != null && sharedCollectionsForItem.none { c -> c.uuid == it.id }
            }
            
            val allCollections =
                sharingDataProvider.getAcceptedCollections(needsAdminRights = false)
            runCatching {
                if (sharedCollectionToAdd.isNotEmpty()) {
                    collectionSharingItemDataProvider.addItemToSharedCollections(
                        sharedCollectionToAdd.mapNotNull { collection ->
                            allCollections.firstOrNull { it.uuid == collection.id }
                        },
                        itemToSave.toSummary()
                    )
                }
                
                val sharedCollectionToRemove = sharedCollectionsForItem.filter {
                    sharedCollectionsFromUi.none { c -> c.id == it.uuid }
                }
                if (sharedCollectionToRemove.isNotEmpty()) {
                    collectionSharingItemDataProvider.removeItemFromSharedCollections(
                        itemToSave.uid,
                        sharedCollectionToRemove
                    )
                }
            }
            this.state = state.copy(
                collections = collectionListSubView.value.value
            )
        }
    }

    private suspend fun saveCollectionState(state: State?, itemToSave: VaultItem<*>) {
        state ?: return
        val collectionListSubView =
            state.screenConfiguration.itemSubViews.filterIsInstance<ItemCollectionListSubView>()
                .firstOrNull()
        if (collectionListSubView != null) { 
            val privateCollectionsFromUi = collectionListSubView.value.value.filter { !it.shared }
            val collectionsToSave =
                buildCollectionVaultItemListToSave(
                    itemToSave,
                    privateCollectionsFromUi.map { it.name }
                )

            runCatching {
                dataSaver.save(collectionsToSave)
            }

            this.state = state.copy(
                collections = collectionListSubView.value.value
            )
        }
    }

    private fun buildCollectionVaultItemListToSave(
        item: VaultItem<*>,
        targetCollectionState: List<String>
    ): List<VaultItem<SyncObject.Collection>> {
        val alreadyExistingCollections = collectionDataQuery.queryAll(
            CollectionFilter().apply {
                withVaultItem = CollectionVaultItems(item.toCollectionDataType(), item.uid)
            }
        )

        val collectionNamesToAdd = targetCollectionState.filterNot { name ->
            alreadyExistingCollections.filter { it.spaceId == item.syncObject.spaceId }.map { it.name }.contains(name)
        }.toSet()

        val collectionsSummaryToRemove =
            alreadyExistingCollections.filterNot { targetCollectionState.contains(it.name) && it.spaceId == item.syncObject.spaceId }

        val syncCollectionsToAdd = buildAddCollectionVaultItemList(item, collectionNamesToAdd)
        val syncCollectionsToRemove = buildRemovedCollectionVaultItemList(item, collectionsSummaryToRemove)

        logCollectionUpdates(syncCollectionsToAdd, syncCollectionsToRemove, item)

        return syncCollectionsToAdd + syncCollectionsToRemove
    }

    private fun buildAddCollectionVaultItemList(
        item: VaultItem<*>,
        collectionNamesToAdd: Set<String>
    ): List<VaultItem<SyncObject.Collection>> {
        return collectionNamesToAdd.map { name ->
            collectionDataQuery.queryByName(
                name,
                CollectionFilter().apply {
                    val spaceFilter = teamSpaceAccessorProvider.get()?.currentBusinessTeam?.takeIf { item.syncObject.spaceId == it.teamId }
                        ?: TeamSpace.Personal
                    specificSpace(
                        spaceFilter
                    )
                }
            )?.let {
                it.copySyncObject {
                    vaultItems = (vaultItems ?: emptyList()) + item.toCollectionVaultItem()
                }.copyWithAttrs {
                    syncState = SyncState.MODIFIED
                }
            } ?: createCollection(
                dataIdentifier = CommonDataIdentifierAttrsImpl(teamSpaceId = item.syncObject.spaceId),
                name = name,
                vaultItems = listOf(item.toCollectionVaultItem())
            )
        }
    }

    private fun buildRemovedCollectionVaultItemList(
        item: VaultItem<*>,
        collectionsSummaryToRemove: List<SummaryObject.Collection>
    ): List<VaultItem<SyncObject.Collection>> = collectionDataQuery.queryByIds(collectionsSummaryToRemove.map { it.id })
        .map { syncCollection ->
            syncCollection.copy(
                syncObject = syncCollection.syncObject.copy {
                    vaultItems = vaultItems?.filterNot { it.id == item.uid }
                }
            )
        }

    private fun logCollectionUpdates(
        collectionsToAdd: List<VaultItem<SyncObject.Collection>>,
        collectionsToRemove: List<VaultItem<SyncObject.Collection>>,
        item: VaultItem<*>
    ) {
        for (collection in collectionsToAdd) {
            if (collection.syncState != SyncState.MODIFIED) { 
                
                hermesLogRepository.queueEvent(
                    UpdateCollection(
                        collectionId = collection.uid,
                        action = CollectionAction.ADD,
                        itemCount = 1,
                        isShared = false
                    )
                )
                activityLogger.sendCollectionCreatedActivityLog(collection.toSummary())
            }
            
            hermesLogRepository.queueEvent(
                UpdateCollection(
                    collectionId = collection.uid,
                    action = CollectionAction.ADD_CREDENTIAL,
                    itemCount = 1,
                    isShared = false
                )
            )

            activityLogger.sendAddItemToCollectionActivityLog(
                collection = collection.toSummary(),
                item = item.toSummary()
            )
        }

        for (collection in collectionsToRemove) {
            
            hermesLogRepository.queueEvent(
                UpdateCollection(
                    collectionId = collection.uid,
                    action = CollectionAction.DELETE_CREDENTIAL,
                    itemCount = 1,
                    isShared = false
                )
            )
            activityLogger.sendRemoveItemFromCollectionActivityLog(
                collection = collection.toSummary(),
                item = item.toSummary()
            )
        }
    }

    private suspend fun updateGeneratedPassword(savedItemId: String) {
        val provider = state?.itemScreenConfigurationProvider
        if (provider is ItemScreenConfigurationAuthentifiantProvider) {
            provider.lastGeneratedPasswordId?.let { id ->
                generatedPasswordQuery.queryAllNotRevoked()
                    .firstOrNull { it.uid == id }
                    ?.addAuthId(savedItemId)
                    ?.copyWithAttrs { syncState = SyncState.MODIFIED }
                    ?.let {
                        dataSaver.save(it)
                    }
            }
        }
    }

    private fun logSavedItem(
        action: Action,
        itemToSave: VaultItem<*>,
        updatedLinkedWebsites: Pair<List<String>, List<String>>?,
        removedLinkedApps: List<String>?,
        collectionCount: Int? = null
    ) {
        logItemUpdate(
            itemToSave,
            action,
            updatedLinkedWebsites?.first,
            updatedLinkedWebsites?.second,
            removedLinkedApps,
            collectionCount
        )
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun saveRefreshedOtp(otp: Otp) {
        val itemToSave = (state?.item as? VaultItem<SyncObject.Authentifiant>)
            ?.copySyncObject { otpUrl = otp.url?.toSyncObfuscatedValue() }
            ?.copyWithAttrs { setStateModifiedIfNotDeleted() } ?: return
        saveItem(itemToSave)
        state = state?.copy(item = itemToSave)
    }

    override fun hasUnsavedChanges(): Boolean {
        val state = state ?: return false
        val collectionSubView =
            state.screenConfiguration.itemSubViews.filterIsInstance<ItemCollectionListSubView>().firstOrNull()
        return state.item != state.itemScreenConfigurationProvider.gatherFromUi(
            state.item,
            state.screenConfiguration.itemSubViews,
            state.screenConfiguration.itemHeader
        ) || collectionSubView != null &&
            state.collections.sortedBy { it.name } != state.itemScreenConfigurationProvider
            .gatherCollectionsFromUi(collectionSubView).sortedBy { it.name }
    }

    private suspend fun onItemViewed() {
        val state = state ?: return
        val item = state.item
        if (item.hasBeenSaved && (!state.editMode || state.dataType == SyncObjectType.SECURE_NOTE)) {
            saveItem(
                item.copyWithAttrs {
                    locallyViewedDate = Instant.now()
                    locallyUsedCount = item.locallyUsedCount + 1
                }
            )
        }
    }

    private suspend fun createCategoriesIfNeeded(context: Context, dataType: SyncObjectType) {
        
        if (dataType == SyncObjectType.SECURE_NOTE) {
            SecureNoteCategoryUtils.createDefaultCategoriesIfNotExist(context, genericDataQuery, dataSaver)
        }
    }

    private fun createAuthentifiant(
        options: ItemEditViewSetupOptions
    ): VaultItem<SyncObject.Authentifiant> {
        val title = SyncObject.Authentifiant.getDefaultName(options.websiteUrl)
        val url = options.websiteUrl?.takeIf { it.isUrlFormat() }
        return createAuthentifiant(
            title = title,
            deprecatedUrl = url,
            autoLogin = "true",
            passwordModificationDate = Instant.now()
        )
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
                }
            )
        }
    }

    private fun prepareItemForSaving(context: Context, isNew: Boolean, originalItem: VaultItem<*>): VaultItem<*> {
        val itemToSave = if (isNew) {
            originalItem.copyWithDefaultValue(context, session)
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
        collectionCount: Int?
    ) {
        val fields = (state?.itemScreenConfigurationProvider as? ItemScreenConfigurationAuthentifiantProvider?)
            ?.editedFields
        vaultItemLogger.logUpdate(
            action = action,
            editedFields = fields?.takeIf { action == Action.EDIT }?.toList(),
            itemId = item.uid,
            itemType = item.syncObjectType.toItemType(),
            space = item.getTeamSpaceLog(),
            url = (item.syncObject as? SyncObject.Authentifiant)?.urlForGoToWebsite,
            addedWebsites = addedWebsites,
            removedWebsites = removedWebsites,
            removedApps = removedApps,
            collectionCount = collectionCount
        )
        fields?.clear()
        activityLogger.sendAuthentifiantActivityLog(vaultItem = item, action = action)
    }

    private data class State(
        val dataType: SyncObjectType,
        val item: VaultItem<*>,
        val editMode: Boolean,
        val canDelete: Boolean,
        val itemScreenConfigurationProvider: ItemScreenConfigurationProvider,
        val screenConfiguration: ScreenConfiguration,
        val listener: ItemEditViewContract.View.UiUpdateListener,
        val collections: List<Collection>
    )
}