package com.dashlane.item

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.dashlane.R
import com.dashlane.applinkfetcher.AuthentifiantAppLinkDownloader
import com.dashlane.authenticator.AuthenticatorLogger
import com.dashlane.authenticator.Otp
import com.dashlane.autofill.LinkedServicesHelper
import com.dashlane.core.DataSync
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.Action
import com.dashlane.hermes.generated.definitions.CollectionAction
import com.dashlane.hermes.generated.definitions.Field
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.hermes.generated.events.user.UpdateCollection
import com.dashlane.item.linkedwebsites.getRemovedLinkedApps
import com.dashlane.item.linkedwebsites.getUpdatedLinkedWebsites
import com.dashlane.item.nfc.NfcCreditCardReader
import com.dashlane.item.subview.ItemCollectionListSubView
import com.dashlane.item.subview.ItemScreenConfigurationProvider
import com.dashlane.item.subview.ItemSubView
import com.dashlane.item.subview.provider.DateTimeFieldFactory
import com.dashlane.item.subview.provider.EditableSubViewFactory
import com.dashlane.item.subview.provider.ItemScreenConfigurationSecureNoteProvider
import com.dashlane.item.subview.provider.ReadOnlySubViewFactory
import com.dashlane.item.subview.provider.SubViewFactory
import com.dashlane.item.subview.provider.credential.ItemScreenConfigurationAuthentifiantProvider
import com.dashlane.item.subview.provider.credential.ItemScreenConfigurationPasskeyProvider
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
import com.dashlane.session.repository.TeamspaceManagerRepository
import com.dashlane.storage.userdata.EmailSuggestionProvider
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.filter.CollectionFilter
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.teamspaces.PersonalTeamspace
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.ui.screens.fragments.SharingPolicyDataProvider
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProvider
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.util.date.RelativeDateFormatterImpl
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.util.isSemanticallyNull
import com.dashlane.util.obfuscated.toSyncObfuscatedValue
import com.dashlane.util.tryOrNull
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.util.userfeatures.UserFeaturesChecker.FeatureFlip
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
import com.dashlane.vault.util.getTeamSpaceLog
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
    private val teamspaceAccessorProvider: OptionalProvider<TeamspaceAccessor>,
    private val mainDataAccessor: MainDataAccessor,
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
    private val teamspaceManagerRepository: TeamspaceManagerRepository,
    clock: Clock
) : BaseDataProvider<ItemEditViewContract.Presenter>(), ItemEditViewContract.DataProvider {

    private val genericDataQuery = mainDataAccessor.getGenericDataQuery()
    private val dataQuery = mainDataAccessor.getVaultDataQuery()
    private val collectionDataQuery = mainDataAccessor.getCollectionDataQuery()
    private val dataSaver = mainDataAccessor.getDataSaver()
    private val session: Session?
        get() = sessionManager.session
    private val dateTimeFieldFactory: DateTimeFieldFactory by lazy {
        DateTimeFieldFactory(
            clock = clock,
            relativeDateFormatter = RelativeDateFormatterImpl(clock = clock)
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

        val teamspaceAccessor = teamspaceAccessorProvider.get()
        
        teamspaceAccessor ?: return false
        createCategoriesIfNeeded(context, options.dataType)
        val provider: ItemScreenConfigurationProvider?
        var item: VaultItem<*>?
        val canDelete: Boolean
        if (options.uid.isSemanticallyNull()) {
            when (options.dataType) {
                SyncObjectType.AUTHENTIFIANT -> item = createAuthentifiant(options)
                SyncObjectType.IDENTITY -> item = createIdentity(title = SyncObject.Identity.Title.MR)
                SyncObjectType.EMAIL -> item = createEmail(type = SyncObject.Email.Type.PERSO)
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
                val sharedCollections =
                    if (userFeaturesChecker.has(FeatureFlip.SHARING_COLLECTION_MILESTONE_3)) {
                        sharingDataProvider.getCollections(item.uid, needsAdminRights = true)
                    } else {
                        emptyList()
                    }
                provider = ItemScreenConfigurationAuthentifiantProvider(
                    teamspaceAccessor = teamspaceAccessor,
                    mainDataAccessor = mainDataAccessor,
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
                    sharedCollections = sharedCollections
                )
            }

            SyncObjectType.IDENTITY ->
                provider = ItemScreenConfigurationIdentityProvider(
                    teamspaceAccessor = teamspaceAccessor,
                    dateTimeFieldFactory = dateTimeFieldFactory
                )

            SyncObjectType.EMAIL ->
                provider = ItemScreenConfigurationEmailProvider(
                    teamspaceAccessor = teamspaceAccessor,
                    emailSuggestionProvider = emailSuggestionProvider,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy
                )

            SyncObjectType.PERSONAL_WEBSITE ->
                provider = ItemScreenConfigurationWebsiteProvider(
                    teamspaceAccessor = teamspaceAccessor,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy
                )

            SyncObjectType.COMPANY ->
                provider = ItemScreenConfigurationCompanyProvider(
                    teamspaceAccessor = teamspaceAccessor,
                    dateTimeFieldFactory = dateTimeFieldFactory
                )

            SyncObjectType.FISCAL_STATEMENT ->
                provider = ItemScreenConfigurationFiscalStatementProvider(
                    teamspaceAccessor = teamspaceAccessor,
                    mainDataAccessor = mainDataAccessor,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy
                )

            SyncObjectType.SECURE_NOTE ->
                provider = ItemScreenConfigurationSecureNoteProvider(
                    teamspaceAccessor = teamspaceAccessor,
                    mainDataAccessor = mainDataAccessor,
                    sharingPolicy = sharingPolicy,
                    userFeaturesChecker = userFeaturesChecker,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    sessionManager = sessionManager,
                    teamspaceManagerRepository = teamspaceManagerRepository,
                )

            SyncObjectType.ADDRESS ->
                provider = ItemScreenConfigurationAddressProvider(
                    teamspaceAccessor = teamspaceAccessor,
                    mainDataAccessor = mainDataAccessor,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy
                )

            SyncObjectType.PHONE ->
                provider = ItemScreenConfigurationPhoneProvider(
                    teamspaceAccessor = teamspaceAccessor,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy
                )

            SyncObjectType.PAYMENT_PAYPAL ->
                provider = ItemScreenConfigurationPaypalProvider(
                    teamspaceAccessor = teamspaceAccessor,
                    emailSuggestionProvider = emailSuggestionProvider,
                    vaultItemLogger = vaultItemLogger,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy
                )

            SyncObjectType.PAYMENT_CREDIT_CARD ->
                provider = ItemScreenConfigurationCreditCardProvider(
                    teamspaceAccessor = teamspaceAccessor,
                    mainDataAccessor = mainDataAccessor,
                    vaultItemLogger = vaultItemLogger,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy
                )

            SyncObjectType.BANK_STATEMENT ->
                provider = ItemScreenConfigurationBankAccountProvider(
                    teamspaceAccessor = teamspaceAccessor,
                    mainDataAccessor = mainDataAccessor,
                    vaultItemLogger = vaultItemLogger,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy
                )

            SyncObjectType.ID_CARD ->
                provider = ItemScreenConfigurationIdCardProvider(
                    teamspaceAccessor = teamspaceAccessor,
                    mainDataAccessor = mainDataAccessor,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy
                )

            SyncObjectType.PASSPORT ->
                provider = ItemScreenConfigurationPassportProvider(
                    teamspaceAccessor = teamspaceAccessor,
                    mainDataAccessor = mainDataAccessor,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy
                )

            SyncObjectType.DRIVER_LICENCE ->
                provider = ItemScreenConfigurationDriverLicenseProvider(
                    teamspaceAccessor = teamspaceAccessor,
                    mainDataAccessor = mainDataAccessor,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy
                )

            SyncObjectType.SOCIAL_SECURITY_STATEMENT ->
                provider = ItemScreenConfigurationSocialSecurityProvider(
                    teamspaceAccessor = teamspaceAccessor,
                    mainDataAccessor = mainDataAccessor,
                    vaultItemLogger = vaultItemLogger,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    vaultItemCopy = vaultItemCopy
                )

            SyncObjectType.PASSKEY ->
                provider = ItemScreenConfigurationPasskeyProvider(
                    teamspaceAccessor = teamspaceAccessor,
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
                
                if (options.editMode) restoreState(it, teamspaceAccessor)
            }
        }

        val collections = genericDataQuery.queryAll(
            genericFilter {
                specificDataType(SyncObjectType.COLLECTION)
                specificSpace(teamspaceAccessor.getOrDefault(item.syncObject.spaceId))
            }
        ).filterIsInstance<SummaryObject.Collection>()
            .filter { collection ->
                collection.vaultItems?.filter { it.type == item.toCollectionVaultItem().type }
                    ?.map { it.id }?.contains(item.uid) ?: false
            }
            .mapNotNull { it.name }

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
            item.syncObject.password?.toString(),
            mainDataAccessor
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

        
        dataSync.sync(Trigger.SAVE)

        return true
    }

    private suspend fun saveCollectionState(state: State?, itemToSave: VaultItem<*>) {
        state ?: return
        val collectionListSubView =
            state.screenConfiguration.itemSubViews.filterIsInstance<ItemCollectionListSubView>().firstOrNull()
        if (collectionListSubView != null) { 
            val collectionsFromUi = collectionListSubView.value.value.mapNotNull { (name, shared) ->
                if (shared) return@mapNotNull null else name
            }
            val collectionsToSave =
                buildCollectionVaultItemListToSave(itemToSave, collectionsFromUi)

            runCatching {
                dataSaver.save(collectionsToSave)
            }

            this.state = state.copy(
                collections = collectionsFromUi
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

        logCollectionUpdates(syncCollectionsToAdd, syncCollectionsToRemove)

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
                    specificSpace(
                        teamspaceAccessorProvider.get()?.getOrDefault(item.syncObject.spaceId) ?: PersonalTeamspace
                    )
                }
            )?.let {
                it.copySyncObject {
                    vaultItems = (vaultItems?.toMutableList() ?: mutableListOf()) + item.toCollectionVaultItem()
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
        collectionsToRemove: List<VaultItem<SyncObject.Collection>>
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
            }
            
            hermesLogRepository.queueEvent(
                UpdateCollection(
                    collectionId = collection.uid,
                    action = CollectionAction.ADD_CREDENTIAL,
                    itemCount = 1,
                    isShared = false
                )
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
        }
    }

    private suspend fun updateGeneratedPassword(savedItemId: String) {
        val provider = state?.itemScreenConfigurationProvider
        if (provider is ItemScreenConfigurationAuthentifiantProvider) {
            provider.lastGeneratedPasswordId?.let { id ->
                mainDataAccessor.getGeneratedPasswordQuery().queryAllNotRevoked()
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
            state.collections.sorted() != state.itemScreenConfigurationProvider
            .gatherCollectionsFromUi(collectionSubView).sorted()
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
            SecureNoteCategoryUtils.createDefaultCategoriesIfNotExist(context, mainDataAccessor)
        }
    }

    private fun createAuthentifiant(
        options: ItemEditViewSetupOptions
    ): VaultItem<SyncObject.Authentifiant> {
        val title = SyncObject.Authentifiant.getDefaultName(options.websiteUrl)
        return createAuthentifiant(
            title = title,
            deprecatedUrl = options.websiteUrl,
            autoLogin = "true",
            passwordModificationDate = Instant.now()
        )
    }

    private fun getSubViewFactory(editMode: Boolean): SubViewFactory {
        return if (editMode) EditableSubViewFactory(userFeaturesChecker) else ReadOnlySubViewFactory(userFeaturesChecker)
    }

    private suspend fun getItem(type: SyncObjectType, uid: String): VaultItem<*>? = withContext(Dispatchers.Default) {
        tryOrNull {
            dataQuery.query(
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
        activityLogger.sendActivityLog(vaultItem = item, action = action)
    }

    private data class State(
        val dataType: SyncObjectType,
        val item: VaultItem<*>,
        val editMode: Boolean,
        val canDelete: Boolean,
        val itemScreenConfigurationProvider: ItemScreenConfigurationProvider,
        val screenConfiguration: ScreenConfiguration,
        val listener: ItemEditViewContract.View.UiUpdateListener,
        val collections: List<String>
    )
}