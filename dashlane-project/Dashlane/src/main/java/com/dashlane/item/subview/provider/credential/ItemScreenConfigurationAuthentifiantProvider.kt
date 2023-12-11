package com.dashlane.item.subview.provider.credential

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.format.DateUtils
import android.view.Gravity
import androidx.compose.runtime.mutableStateOf
import com.dashlane.R
import com.dashlane.authenticator.AuthenticatorLogger
import com.dashlane.authenticator.Otp
import com.dashlane.authenticator.otp
import com.dashlane.autofill.LinkedServicesHelper
import com.dashlane.design.component.InfoboxButton
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.ext.application.KnownLinkedDomains
import com.dashlane.hermes.generated.definitions.Field
import com.dashlane.hermes.generated.definitions.ItemType
import com.dashlane.item.ItemEditViewContract
import com.dashlane.item.ScreenConfiguration
import com.dashlane.item.header.ItemHeader
import com.dashlane.item.subview.ItemCollectionListSubView
import com.dashlane.item.subview.ItemScreenConfigurationProvider
import com.dashlane.item.subview.ItemSubView
import com.dashlane.item.subview.ItemSubViewWithActionWrapper
import com.dashlane.item.subview.ValueChangeManager
import com.dashlane.item.subview.action.AuthenticatorCodeCopyAction
import com.dashlane.item.subview.action.CopyAction
import com.dashlane.item.subview.action.DeleteMenuAction
import com.dashlane.item.subview.action.GeneratePasswordAction
import com.dashlane.item.subview.action.LimitedSharingRightsInfoAction
import com.dashlane.item.subview.action.LoginAction
import com.dashlane.item.subview.action.LoginOpener
import com.dashlane.item.subview.action.MenuAction
import com.dashlane.item.subview.action.OpenLinkedWebsitesAction
import com.dashlane.item.subview.action.authenticator.ActivateRemoveAuthenticatorAction
import com.dashlane.item.subview.edit.ItemAuthenticatorEditSubView
import com.dashlane.item.subview.edit.ItemEditPasswordWithStrengthSubView
import com.dashlane.item.subview.provider.DateTimeFieldFactory
import com.dashlane.item.subview.provider.SubViewFactory
import com.dashlane.item.subview.readonly.EmptyLinkedServicesSubView
import com.dashlane.item.subview.readonly.ItemAuthenticatorReadSubView
import com.dashlane.item.subview.readonly.ItemClickActionSubView
import com.dashlane.item.subview.readonly.ItemInfoboxSubView
import com.dashlane.item.subview.readonly.ItemLinkedServicesSubView
import com.dashlane.item.subview.readonly.ItemPasswordSafetySubView
import com.dashlane.navigation.Navigator
import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.similarpassword.SimilarPassword
import com.dashlane.storage.userdata.EmailSuggestionProvider
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.filter.CollectionFilter
import com.dashlane.storage.userdata.accessor.filter.DataChangeHistoryFilter
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.teamspaces.PersonalTeamspace
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.ui.VaultItemImageHelper
import com.dashlane.ui.credential.passwordgenerator.PasswordGeneratorDialog
import com.dashlane.ui.screens.fragments.SharingPolicyDataProvider
import com.dashlane.url.UrlDomain
import com.dashlane.url.name
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.url.toUrlOrNull
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.isSemanticallyNull
import com.dashlane.util.isValidEmail
import com.dashlane.util.obfuscated.matchesNullAsEmpty
import com.dashlane.util.obfuscated.toSyncObfuscatedValue
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.util.userfeatures.UserFeaturesChecker.FeatureFlip
import com.dashlane.vault.VaultItemLogger
import com.dashlane.vault.history.password
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.model.hasBeenSaved
import com.dashlane.vault.model.isLoginEmail
import com.dashlane.vault.model.isSpaceItem
import com.dashlane.vault.model.loginForUi
import com.dashlane.vault.model.navigationUrl
import com.dashlane.vault.model.titleForListNormalized
import com.dashlane.vault.model.toCollectionDataType
import com.dashlane.vault.model.urlDomain
import com.dashlane.vault.model.urlForGoToWebsite
import com.dashlane.vault.model.urlForUI
import com.dashlane.vault.model.urlForUsageLog
import com.dashlane.vault.summary.CollectionVaultItems
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.vault.util.SecurityBreachUtil.isCompromised
import com.dashlane.xml.domain.SyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import java.text.Collator
import java.time.Duration
import java.time.Instant

