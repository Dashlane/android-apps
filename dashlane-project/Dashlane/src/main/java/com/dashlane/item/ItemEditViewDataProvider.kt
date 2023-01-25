package com.dashlane.item

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.dashlane.R
import com.dashlane.applinkfetcher.AuthentifiantAppLinkDownloader
import com.dashlane.authenticator.Otp
import com.dashlane.autofill.LinkedServicesHelper
import com.dashlane.core.DataSync
import com.dashlane.device.DeviceInfoRepository
import com.dashlane.hermes.generated.definitions.Action
import com.dashlane.hermes.generated.definitions.Field
import com.dashlane.item.linkedwebsites.getRemovedLinkedApps
import com.dashlane.item.linkedwebsites.getUpdatedLinkedWebsites
import com.dashlane.item.nfc.NfcCreditCardReader
import com.dashlane.item.subview.ItemScreenConfigurationProvider
import com.dashlane.item.subview.ItemSubView
import com.dashlane.item.subview.edit.ItemEditSpaceSubView
import com.dashlane.item.subview.provider.DateTimeFieldFactory
import com.dashlane.item.subview.provider.EditableSubViewFactory
import com.dashlane.item.subview.provider.ItemScreenConfigurationAuthentifiantProvider
import com.dashlane.item.subview.provider.ItemScreenConfigurationSecureNoteProvider
import com.dashlane.item.subview.provider.ReadOnlySubViewFactory
import com.dashlane.item.subview.provider.SubViewFactory
import com.dashlane.item.subview.provider.getPreviousPassword
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
import com.dashlane.session.BySessionRepository
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.EmailSuggestionProvider
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.ui.screens.fragments.SharingPolicyDataProvider
import com.dashlane.useractivity.log.usage.UsageLogCode134
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.util.date.RelativeDateFormatterImpl
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.util.isSemanticallyNull
import com.dashlane.util.obfuscated.toSyncObfuscatedValue
import com.dashlane.util.tryOrNull
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.vault.VaultItemLogger
import com.dashlane.vault.model.DataIdentifierId
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.addAuthId
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.model.createAddress
import com.dashlane.vault.model.createAuthentifiant
import com.dashlane.vault.model.createBankStatement
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
import com.dashlane.vault.model.urlForGoToWebsite
import com.dashlane.vault.summary.toSummary
import com.dashlane.vault.toItemType
import com.dashlane.vault.util.SecureNoteCategoryUtils
import com.dashlane.vault.util.copyWithDefaultValue
import com.dashlane.vault.util.desktopId
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
    private val deviceInfoRepository: DeviceInfoRepository,
    private val lockManager: LockManager,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val navigator: Navigator,
    private val vaultItemLogger: VaultItemLogger,
    private val sharingPolicy: SharingPolicyDataProvider,
    private val linkedServicesHelper: LinkedServicesHelper,
    clock: Clock
) : BaseDataProvider<ItemEditViewContract.Presenter>(), ItemEditViewContract.DataProvider {

    private val dataQuery = mainDataAccessor.getVaultDataQuery()
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
        var provider: ItemScreenConfigurationProvider? = null
        var item: VaultItem<*>? = null
        val canDelete: Boolean
        if (options.uid.isSemanticallyNull()) {
            when (options.dataType.desktopId) {
                DataIdentifierId.AUTHENTIFIANT -> item = createAuthentifiant(options)
                DataIdentifierId.IDENTITY -> item = createIdentity(title = SyncObject.Identity.Title.MR)
                DataIdentifierId.EMAIL -> item = createEmail(type = SyncObject.Email.Type.PERSO)
                DataIdentifierId.PERSONAL_WEBSITE -> item = createPersonalWebsite()
                DataIdentifierId.COMPANY -> item = createCompany()
                DataIdentifierId.FISCAL_STATEMENT -> item = createFiscalStatement()
                DataIdentifierId.SECURE_NOTE -> item = createSecureNote(category = "")
                DataIdentifierId.ADDRESS -> {
                    val addressCountry = context.getDefaultCountry()
                    val state = com.dashlane.core.domain.State.getStatesForCountry(addressCountry)
                        .firstOrNull()?.stateDescriptor
                    item = createAddress(
                        name = context.getString(R.string.address),
                        state = state,
                        addressCountry = addressCountry
                    )
                }
                DataIdentifierId.PHONE -> item = createPhone(
                    type = SyncObject.Phone.Type.PHONE_TYPE_MOBILE,
                    phoneName = context.getString(R.string.phone)
                )
                DataIdentifierId.PAYMENT_PAYPAL -> item = createPaymentPaypal()
                DataIdentifierId.PAYMENT_CREDIT_CARD -> item = createPaymentCreditCard()
                DataIdentifierId.BANK_STATEMENT -> item = createBankStatement()
                DataIdentifierId.ID_CARD -> item = createIdCard()
                DataIdentifierId.PASSPORT -> item = createPassport()
                DataIdentifierId.DRIVER_LICENCE -> item = createDriverLicence()
                DataIdentifierId.SOCIAL_SECURITY_STATEMENT -> item = createSocialSecurityStatement()
            }
            
            when (item?.syncObject) {
                is SyncObject.PaymentCreditCard -> item = item.copyWithDefaultValue(context, session)
                is SyncObject.BankStatement -> item = item.copyWithDefaultValue(context, session)
                else -> item?.copyWithAttrs { formatLang = context.getDefaultCountry() }
            }
            canDelete = sharingPolicy.isDeleteAllowed(true, item)
        } else {
            item = getItem(options.dataType, options.uid!!)
            if (item == null) {
                
                return false
            }
            canDelete = sharingPolicy.isDeleteAllowed(false, item)
        }

        when (options.dataType.desktopId) {
            DataIdentifierId.AUTHENTIFIANT -> {
                provider = ItemScreenConfigurationAuthentifiantProvider(
                    teamspaceAccessor = teamspaceAccessor,
                    mainDataAccessor = mainDataAccessor,
                    sharingPolicy = sharingPolicy,
                    sender = options.sender,
                    emailSuggestionProvider = emailSuggestionProvider,
                    sessionManager = sessionManager,
                    bySessionUsageLogRepository = bySessionUsageLogRepository,
                    navigator = navigator,
                    vaultItemLogger = vaultItemLogger,
                    dateTimeFieldFactory = dateTimeFieldFactory,
                    userFeaturesChecker = userFeaturesChecker,
                    scannedOtp = options.scannedOtp,
                    linkedServicesHelper = linkedServicesHelper
                )
            }
            DataIdentifierId.IDENTITY -> provider =
                ItemScreenConfigurationIdentityProvider(
                    teamspaceAccessor = teamspaceAccessor,
                    dataCounter = mainDataAccessor.getDataCounter(),
                    deviceInfoRepository = deviceInfoRepository,
                    sessionManager = sessionManager,
                    bySessionUsageLogRepository = bySessionUsageLogRepository,
                    dateTimeFieldFactory = dateTimeFieldFactory
                )
            DataIdentifierId.EMAIL -> provider =
                ItemScreenConfigurationEmailProvider(
                    teamspaceAccessor = teamspaceAccessor,
                    dataCounter = mainDataAccessor.getDataCounter(),
                    emailSuggestionProvider = emailSuggestionProvider,
                    sessionManager = sessionManager,
                    bySessionUsageLogRepository = bySessionUsageLogRepository,
                    dateTimeFieldFactory = dateTimeFieldFactory
                )
            DataIdentifierId.PERSONAL_WEBSITE -> provider =
                ItemScreenConfigurationWebsiteProvider(
                    teamspaceAccessor = teamspaceAccessor,
                    dataCounter = mainDataAccessor.getDataCounter(),
                    sessionManager = sessionManager,
                    bySessionUsageLogRepository = bySessionUsageLogRepository,
                    dateTimeFieldFactory = dateTimeFieldFactory
                )
            DataIdentifierId.COMPANY -> provider =
                ItemScreenConfigurationCompanyProvider(
                    teamspaceAccessor = teamspaceAccessor,
                    dataCounter = mainDataAccessor.getDataCounter(),
                    sessionManager = sessionManager,
                    bySessionUsageLogRepository = bySessionUsageLogRepository,
                    dateTimeFieldFactory = dateTimeFieldFactory
                )
            DataIdentifierId.FISCAL_STATEMENT -> provider =
                ItemScreenConfigurationFiscalStatementProvider(
                    teamspaceAccessor = teamspaceAccessor,
                    mainDataAccessor = mainDataAccessor,
                    dataCounter = mainDataAccessor.getDataCounter(),
                    sessionManager = sessionManager,
                    bySessionUsageLogRepository = bySessionUsageLogRepository,
                    dateTimeFieldFactory = dateTimeFieldFactory
                )
            DataIdentifierId.SECURE_NOTE -> provider =
                ItemScreenConfigurationSecureNoteProvider(
                    teamspaceAccessor = teamspaceAccessor,
                    dataCounter = mainDataAccessor.getDataCounter(),
                    mainDataAccessor = mainDataAccessor,
                    sharingPolicy = sharingPolicy,
                    userFeaturesChecker = userFeaturesChecker,
                    sessionManager = sessionManager,
                    bySessionUsageLogRepository = bySessionUsageLogRepository,
                    dateTimeFieldFactory = dateTimeFieldFactory
                )
            DataIdentifierId.ADDRESS -> provider =
                ItemScreenConfigurationAddressProvider(
                    teamspaceAccessor = teamspaceAccessor,
                    mainDataAccessor = mainDataAccessor,
                    deviceInfoRepository = deviceInfoRepository,
                    sessionManager = sessionManager,
                    bySessionUsageLogRepository = bySessionUsageLogRepository,
                    dateTimeFieldFactory = dateTimeFieldFactory
                )
            DataIdentifierId.PHONE -> provider =
                ItemScreenConfigurationPhoneProvider(
                    teamspaceAccessor = teamspaceAccessor,
                    mainDataAccessor = mainDataAccessor,
                    sessionManager = sessionManager,
                    bySessionUsageLogRepository = bySessionUsageLogRepository,
                    dateTimeFieldFactory = dateTimeFieldFactory
                )
            DataIdentifierId.PAYMENT_PAYPAL -> provider =
                ItemScreenConfigurationPaypalProvider(
                    teamspaceAccessor = teamspaceAccessor,
                    dataCounter = mainDataAccessor.getDataCounter(),
                    emailSuggestionProvider = emailSuggestionProvider,
                    sessionManager = sessionManager,
                    bySessionUsageLogRepository = bySessionUsageLogRepository,
                    vaultItemLogger = vaultItemLogger,
                    dateTimeFieldFactory = dateTimeFieldFactory
                )
            DataIdentifierId.PAYMENT_CREDIT_CARD -> provider =
                ItemScreenConfigurationCreditCardProvider(
                    teamspaceAccessor = teamspaceAccessor,
                    mainDataAccessor = mainDataAccessor,
                    sessionManager = sessionManager,
                    bySessionUsageLogRepository = bySessionUsageLogRepository,
                    vaultItemLogger = vaultItemLogger,
                    dateTimeFieldFactory = dateTimeFieldFactory
                )
            DataIdentifierId.BANK_STATEMENT -> provider =
                ItemScreenConfigurationBankAccountProvider(
                    teamspaceAccessor = teamspaceAccessor,
                    mainDataAccessor = mainDataAccessor,
                    sessionManager = sessionManager,
                    bySessionUsageLogRepository = bySessionUsageLogRepository,
                    vaultItemLogger = vaultItemLogger,
                    dateTimeFieldFactory = dateTimeFieldFactory
                )
            DataIdentifierId.ID_CARD -> provider =
                ItemScreenConfigurationIdCardProvider(
                    teamspaceAccessor = teamspaceAccessor,
                    mainDataAccessor = mainDataAccessor,
                    sessionManager = sessionManager,
                    bySessionUsageLogRepository = bySessionUsageLogRepository,
                    dateTimeFieldFactory = dateTimeFieldFactory
                )
            DataIdentifierId.PASSPORT -> provider =
                ItemScreenConfigurationPassportProvider(
                    teamspaceAccessor = teamspaceAccessor,
                    mainDataAccessor = mainDataAccessor,
                    sessionManager = sessionManager,
                    bySessionUsageLogRepository = bySessionUsageLogRepository,
                    dateTimeFieldFactory = dateTimeFieldFactory
                )
            DataIdentifierId.DRIVER_LICENCE -> provider =
                ItemScreenConfigurationDriverLicenseProvider(
                    teamspaceAccessor = teamspaceAccessor,
                    mainDataAccessor = mainDataAccessor,
                    sessionManager = sessionManager,
                    bySessionUsageLogRepository = bySessionUsageLogRepository,
                    dateTimeFieldFactory = dateTimeFieldFactory
                )
            DataIdentifierId.SOCIAL_SECURITY_STATEMENT -> provider =
                ItemScreenConfigurationSocialSecurityProvider(
                    teamspaceAccessor = teamspaceAccessor,
                    mainDataAccessor = mainDataAccessor,
                    sessionManager = sessionManager,
                    bySessionUsageLogRepository = bySessionUsageLogRepository,
                    vaultItemLogger = vaultItemLogger,
                    dateTimeFieldFactory = dateTimeFieldFactory
                )
        }
        provider ?: return false

        
        options.savedAdditionalData?.let {
            provider.restoreAdditionalData(it)
        }

        
        val screenConfiguration = provider.createScreenConfiguration(
            context, item!!, getSubViewFactory(options.editMode),
            options.editMode, canDelete, listener
        ).apply {
            
            options.savedScreenConfiguration?.let {
                
                if (options.editMode) restoreState(it, teamspaceAccessor)
            }
        }
        state = State(
            options.dataType,
            item,
            options.editMode,
            canDelete,
            provider,
            screenConfiguration,
            listener
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
        state?.run {
            itemScreenConfigurationProvider.logger.logDisplay(dataType)
        }
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

            
            itemScreenProvider.editedFields += Field.ASSOCIATED_WEBSITES_LIST

            
            val restoreItemFromUI = state!!.itemScreenConfigurationProvider.gatherFromUi(
                state!!.item,
                state!!.screenConfiguration.itemSubViews,
                state!!.screenConfiguration.itemHeader
            )
            state = state!!.copy(
                screenConfiguration = state!!.itemScreenConfigurationProvider.createScreenConfiguration(
                    context, restoreItemFromUI, getSubViewFactory(isEditMode), isEditMode, canDelete, listener
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
                context, state!!.item, getSubViewFactory(editMode), editMode, canDelete, listener
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

    @Suppress("UNCHECKED_CAST")
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
            ) return false
            itemToSave = prepareItemForSaving(context, isNew, itemToSave)

            
            val updatedLinkedWebsites = itemToSave.getUpdatedLinkedWebsites(state.item)

            
            val removedLinkedApps = itemToSave.getRemovedLinkedApps(state.item)

            
            updateGeneratedPassword(itemToSave.uid)

            
            saveItem(itemToSave)
            this.state = state.copy(
                item = itemToSave,
                canDelete = sharingPolicy.isDeleteAllowed(false, itemToSave)
            )
            val categorizationMethod =
                (subViews.firstOrNull { it is ItemEditSpaceSubView } as? ItemEditSpaceSubView)?.categorizationMethod
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
                logSavedItem(Action.ADD, itemToSave, categorizationMethod, updatedLinkedWebsites, removedLinkedApps)
            } else {
                logSavedItem(Action.EDIT, itemToSave, categorizationMethod, updatedLinkedWebsites, removedLinkedApps)
            }
        }

        
        DataSync.sync(UsageLogCode134.Origin.SAVE)

        return true
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
        categorizationMethod: ItemEditSpaceSubView.CategorizationMethod?,
        updatedLinkedWebsites: Pair<List<String>, List<String>>?,
        removedLinkedApps: List<String>?
    ) {
        if (action == Action.ADD) {
            state!!.itemScreenConfigurationProvider.logger.logItemAdded(
                itemToSave, state!!.dataType, categorizationMethod
            )
        } else if (action == Action.EDIT) {
            state!!.itemScreenConfigurationProvider.logger.logItemModified(
                itemToSave, state!!.dataType, categorizationMethod
            )
        }
        logItemUpdate(
            itemToSave, action, updatedLinkedWebsites?.first, updatedLinkedWebsites?.second, removedLinkedApps
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
        return state.item != state.itemScreenConfigurationProvider.gatherFromUi(
            state.item,
            state.screenConfiguration.itemSubViews,
            state.screenConfiguration.itemHeader
        )
    }

    private suspend fun onItemViewed() {
        val state = state ?: return
        val item = state.item
        if (item.hasBeenSaved && (!state.editMode || state.dataType.desktopId == DataIdentifierId.SECURE_NOTE)) {
            saveItem(item.copyWithAttrs {
                locallyViewedDate = Instant.now()
                locallyUsedCount = item.locallyUsedCount + 1
            })
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
            dataQuery.query(vaultFilter {
                specificUid(uid)
                specificDataType(type)
            })
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
        removedApps: List<String>?
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
            removedApps = removedApps
        )
        fields?.clear()
    }

    

    private data class State(
        val dataType: SyncObjectType,
        val item: VaultItem<*>,
        val editMode: Boolean,
        val canDelete: Boolean,
        val itemScreenConfigurationProvider: ItemScreenConfigurationProvider,
        val screenConfiguration: ScreenConfiguration,
        val listener: ItemEditViewContract.View.UiUpdateListener
    )
}