class ItemScreenConfigurationAuthentifiantProvider(
    private val teamspaceAccessor: TeamspaceAccessor,
    private val mainDataAccessor: MainDataAccessor,
    private val sharingPolicy: SharingPolicyDataProvider,
    private val emailSuggestionProvider: EmailSuggestionProvider,
    private val navigator: Navigator,
    private val vaultItemLogger: VaultItemLogger,
    private val dateTimeFieldFactory: DateTimeFieldFactory,
    private val scannedOtp: Otp?,
    private val linkedServicesHelper: LinkedServicesHelper,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val authenticatorLogger: AuthenticatorLogger,
    private val vaultItemCopy: VaultItemCopyService,
    private val sharedCollections: List<Collection>
) : ItemScreenConfigurationProvider() {
    
    val editedFields: MutableSet<Field> = mutableSetOf()

    
    var temporaryLinkedWebsites: List<String>? = null
    var temporaryLinkedApps: List<String>? = null

    
    var lastGeneratedPasswordId: String? = null

    private var isLoginCopied = false
    private var isPasswordCopied = false

    @Suppress("UNCHECKED_CAST")
    override fun createScreenConfiguration(
        context: Context,
        item: VaultItem<*>,
        subViewFactory: SubViewFactory,
        editMode: Boolean,
        canDelete: Boolean,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): ScreenConfiguration {
        val authentifiantItem = item as VaultItem<SyncObject.Authentifiant>
        return ScreenConfiguration(
            createSubViews(context, authentifiantItem, subViewFactory, editMode, canDelete, listener),
            createHeader(context, authentifiantItem, editMode, canDelete, listener)
        )
    }

    override fun hasEnoughDataToSave(itemToSave: VaultItem<*>): Boolean {
        check(itemToSave.syncObject is SyncObject.Authentifiant) { "Unexpected item $itemToSave isn't an Authentifiant" }
        val authentifiant = itemToSave.syncObject as SyncObject.Authentifiant
        return (
            authentifiant.urlForUI().isNotSemanticallyNull() ||
                authentifiant.title.isNotSemanticallyNull()
            ) &&
            authentifiant.loginForUi.isNotSemanticallyNull()
    }

    override fun saveAdditionalData(): Bundle = Bundle().also { bundle ->
        temporaryLinkedWebsites?.let {
            bundle.putStringArrayList(LINKED_WEBSITES_BUNDLE_KEY, ArrayList(it))
        }
        temporaryLinkedApps?.let {
            bundle.putStringArrayList(LINKED_APPS_BUNDLE_KEY, ArrayList(it))
        }
        lastGeneratedPasswordId?.let {
            bundle.putString(LAST_GENERATED_PASSWORD_BUNDLE_KEY, it)
        }
    }

    override fun restoreAdditionalData(data: Bundle) {
        data.getStringArrayList(LINKED_WEBSITES_BUNDLE_KEY)?.let {
            temporaryLinkedWebsites = it
        }
        data.getStringArrayList(LINKED_APPS_BUNDLE_KEY)?.let {
            temporaryLinkedApps = it
        }
        data.getString(LAST_GENERATED_PASSWORD_BUNDLE_KEY)?.let {
            lastGeneratedPasswordId = it
        }
    }

    private fun createHeader(
        context: Context,
        item: VaultItem<SyncObject.Authentifiant>,
        editMode: Boolean,
        canDelete: Boolean,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): ItemHeader {
        val authentifiant = item.syncObject
        val urlIconDrawable = VaultItemImageHelper.getIconDrawableFromSyncObject(
            context,
            item.syncObject
        )
        val menuActions = mutableListOf<MenuAction>().apply {
            addAll(createMenus())
            if (!editMode && canDelete && !sharingPolicy.canEditItem(item.toSummary(), !item.hasBeenSaved)) {
                
                
                
                add(DeleteMenuAction(listener))
            }
        }
        return ItemHeader(menuActions, authentifiant.title, urlIconDrawable)
    }

    @Suppress("LongMethod", "UNCHECKED_CAST")
    private fun createSubViews(
        context: Context,
        item: VaultItem<SyncObject.Authentifiant>,
        subViewFactory: SubViewFactory,
        editMode: Boolean,
        canDelete: Boolean,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): List<ItemSubView<*>> {
        val domain = item.syncObject.navigationUrl?.toUrlDomainOrNull()
        val isNew = !item.hasBeenSaved

        val passwordView: ItemSubView<*>? = createPasswordField(
            editMode,
            subViewFactory,
            context,
            item,
            isNew,
            listener,
            domain
        )

        val suggestions = emailSuggestionProvider.getAllEmails()
        val emailView = createEmailField(context, editMode, subViewFactory, item, suggestions)
        val copyField =
            if (item.syncObject.email != null && item.syncObject.email == item.syncObject.loginForUi) {
                CopyField.Email
            } else {
                CopyField.Login
            }
        val otp = item.syncObject.otp() ?: scannedOtp
        val loginView = createLoginField(
            editMode,
            subViewFactory,
            item,
            getLoginHeader(item, context),
            item.syncObject.loginForUi ?: scannedOtp?.user,
            copyField,
            suggestions,
            ::copyForUpdatedLogin
        )
        val secondaryLoginView = createLoginField(
            editMode,
            subViewFactory,
            item,
            context.getString(R.string.authentifiant_hint_secondary_login),
            item.syncObject.secondaryLogin,
            CopyField.SecondaryLogin,
            suggestions,
            ::copyForUpdatedSecondaryLogin
        )
        val websiteView = createWebsiteField(editMode, item, subViewFactory, context)

        val teamspaceView = createTeamspaceField(
            editMode = editMode,
            subViewFactory = subViewFactory,
            item = item,
            views = listOfNotNull(loginView, secondaryLoginView, emailView, websiteView)
        )

        return listOfNotNull(
            
            createChangePasswordInfobox(domain, editMode, context, item, listener),
            
            emailView,
            
            loginView,
            
            secondaryLoginView,
            
            passwordView,
            
            createPasswordSafetyField(editMode, context, item, listener),
            
            createAuthenticatorField(editMode, context, domain, otp, item, listener),
            
            websiteView,
            
            createLinkedServicesButton(editMode, context, listener, item),
            
            createAddWebsiteButton(editMode, context, listener, item),
            
            createNameField(editMode, subViewFactory, context, item),
            
            createEditNoteField(editMode, subViewFactory, context, item),
            
            teamspaceView,
            
            createReadOnlyNoteField(editMode, item, subViewFactory, context),
            
            createSharingField(item, context, subViewFactory),
            
            subViewFactory.createSubviewAttachmentDetails(context, item),
            
            dateTimeFieldFactory.createCreationDateField(
                editMode = editMode,
                context = context,
                item = item
            ),
            
            dateTimeFieldFactory.createLatestUpdateDateField(
                editMode = editMode,
                shared = item.isShared(),
                context = context,
                item = item
            ),
            
            createCollectionField(
                editMode = editMode,
                item = item,
                context = context,
                listener = listener,
                teamspaceView = teamspaceView as? ItemSubView<Teamspace>
            ),
            
            createDeleteButton(canDelete, context, subViewFactory, listener)
        )
    }

    private fun createCollectionField(
        editMode: Boolean,
        item: VaultItem<SyncObject.Authentifiant>,
        context: Context,
        listener: ItemEditViewContract.View.UiUpdateListener,
        teamspaceView: ItemSubView<Teamspace>?
    ): ItemSubView<*> {
        val collections = mainDataAccessor.getCollectionDataQuery().queryAll(
            CollectionFilter().apply {
                withVaultItem = CollectionVaultItems(item.toCollectionDataType(), item.uid)
                specificSpace(teamspaceAccessor.getOrDefault(item.syncObject.spaceId))
            }
        ).mapNotNull {
            val name = it.name ?: return@mapNotNull null
            name to false
        } + sharedCollections.map { it.name to true }
        val sortedCollections =
            collections.sortedWith(compareBy(Collator.getInstance()) { (name, _) -> name })
        val header = getCollectionListSubViewHeader(context, teamspaceView)
        return ItemCollectionListSubView(
            value = mutableStateOf(sortedCollections),
            editMode = editMode,
            itemId = item.uid,
            header = mutableStateOf(header),
            listener = listener,
            teamspaceView = teamspaceView
        )
    }

    private fun getCollectionListSubViewHeader(
        context: Context,
        teamspaceView: ItemSubView<Teamspace>?
    ): String {
        if (!userFeaturesChecker.has(FeatureFlip.SHARING_COLLECTION_MILESTONE_3) || teamspaceView?.value == null) {
            return context.getString(R.string.collections_header_item_edit)
        }
        val teamId = teamspaceView.value.teamId
        return if (teamspaceAccessor.onlyBusinessSpaces.firstOrNull { it.teamId == teamId } != null) {
            context.getString(R.string.collections_header_business_item_edit)
        } else {
            context.getString(R.string.collections_header_personal_item_edit)
        }
    }

    private fun createEditNoteField(
        editMode: Boolean,
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.Authentifiant>
    ): ItemSubView<*>? {
        return if (editMode) {
            subViewFactory.createSubViewString(
                header = context.getString(R.string.authentifiant_hint_note),
                value = item.syncObject.note,
                valueUpdate = ::copyForUpdatedNote,
                multiline = true
            )
        } else {
            null
        }
    }

    private fun createReadOnlyNoteField(
        editMode: Boolean,
        item: VaultItem<SyncObject.Authentifiant>,
        subViewFactory: SubViewFactory,
        context: Context
    ): ItemSubView<*>? {
        return if (!editMode) {
            
            if (item.syncObject.note.isNotSemanticallyNull()) {
                subViewFactory.createSubViewString(
                    header = context.getString(R.string.authentifiant_hint_note),
                    value = item.syncObject.note,
                    multiline = true
                )
            } else {
                null
            }
        } else {
            null
        }
    }

    private fun createTeamspaceField(
        editMode: Boolean,
        subViewFactory: SubViewFactory,
        item: VaultItem<SyncObject.Authentifiant>,
        views: List<ItemSubView<String>>
    ): ItemSubView<*>? {
        return if (teamspaceAccessor.canChangeTeamspace()) {
            subViewFactory.createSpaceSelector(
                item.syncObject.spaceId,
                teamspaceAccessor,
                views,
                ::copyForUpdatedTeamspace,
                getLinkedWebsites(item, editMode)
            )
        } else {
            null
        }
    }

    private fun createNameField(
        editMode: Boolean,
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.Authentifiant>
    ): ItemSubView<*>? {
        return if (editMode) {
            subViewFactory.createSubViewString(
                context.getString(R.string.authentifiant_hint_name),
                item.syncObject.title,
                false,
                ::copyForUpdatedName
            )
        } else {
            null
        }
    }

    private fun getLinkedWebsites(item: VaultItem<SyncObject.Authentifiant>, editMode: Boolean): List<String> {
        return if (temporaryLinkedWebsites != null && editMode) {
            temporaryLinkedWebsites!!
        } else {
            item.syncObject.linkedServices?.associatedDomains?.mapNotNull { it.domain } ?: listOf()
        }
    }

    private fun createAddWebsiteButton(
        editMode: Boolean,
        context: Context,
        listener: ItemEditViewContract.View.UiUpdateListener,
        item: VaultItem<SyncObject.Authentifiant>
    ): ItemSubView<*>? {
        return if (editMode) {
            ItemClickActionSubView(
                context.getString(R.string.multi_domain_credentials_add_service),
                mood = Mood.Neutral,
                intensity = Intensity.Quiet,
                iconResId = R.drawable.ic_add,
                gravity = Gravity.START,
            ) {
                listener.openLinkedServices(
                    item.uid,
                    fromViewOnly = false,
                    addNew = true,
                    temporaryWebsites = getLinkedWebsites(item = item, editMode = true),
                    temporaryApps = temporaryLinkedApps,
                    urlDomain = item.syncObject.urlDomain
                )
            }.apply {
                topMargin = R.dimen.spacing_empty
            }
        } else {
            return null
        }
    }

    private fun createLinkedServicesButton(
        editMode: Boolean,
        context: Context,
        listener: ItemEditViewContract.View.UiUpdateListener,
        item: VaultItem<SyncObject.Authentifiant>
    ): ItemSubView<*> {
        val linkedWebsites = temporaryLinkedWebsites?.let {
            linkedServicesHelper.replaceAllLinkedDomains(item.syncObject.linkedServices, it).associatedDomains
        } ?: item.syncObject.linkedServices?.associatedDomains
        val linkedApps = temporaryLinkedApps?.let {
            linkedServicesHelper.replaceAllLinkedAppsByUser(
                item.syncObject.linkedServices,
                it
            ).associatedAndroidApps
        } ?: item.syncObject.linkedServices?.associatedAndroidApps
        val autoFillNumber = (linkedWebsites?.size ?: 0) +
            (KnownLinkedDomains.getMatchingLinkedDomainSet(item.syncObject.urlDomain)?.size ?: 0) +
            (linkedApps?.size ?: 0)
        return if (autoFillNumber > 0) {
            ItemLinkedServicesSubView(
                linkedWebsites,
                linkedApps,
                OpenLinkedWebsitesAction(
                    listener,
                    item.uid,
                    editMode,
                    getLinkedWebsites(item, editMode),
                    temporaryLinkedApps,
                    item.syncObject.urlDomain
                ),
                context.resources.getQuantityString(
                    R.plurals.multi_domain_credentials_autofill_plurals,
                    autoFillNumber,
                    autoFillNumber
                )
            ).apply {
                topMargin = R.dimen.spacing_empty
            }
        } else {
            EmptyLinkedServicesSubView()
        }
    }

    private fun createWebsiteField(
        editMode: Boolean,
        item: VaultItem<SyncObject.Authentifiant>,
        subViewFactory: SubViewFactory,
        context: Context
    ): ItemSubView<String>? {
        val loginAction = if (editMode) {
            
            null
        } else {
            val loginListener = object : LoginOpener.Listener {
                override fun onShowOption() {
                }

                override fun onLogin(packageName: String) {
                    vaultItemLogger.logOpenExternalLink(
                        itemId = item.uid,
                        packageName = packageName,
                        url = item.syncObject.urlForGoToWebsite
                    )
                }
            }
            LoginAction(
                item.syncObject.urlForGoToWebsite ?: "",
                item.syncObject.toSummary<SummaryObject.Authentifiant>().linkedServices,
                loginListener
            )
        }
        val urlToDisplay = item.syncObject.urlForUI()?.let { fullUrl ->
            if (editMode) {
                
                fullUrl
            } else {
                
                fullUrl.toUrlOrNull()?.name
                    ?: fullUrl 
            }
        }
        return subViewFactory.createSubViewString(
            context.getString(R.string.authentifiant_hint_url),
            urlToDisplay,
            false,
            ::copyForUpdatedWebsite
        )?.let {
            if (loginAction == null) {
                it
            } else {
                ItemSubViewWithActionWrapper(it, loginAction)
            }
        }
    }

    private fun createPasswordSafetyField(
        editMode: Boolean,
        context: Context,
        item: VaultItem<SyncObject.Authentifiant>,
        listener: ItemEditViewContract.View.UiUpdateListener
    ) = if (editMode) {
        null
    } else {
        createSubViewPasswordSafety(
            context.getString(R.string.password_safety_label),
            item.syncObject.password?.toString().orEmpty(),
            listener = listener
        )
    }

    private fun createAuthenticatorField(
        editMode: Boolean,
        context: Context,
        domain: UrlDomain?,
        otp: Otp?,
        item: VaultItem<SyncObject.Authentifiant>,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): ItemSubView<*>? {
        val credentialName = item.syncObject.titleForListNormalized
            ?: context.getString(R.string.authenticator_default_account_name)
        return when {
            editMode -> {
                ItemAuthenticatorEditSubView(
                    credentialName,
                    item.uid,
                    domain?.root?.value,
                    item.toSummary<SummaryObject.Authentifiant>().linkedServices,
                    item.isSpaceItem() && item.syncObject.spaceId.isNotSemanticallyNull(),
                    otp,
                    ::copyForUpdatedOtp
                ).let {
                    ItemSubViewWithActionWrapper(
                        it,
                        ActivateRemoveAuthenticatorAction(it, listener, authenticatorLogger)
                    )
                }
            }

            otp?.secret.isNotSemanticallyNull() -> {
                val title = if (otp?.secret.isSemanticallyNull()) {
                    context.getString(R.string.authenticator_item_edit_activate_title)
                } else {
                    context.getString(R.string.authenticator_item_edit_activated_title)
                }
                ItemAuthenticatorReadSubView(title, credentialName, otp!!).let { subView ->
                    val action = AuthenticatorCodeCopyAction(
                        summaryObject = item.toSummary(),
                        vaultItemCopy = vaultItemCopy
                    ) {
                        vaultItemLogger.logCopyField(
                            field = Field.OTP_SECRET,
                            itemId = item.uid,
                            itemType = ItemType.CREDENTIAL,
                            isProtected = false,
                            domain = null
                        )
                    }
                    ItemSubViewWithActionWrapper(subView, action)
                }
            }

            else -> null
        }
    }

    private fun createPasswordField(
        editMode: Boolean,
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.Authentifiant>,
        isNew: Boolean,
        listener: ItemEditViewContract.View.UiUpdateListener,
        domain: UrlDomain?
    ): ItemSubView<*>? {
        return if (!editMode) {
            createPasswordFieldReadOnly(context, subViewFactory, item, isNew)
        } else {
            createPasswordFieldEdit(context, item, domain?.value ?: "", isNew, listener)
        }
    }

    private fun createPasswordFieldEdit(
        context: Context,
        item: VaultItem<SyncObject.Authentifiant>,
        domain: String,
        isNew: Boolean,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): ItemSubViewWithActionWrapper<String> {
        val passwordUpdate = ::copyForUpdatedPassword
        val subview = ItemEditPasswordWithStrengthSubView(
            context.getString(R.string.authentifiant_hint_password),
            item.syncObject.password?.toString().orEmpty(),
            passwordUpdate,
            protectedStateListener = { passwordShown ->
                if (passwordShown) logPasswordReveal(item)
            }
        ).apply {
            addValueChangedListener(object : ValueChangeManager.Listener<String> {
                override fun onValueChanged(origin: Any, newValue: String) {
                    listener.notifySubViewChanged(this@apply)
                }
            })
        }
        val origin = if (isNew) {
            PasswordGeneratorDialog.CREATION_VIEW
        } else {
            PasswordGeneratorDialog.EDIT_VIEW
        }
        return ItemSubViewWithActionWrapper(
            subview,
            GeneratePasswordAction(domain, origin) { id, password ->
                
                subview.notifyValueChanged(password)
                lastGeneratedPasswordId = id
            }
        )
    }

    private fun createPasswordFieldReadOnly(
        context: Context,
        subViewFactory: SubViewFactory,
        item: VaultItem<SyncObject.Authentifiant>,
        isNew: Boolean
    ): ItemSubView<*>? {
        val canEditItem = sharingPolicy.canEditItem(item.toSummary(), isNew)
        return subViewFactory.createSubViewString(
            context.getString(R.string.authentifiant_hint_password),
            item.syncObject.password?.toString(),
            true,
            allowReveal = canEditItem,
            coloredCharacter = true
        ) { passwordShown ->
            if (passwordShown) logPasswordReveal(item)
        }?.let {
            if (canEditItem) {
                
                ItemSubViewWithActionWrapper(
                    it,
                    CopyAction(
                        summaryObject = item.toSummary(),
                        copyField = CopyField.Password,
                        action = {
                            isPasswordCopied = true
                        },
                        vaultItemCopy = vaultItemCopy
                    )
                )
            } else {
                
                ItemSubViewWithActionWrapper(
                    it,
                    LimitedSharingRightsInfoAction()
                )
            }
        }
    }

    private fun createEmailField(
        context: Context,
        editMode: Boolean,
        subViewFactory: SubViewFactory,
        item: VaultItem<SyncObject.Authentifiant>,
        suggestions: List<String>
    ): ItemSubView<String>? {
        val authentifiant = item.syncObject
        
        
        return if (authentifiant.login.isNotSemanticallyNull() && authentifiant.email.isNotSemanticallyNull()) {
            return subViewFactory.createSubViewString(
                context.getString(R.string.email),
                item.syncObject.email,
                false,
                ::copyForUpdatedEmail,
                suggestions
            )?.let {
                if (editMode) return it
                ItemSubViewWithActionWrapper(
                    it,
                    CopyAction(
                        summaryObject = item.toSummary(),
                        copyField = CopyField.Email,
                        vaultItemCopy = vaultItemCopy
                    )
                )
            }
        } else {
            null
        }
    }

    private fun createLoginField(
        editMode: Boolean,
        subViewFactory: SubViewFactory,
        item: VaultItem<SyncObject.Authentifiant>,
        header: String,
        value: String?,
        copyField: CopyField,
        suggestions: List<String>,
        valueUpdate: (VaultItem<*>, String) -> VaultItem<*>
    ): ItemSubView<String>? {
        return if (!editMode) {
            createLoginFieldReadOnly(subViewFactory, header, value, copyField, item)
        } else {
            createLoginFieldEditMode(subViewFactory, header, value, suggestions, valueUpdate)
        }
    }

    private fun createLoginFieldEditMode(
        subViewFactory: SubViewFactory,
        header: String,
        value: String?,
        suggestions: List<String>,
        valueUpdate: (VaultItem<*>, String) -> VaultItem<*>
    ) = subViewFactory.createSubViewString(header, value, false, valueUpdate, suggestions)

    private fun createLoginFieldReadOnly(
        subViewFactory: SubViewFactory,
        header: String,
        value: String?,
        copyField: CopyField,
        item: VaultItem<SyncObject.Authentifiant>
    ) = subViewFactory.createSubViewString(header, value, false)?.let {
        ItemSubViewWithActionWrapper(
            it,
            CopyAction(
                summaryObject = item.toSummary(),
                copyField = copyField,
                action = {
                    isLoginCopied = true
                },
                vaultItemCopy = vaultItemCopy
            )
        )
    }

    private fun getLoginHeader(
        item: VaultItem<SyncObject.Authentifiant>,
        context: Context
    ): String {
        return if (item.syncObject.isLoginEmail()) {
            context.getString(R.string.email_hint_email)
        } else {
            context.getString(R.string.authentifiant_hint_login)
        }
    }

    private fun createSubViewPasswordSafety(
        header: String,
        password: String,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): ItemSubView<String> {
        val subView = ItemPasswordSafetySubView(header, password)
        password.let {
            subView.value = it
            listener.notifySubViewChanged(subView)
        }
        return subView
    }

    private fun createSharingField(
        item: VaultItem<SyncObject.Authentifiant>,
        context: Context,
        subViewFactory: SubViewFactory
    ): ItemSubView<*>? = subViewFactory.createSubviewSharingDetails(context, item, sharingPolicy)

    private fun createDeleteButton(
        canDelete: Boolean,
        context: Context,
        subViewFactory: SubViewFactory,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): ItemSubView<*>? =
        subViewFactory.createSubviewDelete(context, listener, canDelete, context.getString(R.string.delete_password))

    private fun logPasswordReveal(item: VaultItem<SyncObject.Authentifiant>) {
        vaultItemLogger.logRevealField(Field.PASSWORD, item.uid, ItemType.CREDENTIAL, item.syncObject.urlForUsageLog)
    }

    private fun createChangePasswordInfobox(
        domain: UrlDomain?,
        editMode: Boolean,
        context: Context,
        item: VaultItem<SyncObject.Authentifiant>,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): ItemSubView<*>? {
        return when {
            editMode || domain?.value == null -> null
            isCompromised(item.syncObject.password?.toString().orEmpty()) -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    
                    null
                } else {
                    val username = item.syncObject.loginForUi
                    ItemInfoboxSubView(
                        value = context.getString(R.string.infobox_change_password_title),
                        mood = Mood.Warning,
                        primaryButton = InfoboxButton(
                            text = context.getString(R.string.infobox_change_password_button),
                            onClick = {
                                navigator.goToGuidedPasswordChangeFromCredential(
                                    item.uid,
                                    domain.value,
                                    username,
                                    GUIDED_PASSWORD_CHANGE_REQUEST_CODE
                                )
                            }
                        )

                    )
                }
            }

            else -> {
                val changeDate = item.getPreviousPassword(item.syncObject.password?.toString(), mainDataAccessor)?.first
                if (changeDate?.plus(Duration.ofDays(2))?.isAfter(Instant.now()) == true) {
                    val formattedDate = DateUtils.formatDateTime(
                        context,
                        changeDate.toEpochMilli(),
                        DateUtils.FORMAT_SHOW_YEAR
                    )
                    ItemInfoboxSubView(
                        value = context.getString(R.string.infobox_restore_password_title, formattedDate),
                        mood = Mood.Neutral,
                        secondaryButton = InfoboxButton(
                            text = context.getString(R.string.infobox_restore_password_button),
                            onClick = { listener.showRestorePromptDialog() }
                        )
                    )
                } else {
                    null
                }
            }
        }
    }

    private fun isCompromised(password: String): Boolean {
        val filter = vaultFilter { specificDataType(SyncObjectType.SECURITY_BREACH) }
        return mainDataAccessor.getVaultDataQuery().queryAll(filter).isCompromised(SimilarPassword(), password)
    }

    companion object {
        const val LAST_GENERATED_PASSWORD_BUNDLE_KEY = "last_generated_password_bundle_key"
        const val LINKED_WEBSITES_BUNDLE_KEY = "linked_websites_bundle_key"
        const val LINKED_APPS_BUNDLE_KEY = "linked_apps_bundle_key"
        const val GUIDED_PASSWORD_CHANGE_REQUEST_CODE = 8374
    }
}

internal fun VaultItem<SyncObject.Authentifiant>.getPreviousPassword(
    currentPassword: String?,
    mainDataAccessor: MainDataAccessor
): Pair<Instant, String>? {
    
    
    
    val filter = DataChangeHistoryFilter(SyncObjectType.AUTHENTIFIANT, uid)
    val dataChangeHistory =
        mainDataAccessor.getDataChangeHistoryQuery().query(filter) ?: return null
    val changeSets = dataChangeHistory.syncObject.changeSets?.sortedByDescending { it.modificationDate } ?: return null
    val previousPasswordIndex = changeSets.indexOfFirst { it.password != currentPassword }
    if (previousPasswordIndex <= 0) return null
    val modificationDate = changeSets[previousPasswordIndex - 1].modificationDate ?: return null
    val password = changeSets[previousPasswordIndex].password ?: return null
    return modificationDate to password
}

@Suppress("UNCHECKED_CAST")
private fun ItemScreenConfigurationAuthentifiantProvider.copyForUpdatedNote(
    item: VaultItem<*>,
    value: String
): VaultItem<SyncObject.Authentifiant> {
    item as VaultItem<SyncObject.Authentifiant>
    val authentifiant = item.syncObject
    return if (value == authentifiant.note.orEmpty()) {
        item
    } else {
        editedFields += Field.NOTE
        item.copySyncObject { note = value }
    }
}

@Suppress("UNCHECKED_CAST")
private fun copyForUpdatedTeamspace(
    item: VaultItem<*>,
    value: Teamspace
): VaultItem<SyncObject.Authentifiant> {
    item as VaultItem<SyncObject.Authentifiant>
    val authentifiant = item.syncObject
    val spaceId = authentifiant.spaceId ?: PersonalTeamspace.teamId
    return if (value.teamId == spaceId) {
        item
    } else {
        item.copySyncObject { this.spaceId = value.teamId }
    }
}

@Suppress("UNCHECKED_CAST")
private fun ItemScreenConfigurationAuthentifiantProvider.copyForUpdatedName(
    item: VaultItem<*>,
    value: String
): VaultItem<SyncObject.Authentifiant> {
    item as VaultItem<SyncObject.Authentifiant>
    val authentifiant = item.syncObject
    return if (value == authentifiant.title.orEmpty()) {
        item
    } else {
        editedFields += Field.TITLE
        item.copySyncObject { title = value }
    }
}

@Suppress("UNCHECKED_CAST")
private fun ItemScreenConfigurationAuthentifiantProvider.copyForUpdatedWebsite(
    item: VaultItem<*>,
    value: String
): VaultItem<SyncObject.Authentifiant> {
    item as VaultItem<SyncObject.Authentifiant>
    val authentifiant = item.syncObject
    return if (value == authentifiant.urlForUI().orEmpty()) {
        item
    } else {
        editedFields += Field.URL
        item.copySyncObject {
            url = value
            userSelectedUrl = value
            useFixedUrl = true
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun ItemScreenConfigurationAuthentifiantProvider.copyForUpdatedOtp(
    item: VaultItem<*>,
    value: Otp?
): VaultItem<SyncObject.Authentifiant> {
    item as VaultItem<SyncObject.Authentifiant>
    val authentifiant = item.syncObject
    return if (authentifiant.otpSecret.matchesNullAsEmpty(value?.secret ?: "") &&
        authentifiant.otpUrl.matchesNullAsEmpty(value?.url ?: "")
    ) {
        item
    } else if (value?.isStandardOtp() == false &&
        authentifiant.otpUrl.matchesNullAsEmpty(
            value.url ?: ""
        ) && authentifiant.otpSecret.isNullOrEmpty()
    ) {
        item
    } else {
        editedFields += Field.OTP_SECRET
        item.copySyncObject {
            otpUrl = value?.url?.toSyncObfuscatedValue() ?: SyncObfuscatedValue("")
            otpSecret = if (value?.isStandardOtp() == true) {
                value.secret?.toSyncObfuscatedValue()
            } else {
                null
            } ?: SyncObfuscatedValue("")
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun ItemScreenConfigurationAuthentifiantProvider.copyForUpdatedPassword(
    item: VaultItem<*>,
    value: String
): VaultItem<SyncObject.Authentifiant> {
    item as VaultItem<SyncObject.Authentifiant>
    val actualPassword = item.syncObject.password
    return if (actualPassword.matchesNullAsEmpty(value)) {
        item
    } else {
        editedFields += Field.PASSWORD
        item.copySyncObject {
            password = value.toSyncObfuscatedValue()
            modificationDatetime = Instant.ofEpochMilli(System.currentTimeMillis())
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun ItemScreenConfigurationAuthentifiantProvider.copyForUpdatedLogin(
    item: VaultItem<*>,
    value: String
): VaultItem<SyncObject.Authentifiant> {
    item as VaultItem<SyncObject.Authentifiant>
    val login = item.syncObject.login.orEmpty()
    val validEmail = value.isValidEmail()
    
    
    val validEmailAsLogin =
        validEmail && item.syncObject.email.orEmpty() == value && login.isSemanticallyNull()
    return when {
        validEmailAsLogin || login == value -> item
        
        validEmail -> {
            editedFields += Field.LOGIN
            item.copySyncObject { email = value; this.login = null }
        }
        
        else -> {
            editedFields += Field.LOGIN
            item.copySyncObject { this.login = value }
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun ItemScreenConfigurationAuthentifiantProvider.copyForUpdatedSecondaryLogin(
    item: VaultItem<*>,
    value: String
): VaultItem<SyncObject.Authentifiant> {
    item as VaultItem<SyncObject.Authentifiant>
    return if (value == item.syncObject.secondaryLogin.orEmpty()) {
        item
    } else {
        editedFields += Field.SECONDARY_LOGIN
        item.copySyncObject { secondaryLogin = value }
    }
}

@Suppress("UNCHECKED_CAST")
private fun ItemScreenConfigurationAuthentifiantProvider.copyForUpdatedEmail(
    item: VaultItem<*>,
    value: String
): VaultItem<SyncObject.Authentifiant> {
    item as VaultItem<SyncObject.Authentifiant>
    return when {
        value.isValidEmail() && value == item.syncObject.email.orEmpty() -> item
        value.isValidEmail() -> {
            editedFields += Field.EMAIL
            item.copySyncObject { email = value }
        }

        else -> {
            editedFields += Field.EMAIL
            item.copySyncObject { email = null }
        }
    }
}
