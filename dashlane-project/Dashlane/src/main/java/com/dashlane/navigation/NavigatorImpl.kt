package com.dashlane.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController.OnDestinationChangedListener
import androidx.navigation.NavDestination
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.dashlane.DrawerNavigationDirections
import com.dashlane.R
import com.dashlane.attachment.ui.AttachmentListActivity
import com.dashlane.authenticator.AuthenticatorIntro
import com.dashlane.authenticator.Otp
import com.dashlane.biometricrecovery.BiometricRecovery
import com.dashlane.biometricrecovery.MasterPasswordResetIntroActivity
import com.dashlane.collections.details.CollectionDetailsFragmentDirections
import com.dashlane.collections.list.CollectionsListFragmentDirections
import com.dashlane.collections.sharing.share.CollectionNewShareActivity
import com.dashlane.collections.sharing.share.CollectionNewShareActivity.Companion.SHARE_COLLECTION
import com.dashlane.collections.sharing.share.CollectionNewShareActivityArgs
import com.dashlane.featureflipping.FeatureFlip.NEW_ITEM_EDIT_SECURE_NOTES
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.featureflipping.canShowVpn
import com.dashlane.featureflipping.canUpgradeToGetVpn
import com.dashlane.followupnotification.discovery.FollowUpNotificationDiscoveryActivity
import com.dashlane.followupnotification.discovery.FollowUpNotificationDiscoveryActivityArgs
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.guidedpasswordchange.OnboardingGuidedPasswordChangeActivity
import com.dashlane.guidedpasswordchange.OnboardingGuidedPasswordChangeActivityArgs
import com.dashlane.help.HelpCenterLink
import com.dashlane.help.newIntent
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.item.delete.DeleteVaultItemFragment
import com.dashlane.item.delete.DeleteVaultItemFragmentArgs
import com.dashlane.item.subview.action.LoginOpener
import com.dashlane.item.subview.action.authenticator.ActivateRemoveAuthenticatorAction
import com.dashlane.item.v3.ItemEditFragmentDirections
import com.dashlane.lock.LockEvent
import com.dashlane.lock.LockManager
import com.dashlane.lock.LockPrompt
import com.dashlane.lock.LockSetting
import com.dashlane.login.LoginActivity
import com.dashlane.login.lock.unlockItemIfNeeded
import com.dashlane.navigation.NavControllerUtils.TOP_LEVEL_DESTINATIONS
import com.dashlane.navigation.NavControllerUtils.setup
import com.dashlane.navigation.NavControllerUtils.setupActionBar
import com.dashlane.navigation.paywall.PaywallIntroType
import com.dashlane.notificationcenter.NotificationCenterFragmentDirections
import com.dashlane.notificationcenter.details.NotificationCenterSectionDetailsFragmentDirections
import com.dashlane.premium.offer.list.view.OffersActivity
import com.dashlane.premium.offer.list.view.OffersActivityArgs
import com.dashlane.premium.paywall.common.PaywallActivity
import com.dashlane.premium.paywall.common.PaywallActivityArgs
import com.dashlane.security.DashlaneIntent
import com.dashlane.security.darkwebmonitoring.DarkWebMonitoringFragmentDirections
import com.dashlane.security.identitydashboard.IdentityDashboardFragmentDirections
import com.dashlane.security.identitydashboard.breach.BreachWrapper
import com.dashlane.server.api.endpoints.premium.PremiumStatus.PremiumCapability.Capability
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.teamspaces.ui.Feature
import com.dashlane.teamspaces.ui.TeamSpaceRestrictionNotificator
import com.dashlane.ui.AbstractActivityLifecycleListener
import com.dashlane.ui.activities.DashlaneWrapperActivity
import com.dashlane.ui.activities.HomeActivity
import com.dashlane.ui.activities.fragments.checklist.ChecklistHelper
import com.dashlane.ui.activities.hideDialogs
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.ui.adapter.toAnyPage
import com.dashlane.ui.screens.activities.onboarding.hardwareauth.HardwareAuthActivationActivity
import com.dashlane.ui.screens.activities.onboarding.hardwareauth.OnboardingHardwareAuthActivity
import com.dashlane.ui.screens.fragments.account.AccountStatusFragmentDirections
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingCenterFragmentDirections
import com.dashlane.ui.screens.fragments.userdata.sharing.itemselection.SharingItemSelectionTabFragmentDirections
import com.dashlane.ui.screens.settings.SettingsFragmentDirections
import com.dashlane.ui.screens.sharing.SharingNewSharePeopleFragment
import com.dashlane.ui.screens.sharing.SharingNewSharePeopleFragmentDirections
import com.dashlane.util.DeviceUtils
import com.dashlane.util.clearTask
import com.dashlane.util.clearTop
import com.dashlane.util.getBaseActivity
import com.dashlane.util.launchUrl
import com.dashlane.util.logPageView
import com.dashlane.util.newTask
import com.dashlane.util.safelyStartBrowserActivity
import com.dashlane.util.singleTop
import com.dashlane.util.startActivityForResult
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.MainCoroutineDispatcher
import com.dashlane.vpn.thirdparty.VpnThirdPartyFragmentDirections
import com.dashlane.xml.domain.SyncObjectType.AUTHENTIFIANT
import com.dashlane.xml.domain.SyncObjectXmlName
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("LargeClass")
@Singleton
class NavigatorImpl @Inject constructor(
    private val userFeaturesChecker: UserFeaturesChecker,
    private val sessionManager: SessionManager,
    private val lockManager: LockManager,
    private val genericDataQuery: GenericDataQuery,
    private val checklistHelper: ChecklistHelper,
    @ApplicationCoroutineScope
    private val applicationCoroutineScope: CoroutineScope,
    @MainCoroutineDispatcher
    private val mainDispatcher: CoroutineDispatcher,
    private val frozenStateManager: FrozenStateManager,
    private val restrictionNotificator: TeamSpaceRestrictionNotificator,
    private val biometricRecovery: BiometricRecovery
) : Navigator, AbstractActivityLifecycleListener() {

    override val currentDestination: NavDestination?
        get() = navigationController?.currentDestination
    private var menuIconDestinationChangedListener: MenuIconDestinationChangedListener? = null
    private var customTitleDestinationChangedListener: CustomTitleDestinationChangedListener? = null
    private var deepLinkFound: Boolean = false

    private var currentActivity: WeakReference<Activity>? = null
        set(value) {
            currentActivity?.get()?.apply {
                
                val controller = navigationController ?: return@apply
                menuIconDestinationChangedListener?.let {
                    controller.removeOnDestinationChangedListener(it)
                }
            }
            field = value
            navigationController?.apply {
                val topLevelDestinations = menuIconDestinationChangedListener?.topLevelDestinations
                    ?: TOP_LEVEL_DESTINATIONS
                setup(activity, deepLinkFound, checklistHelper, topLevelDestinations)
                menuIconDestinationChangedListener = MenuIconDestinationChangedListener(
                    activity,
                    topLevelDestinations
                )
                addOnDestinationChangedListener(menuIconDestinationChangedListener!!)
                customTitleDestinationChangedListener =
                    CustomTitleDestinationChangedListener(activity)
                addOnDestinationChangedListener(customTitleDestinationChangedListener!!)
            }
        }

    private val activity: Activity
        get() = currentActivity!!.get()!!
    private val navigationController
        get() = try {
            currentActivity?.get()?.findNavController(R.id.nav_host_fragment)
        } catch (e: IllegalArgumentException) {
            
            null
        }
    private val navDeepLinkHelper = NavDeepLinkHelper(navigator = this)

    override fun launchHomeActivity() {
        activity.startActivity(Intent(activity, HomeActivity::class.java).newTask().clearTask())
    }

    override fun goToHome(filter: String?) {
        val action =
            DrawerNavigationDirections.goToHome(filter = filter)
        navigate(action)
    }

    override fun goToActionCenter() {
        val action =
            DrawerNavigationDirections.goToNotificationCenter()
        navigate(action)
    }

    override fun goToPasswordGenerator() {
        val action =
            DrawerNavigationDirections.goToPasswordGenerator()
        navigate(action)
    }

    override fun goToPasswordSharing() {
        val action = DrawerNavigationDirections.goToSharingCenter()
        navigate(action)
    }

    override fun goToIdentityDashboard() {
        val action =
            DrawerNavigationDirections.goToIdentityDashboard()
        navigate(action)
    }

    override fun goToDarkWebMonitoring() {
        val action =
            when (userFeaturesChecker.has(Capability.DATALEAK)) {
                true -> DrawerNavigationDirections.goToDarkWebMonitoring()
                else -> DrawerNavigationDirections.goToPaywall(
                    paywallIntroType = PaywallIntroType.DARK_WEB_MONITORING
                )
            }
        navigate(action)
    }

    override fun goToVpn() {
        val action = when {
            !userFeaturesChecker.canShowVpn() -> {
                
                return
            }

            userFeaturesChecker.has(Capability.SECUREWIFI) -> {
                DrawerNavigationDirections.goToThirdPartyVpn()
            }

            userFeaturesChecker.canUpgradeToGetVpn() -> {
                DrawerNavigationDirections.goToPaywall(
                    paywallIntroType = PaywallIntroType.VPN
                )
            }

            else -> {
                DrawerNavigationDirections.goToVpnB2bIntro()
            }
        }
        navigate(action)
    }

    override fun goToCollectionsList() {
        navigate(DrawerNavigationDirections.goToCollectionsList())
    }

    override fun goToAuthenticator(otpUri: Uri?) {
        navigate(DrawerNavigationDirections.goToAuthenticatorDashboard(otpUri))
    }

    override fun goToAuthenticatorSuggestions(hasSetupOtpCredentials: Boolean) {
        if (hasSetupOtpCredentials) {
            
            
            waitForNavControllerIfNeeded {
                val topLevelDestinations =
                    TOP_LEVEL_DESTINATIONS.filter {
                        it != R.id.nav_authenticator_suggestions
                    }.toSet()
                navigationController!!.setupActionBar(activity, topLevelDestinations)
                menuIconDestinationChangedListener?.topLevelDestinations = topLevelDestinations
            }
        }
        navigate(DrawerNavigationDirections.goToAuthenticatorSuggestions(hasSetupOtpCredentials))
    }

    override fun goToAuthenticatorIntro(
        credentialName: String,
        credentialId: String,
        topDomain: String,
        packageName: String?,
        proSpace: Boolean
    ) {
        if (frozenStateManager.isAccountFrozen) {
            goToPaywall(PaywallIntroType.FROZEN_ACCOUNT)
            return
        }

        activity.startActivityForResult<AuthenticatorIntro>(ActivateRemoveAuthenticatorAction.REQUEST_CODE) {
            putExtra(AuthenticatorIntro.EXTRA_CREDENTIAL_NAME, credentialName)
            putExtra(AuthenticatorIntro.EXTRA_CREDENTIAL_ID, credentialId)
            putExtra(AuthenticatorIntro.EXTRA_CREDENTIAL_TOP_DOMAIN, topDomain)
            putExtra(AuthenticatorIntro.EXTRA_CREDENTIAL_PACKAGE_NAME, packageName)
            putExtra(AuthenticatorIntro.EXTRA_CREDENTIAL_PROFESSIONAL, proSpace)
        }
    }

    override fun goToLearnMoreAboutVpnFromVpnThirdParty() {
        navigate(VpnThirdPartyFragmentDirections.goToLearnMoreAboutVpnFromVpnThirdParty())
    }

    override fun goToGetStartedFromVpnThirdParty() {
        navigate(VpnThirdPartyFragmentDirections.goToGetStartedFromVpnThirdParty())
    }

    override fun goToActivateAccountFromVpnThirdParty(
        defaultEmail: String?,
        suggestions: List<String>?
    ) {
        navigate(
            VpnThirdPartyFragmentDirections.goToActivateAccountFromVpnThirdParty(
                email = defaultEmail,
                suggestions = suggestions?.toTypedArray()
            )
        )
    }

    override fun goToFollowUpNotificationDiscoveryScreen(isReminder: Boolean) {
        val bundle =
            FollowUpNotificationDiscoveryActivityArgs(isReminder = isReminder).toBundle()
        activity.startActivity(
            Intent(activity, FollowUpNotificationDiscoveryActivity::class.java).apply {
                putExtras(bundle)
            }
        )
    }

    override fun goToQuickActions(itemId: String, itemListContext: Parcelable) {
        val originPage = (itemListContext as ItemListContext).toAnyPage()
        navigate(
            DrawerNavigationDirections.goToQuickActions(
                itemId = itemId,
                itemListContext = itemListContext,
                originPage = originPage?.code
            )
        )
    }

    override fun goToSettings(settingsId: String?) {
        val action = DrawerNavigationDirections.goToSettings(id = settingsId)
        navigate(action)
    }

    override fun goToHelpCenter() {
        activity.logPageView(AnyPage.HELP)
        val intent = HelpCenterLink.Base.newIntent(
            context = activity
        )
        activity.safelyStartBrowserActivity(intent)
    }

    override fun goToPersonalPlanOrHome() {
        if (checklistHelper.shouldDisplayChecklist()) {
            goToPersonalPlan()
        } else {
            goToHome()
        }
    }

    private fun goToPersonalPlan() {
        val action = DrawerNavigationDirections.goToPersonalPlan()
        navigate(action)
    }

    override fun goToOffers(offerType: String?) {
        if (navigationController == null) {
            
            val extras = OffersActivityArgs(offerType = offerType).toBundle()
            activity.startActivity(
                Intent(activity, OffersActivity::class.java).apply {
                    putExtras(extras)
                }
            )
            return
        }
        val action = DrawerNavigationDirections.goToOffers(offerType = offerType)
        navigate(action)
    }

    override fun goToActionCenterSectionDetails(section: String) {
        val action =
            DrawerNavigationDirections.goToActionCenterSectionDetails(extraSection = section)
        navigate(action)
    }

    override fun goToSectionDetailsFromActionCenter(section: String) {
        val action =
            NotificationCenterFragmentDirections.actionCenterToSectionDetails(extraSection = section)
        navigate(action)
    }

    override fun goToBreachAlertDetail(breachWrapper: Parcelable) {
        val breach = breachWrapper as BreachWrapper
        if (breachWrapper.publicBreach.isDarkWebBreach() && !userFeaturesChecker.has(Capability.DATALEAK)) {
            goToPaywall(type = PaywallIntroType.DARK_WEB_MONITORING)
            return
        }
        val action = when (navigationController?.currentDestination?.id) {
            R.id.nav_dark_web_monitoring ->
                DarkWebMonitoringFragmentDirections.darkWebMonitoringToBreachAlertDetail(
                    breach = breach,
                )

            R.id.nav_notif_center ->
                NotificationCenterFragmentDirections.actionCenterToBreachAlertDetail(
                    breach = breach,
                )

            R.id.nav_action_center_section_details ->
                NotificationCenterSectionDetailsFragmentDirections.actionCenterSectionToBreachAlertDetail(
                    breach = breach,
                )

            else -> DrawerNavigationDirections.goToBreachAlertDetail(breach = breach)
        }
        navigate(action)
    }

    override fun goToItem(uid: String, type: String, editMode: Boolean) {
        applicationCoroutineScope.launch(Dispatchers.Main.immediate) {
            
            if (!lockManager.unlockItemIfNeeded(
                    context = activity,
                    dataQuery = genericDataQuery,
                    uid = uid,
                    type = type,
                )
            ) {
                return@launch
            }

            if (isNewItemEditEnabled(type)) {
                navigate(
                    DrawerNavigationDirections.goToNewItemEdit(
                        uid = uid,
                        dataType = type,
                        forceEdit = editMode
                    )
                )
            } else {
                navigate(
                    DrawerNavigationDirections.goToItemEdit(
                        uid = uid,
                        dataType = type,
                        forceEdit = editMode
                    )
                )
            }
        }
    }

    override fun goToItemHistory(uid: String) {
        navigate(
            ItemEditFragmentDirections.actionNavNewItemEditToNavPasswordHistory(uid = uid)
        )
    }

    override fun goToCreateItem(type: String) {
        if (frozenStateManager.isAccountFrozen) {
            goToPaywall(PaywallIntroType.FROZEN_ACCOUNT)
            return
        }

        if (isNewItemEditEnabled(type)) {
            navigate(DrawerNavigationDirections.goToNewItemEdit(dataType = type))
        } else {
            navigate(DrawerNavigationDirections.goToItemEdit(dataType = type))
        }
    }

    override fun goToCreateAuthentifiant(
        url: String,
        requestCode: Int?,
        successIntent: Intent?,
        otp: Parcelable?
    ) {
        if (frozenStateManager.isAccountFrozen) {
            goToPaywall(PaywallIntroType.FROZEN_ACCOUNT)
            return
        }

        navigate(
            DrawerNavigationDirections.goToNewItemEdit(
                dataType = AUTHENTIFIANT.xmlObjectName,
                url = url,
                otp = otp as? Otp
            )
        )
    }

    override fun goToDeleteVaultItem(itemId: String, isShared: Boolean) {
        if (navigationController == null) {
            val argsBundle =
                DeleteVaultItemFragmentArgs(itemId = itemId, isShared = isShared).toBundle()
            DeleteVaultItemFragment().apply { arguments = argsBundle }
                .show((activity as AppCompatActivity).supportFragmentManager, DeleteVaultItemFragment.DELETE_DIALOG_TAG)
            return
        }
        val action = DrawerNavigationDirections.goToDeleteItem(itemId = itemId, isShared = isShared)
        navigate(action)
    }

    override fun goToCollectionSelectorFromItemEdit(
        fromViewOnly: Boolean,
        temporaryPrivateCollectionsName: List<String>,
        temporarySharedCollectionsId: List<String>,
        spaceId: String?,
        isLimited: Boolean
    ) {
        navigate(
            ItemEditFragmentDirections.actionNavNewItemEditToNavCollectionSelectorFragment(
                temporaryPrivateCollectionsName = temporaryPrivateCollectionsName.toTypedArray(),
                temporarySharedCollectionsId = temporarySharedCollectionsId.toTypedArray(),
                fromView = fromViewOnly,
                spaceId = spaceId,
                isLimited = isLimited
            )
        )
    }

    override fun goToCredentialAddStep1(
        expandImportOptions: Boolean,
        successIntent: Intent?
    ) {
        if (frozenStateManager.isAccountFrozen) {
            goToPaywall(PaywallIntroType.FROZEN_ACCOUNT)
            return
        }

        val action = DrawerNavigationDirections.goToCredentialAddStep1(
            expandImportOptions = expandImportOptions,
            paramSuccessIntent = successIntent,
        )
        navigate(action)
    }

    override fun goToCollectionDetailsFromCollectionsList(
        collectionId: String,
        businessSpace: Boolean,
        sharedCollection: Boolean,
        shareAllowed: Boolean
    ) {
        val action = CollectionsListFragmentDirections.collectionsListToCollectionDetails(
            collectionId = collectionId,
            businessSpace = businessSpace,
            sharedCollection = sharedCollection,
            shareAllowed = shareAllowed
        )
        navigate(action)
    }

    override fun goToCollectionAddFromCollectionsList() {
        if (frozenStateManager.isAccountFrozen) {
            goToPaywall(PaywallIntroType.FROZEN_ACCOUNT)
            return
        }

        val action = CollectionsListFragmentDirections.collectionsListToCollectionEdit(null, false)
        navigate(action)
    }

    override fun goToCollectionEditFromCollectionsList(collectionId: String, sharedCollection: Boolean) {
        if (frozenStateManager.isAccountFrozen) {
            goToPaywall(PaywallIntroType.FROZEN_ACCOUNT)
            return
        }

        val action = CollectionsListFragmentDirections.collectionsListToCollectionEdit(collectionId, sharedCollection)
        navigate(action)
    }

    override fun goToCollectionShareFromCollectionList(collectionId: String) {
        val args = CollectionNewShareActivityArgs(collectionId).toBundle()
        activity.startActivityForResult<CollectionNewShareActivity>(SHARE_COLLECTION) {
            putExtras(args)
        }
    }

    override fun goToCollectionSharedAccessFromCollectionsList(collectionId: String) {
        val action = CollectionsListFragmentDirections.collectionsListToCollectionSharedAccess(
            collectionId = collectionId
        )
        navigate(action)
    }

    override fun goToCollectionDetails(
        collectionId: String,
        businessSpace: Boolean,
        sharedCollection: Boolean,
        shareAllowed: Boolean
    ) {
        val action = DrawerNavigationDirections.goToCollectionDetails(
            collectionId = collectionId,
            businessSpace = businessSpace,
            sharedCollection = sharedCollection,
            shareAllowed = shareAllowed
        )
        navigate(action)
    }

    override fun goToCollectionEditFromCollectionDetail(collectionId: String, sharedCollection: Boolean) {
        if (frozenStateManager.isAccountFrozen) {
            goToPaywall(PaywallIntroType.FROZEN_ACCOUNT)
            return
        }

        val action =
            CollectionDetailsFragmentDirections.collectionDetailsToCollectionEdit(collectionId, sharedCollection)
        navigate(action)
    }

    override fun goToCollectionShareFromCollectionDetail(collectionId: String) {
        val args = CollectionNewShareActivityArgs(collectionId).toBundle()
        activity.startActivityForResult<CollectionNewShareActivity>(SHARE_COLLECTION) {
            putExtras(args)
        }
    }

    override fun goToCollectionSharedAccessFromCollectionDetail(collectionId: String) {
        val action = CollectionDetailsFragmentDirections.collectionsDetailsToCollectionSharedAccess(
            collectionId = collectionId
        )
        navigate(action)
    }

    override fun goToInAppLoginIntro() {
        val action = DrawerNavigationDirections.goToInAppLoginIntro()
        navigate(action)
    }

    override fun goToInAppLogin() {
        val action = DrawerNavigationDirections.goToInAppLogin()
        navigate(action)
    }

    override fun goToSearch(query: String?) {
        val action = DrawerNavigationDirections.goToSearch(argsQuery = query)
        navigate(action, keepKeyboardOpen = true)
    }

    override fun goToCredentialFromPasswordAnalysis(uid: String) {
        navigate(
            DrawerNavigationDirections.goToNewItemEdit(
                uid = uid,
                dataType = AUTHENTIFIANT.xmlObjectName
            )
        )
    }

    override fun goToPasswordAnalysisFromBreach(breachId: String) {
        
        val action = DrawerNavigationDirections.goToPasswordAnalysis(
            breachFocus = breachId,
        )
        navigate(action)
    }

    override fun goToPasswordAnalysisFromIdentityDashboard(tab: String?) {
        if (frozenStateManager.isAccountFrozen) {
            goToPaywall(PaywallIntroType.FROZEN_ACCOUNT)
            return
        }

        val action =
            IdentityDashboardFragmentDirections.identityDashboardToPasswordAnalysis(
                tab = tab,
            )
        navigate(action)
    }

    override fun goToNewShare() {
        val action = DrawerNavigationDirections.goToNewShare()
        navigate(action)
    }

    override fun goToSharePeopleSelection(
        selectedPasswords: Array<String>,
        selectedNotes: Array<String>,
        origin: String?
    ) {
        val action = DrawerNavigationDirections.goToSharePeopleSelection(
            argsAuthentifiantUIDs = selectedPasswords,
            argsSecureNotesUIDs = selectedNotes,
            from = origin
        )
        navigate(action)
    }

    override fun goToShareUsersForItems(uid: String) {
        val action = DrawerNavigationDirections.goToShareUsersForItems(
            argsItemUid = uid,
        )
        navigate(action)
    }

    override fun goToPeopleSelectionFromNewShare(
        selectedPasswords: Array<String>,
        selectedNotes: Array<String>
    ) {
        val action = SharingItemSelectionTabFragmentDirections.newShareToSharePeopleSelection(
            argsAuthentifiantUIDs = selectedPasswords,
            argsSecureNotesUIDs = selectedNotes,
            from = null
        )
        navigate(action)
    }

    override fun goToPasswordSharingFromActionCenter(needsRefresh: Boolean) {
        val action: NavDirections = when (navigationController?.currentDestination?.id) {
            R.id.nav_notif_center ->
                NotificationCenterFragmentDirections.actionCenterToSharingCenter(
                    needsRefresh = needsRefresh
                )

            R.id.nav_action_center_section_details ->
                NotificationCenterSectionDetailsFragmentDirections.actionCenterSectionToSharingCenter(
                    needsRefresh = needsRefresh
                )

            else -> return
        }
        navigate(action)
    }

    override fun goToPasswordSharingFromPeopleSelection() {
        val action =
            SharingNewSharePeopleFragmentDirections.goToSharingCenter(needsRefresh = true)
        navigate(action)
    }

    override fun goToItemsForUserFromPasswordSharing(memberEmail: String) {
        val action =
            SharingCenterFragmentDirections.sharingCenterToShareItemsForUsers(argsMemberLogin = memberEmail)
        navigate(action)
    }

    override fun goToUserGroupFromPasswordSharing(groupId: String, groupName: String) {
        val action = SharingCenterFragmentDirections.sharingCenterToShareUserGroups(
            argsGroupId = groupId,
            argsGroupName = groupName
        )
        navigate(action)
    }

    override fun goToManageDevicesFromSettings() {
        val action = SettingsFragmentDirections.settingsToManageDevices()
        navigate(action)
    }

    override fun goToAutofillPauseAndLinkedFromSettings() {
        val action =
            SettingsFragmentDirections.settingsToAutofillPausedAndLinked()
        navigate(action)
    }

    override fun goToDashlaneLabs() {
        val action = SettingsFragmentDirections.settingsToDashlaneLabs()
        navigate(action)
    }

    override fun goToGuidedWebCsvExport() {
        val action = SettingsFragmentDirections.settingsToGuidedWebCsvExport()
        navigate(action)
    }

    override fun goToAccountStatus() {
        val action = SettingsFragmentDirections.settingsToAccountStatus()
        navigate(action)
    }

    override fun goToChangeContactEmail() {
        val action = AccountStatusFragmentDirections.changeContactEmail()
        navigate(action)
    }

    override fun goToPaywall(type: PaywallIntroType) {
        if (navigationController == null) {
            val args = PaywallActivityArgs(paywallIntroType = type).toBundle()
            activity.startActivity(
                Intent(activity, PaywallActivity::class.java).apply {
                    putExtras(args)
                }
            )
            return
        }
        val action =
            DrawerNavigationDirections.goToPaywall(paywallIntroType = type)
        navigate(action)
    }

    override fun goToGuidedPasswordChange(
        itemId: String,
        domain: String,
        username: String?
    ) {
        val action =
            DrawerNavigationDirections.goToGuidedPasswordChange(
                websiteDomain = domain,
                itemId = itemId,
                username = username
            )
        navigate(action)
    }

    override fun goToCsvImportIntro() {
        val action = DrawerNavigationDirections.goToCsvImportIntro(fromCompetitor = false)
        navigate(action)
    }

    override fun goToM2wImportIntro() {
        val action = DrawerNavigationDirections.goToM2wIntro()
        navigate(action)
    }

    override fun goToCompetitorImportIntro() {
        val action = DrawerNavigationDirections.goToCsvImportIntro(fromCompetitor = true)
        navigate(action)
    }

    override fun goToGuidedPasswordChangeFromCredential(
        itemId: String,
        domain: String,
        username: String?
    ) {
        val args =
            OnboardingGuidedPasswordChangeActivityArgs(
                websiteDomain = domain,
                itemId = itemId,
                username = username
            ).toBundle()
        activity.startActivity(
            Intent(activity, OnboardingGuidedPasswordChangeActivity::class.java).apply {
                putExtras(args)
            }
        )
    }

    override fun goToCurrentPlan() {
        val action = DrawerNavigationDirections.goToCurrentPlan()
        navigate(action)
    }

    override fun navigateUp() =
        navigationController?.navigateUp().apply { DeviceUtils.hideKeyboard(activity) } ?: false

    override fun popBackStack() {
        waitForNavControllerIfNeeded {
            if (!navigationController!!.popBackStack()) {
                activity.finish()
            }
        }
    }

    override fun handleDeepLink(intent: Intent) {
        intent.data ?: intent.extras ?: return
        applicationCoroutineScope.launch(Dispatchers.Main.immediate) {
            if (lockManager.isLocked) {
                val lockEvent = lockManager.showAndWaitLockActivityForReason(
                    context = activity,
                    reason = LockEvent.Unlock.Reason.WithCode(UNLOCK_EVENT_CODE),
                    lockPrompt = LockPrompt.Regular,
                )
                if (lockEvent is LockEvent.Unlock) return@launch
            }
            waitForNavControllerIfNeeded {
                deepLinkFound = if (!navDeepLinkHelper.overrideDeepLink(intent)) {
                    navigationController!!.handleDeepLink(intent)
                } else {
                    true
                }
            }
        }
    }

    override fun addOnDestinationChangedListener(listener: OnDestinationChangedListener) {
        navigationController?.addOnDestinationChangedListener(listener)
    }

    override fun removeOnDestinationChangedListener(listener: OnDestinationChangedListener) {
        navigationController?.removeOnDestinationChangedListener(listener)
    }

    override fun goToManageDashlaneNotificationsSystem() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, activity.application.packageName)
        activity.startActivity(intent)
    }

    override fun goToLinkedWebsites(
        itemId: String,
        fromViewOnly: Boolean,
        addNew: Boolean,
        temporaryWebsites: List<String>,
        temporaryApps: List<String>?,
        urlDomain: String?
    ) {
        navigate(
            ItemEditFragmentDirections.actionNavNewItemEditToLinkedServicesFragment(
                uid = itemId,
                fromViewOnly = fromViewOnly,
                addNew = addNew,
                temporaryWebsites = temporaryWebsites.toTypedArray(),
                temporaryApps = temporaryApps?.toTypedArray(),
                url = urlDomain
            )
        )
    }

    override fun goToSecretTransfer(settingsId: String?) {
        val action = DrawerNavigationDirections.goToSecretTransfer(id = settingsId)
        navigate(action)
    }

    override fun goToAccountRecoveryKey(
        settingsId: String?,
        showIntro: Boolean,
        userCanExitFlow: Boolean
    ) {
        val action =
            DrawerNavigationDirections.goToAccountRecoveryKey(
                id = settingsId,
                showIntro = showIntro,
                userCanExitFlow = userCanExitFlow
            )
        navigate(action)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        super.onActivityCreated(activity, savedInstanceState)
        currentActivity = WeakReference(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)
        currentActivity = WeakReference(activity)
    }

    override fun logoutAndCallLoginScreen(context: Context?) {
        val intent = if (context is Activity) {
            context.intent
        } else {
            null
        }
        logoutAndCallLoginScreen(context ?: activity, intent)
    }

    override fun goToWebView(url: String) {
        activity.launchUrl(url)
    }

    @Suppress("kotlin:S6311")
    private fun navigate(
        action: NavDirections,
        keepKeyboardOpen: Boolean = false,
        closeMainActivity: Boolean = false
    ) {
        val mainActivity = activity
        (mainActivity as? FragmentActivity)?.hideDialogs()
        waitForNavControllerIfNeeded {
            val current = currentDestination
            val controller = navigationController!!
            val navOptions =
                if (current != null && NavigationUtils.matchDestination(
                        controller.currentBackStackEntry,
                        action
                    )
                ) {
                    
                    NavOptions.Builder().setPopUpTo(current.id, true).build()
                } else {
                    null
                }
            controller.navigate(action, navOptions)
            if (!keepKeyboardOpen) {
                applicationCoroutineScope.launch(Dispatchers.Main.immediate) { closeKeyboard() }
            }
            if (closeMainActivity) mainActivity.finish()
        }
    }

    private suspend fun closeKeyboard() {
        try {
            
            delay(150L)
            DeviceUtils.hideKeyboard(activity)
        } catch (e: IllegalStateException) {
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun waitForNavControllerIfNeeded(block: () -> Unit) {
        if (navigationController != null) {
            block.invoke()
        } else {
            applicationCoroutineScope.launch(Dispatchers.Main.immediate) {
                while (navigationController == null) delay(1)
                
                
                
                
                
                try {
                    block.invoke()
                } catch (e: IllegalArgumentException) {
                    
                }
            }
        }
    }

    private fun logoutAndCallLoginScreen(context: Context, originalIntent: Intent?) {
        val appContext = context.applicationContext
        
        
        
        
        
        val contextIsLogin = context is LoginActivity
        val baseContext = context.getBaseActivity() 
        if (baseContext is Activity && !contextIsLogin) {
            baseContext.finish()
        }

        applicationCoroutineScope.launch(mainDispatcher) {
            sessionManager.session?.let { sessionManager.destroySession(it, true) }
            val loginIntent = DashlaneIntent.newInstance(appContext, LoginActivity::class.java)
            loginIntent.putExtra(LockSetting.EXTRA_REDIRECT_TO_HOME, true)
            if (contextIsLogin) loginIntent.clearTop() else loginIntent.clearTask()
            if (originalIntent != null && originalIntent.hasExtra(NavigationConstants.LOGIN_CALLED_FROM_INAPP_LOGIN)) {
                loginIntent.putExtra(
                    NavigationConstants.LOGIN_CALLED_FROM_INAPP_LOGIN,
                    originalIntent.getBooleanExtra(NavigationConstants.LOGIN_CALLED_FROM_INAPP_LOGIN, false)
                )
            }
            context.startActivity(loginIntent)
        }
    }

    override fun setupActionBar(topLevelDestinations: Set<Int>) {
        navigationController!!.setupActionBar(activity, topLevelDestinations)
        menuIconDestinationChangedListener?.topLevelDestinations = topLevelDestinations
    }

    override fun showAttachments(
        id: String,
        xmlObjectName: String,
        attachments: String?,
    ) {
        val attachmentListIntent = Intent(activity, AttachmentListActivity::class.java).apply {
            putExtra(AttachmentListActivity.ITEM_ATTACHMENTS, attachments)
            putExtra(AttachmentListActivity.ITEM_ID, id)
            putExtra(AttachmentListActivity.ITEM_TYPE, xmlObjectName)
        }
        activity.startActivityForResult(
            attachmentListIntent,
            AttachmentListActivity.REQUEST_CODE_ATTACHMENT_LIST
        )
    }

    override fun showNewSharing(
        id: String,
        fromAuthentifiant: Boolean,
        fromSecureNote: Boolean
    ) {
        (activity.getBaseActivity() as? FragmentActivity)?.let {
            restrictionNotificator.runOrNotifyTeamRestriction(
                activity = it,
                feature = Feature.SHARING_DISABLED
            ) {
                val uri = NavigationUriBuilder().apply {
                    if (fromAuthentifiant) {
                        host(NavigationHelper.Destination.MainPath.PASSWORDS)
                        origin(SharingNewSharePeopleFragment.FROM_ITEM_VIEW)
                    } else if (fromSecureNote) {
                        host(NavigationHelper.Destination.MainPath.NOTES)
                        origin(SharingNewSharePeopleFragment.FROM_ITEM_VIEW)
                    }
                    appendPath(id)
                    appendPath(NavigationHelper.Destination.SecondaryPath.Items.SHARE)
                }.build()
                DashlaneWrapperActivity.startActivityForResult(
                    NEW_SHARE_REQUEST_CODE,
                    activity,
                    uri,
                    Bundle()
                )
            }
        }
    }

    override fun openWebsite(url: String?, packageNames: Set<String>) {
        LoginOpener(activity).show(url = url, packageNames = packageNames, listener = null)
    }

    private fun isNewItemEditEnabled(type: String): Boolean {
        val newItemEditTypes = listOfNotNull(
            SyncObjectXmlName.AUTHENTIFIANT,
            SyncObjectXmlName.SECURE_NOTE.takeIf { userFeaturesChecker.has(NEW_ITEM_EDIT_SECURE_NOTES) },
            SyncObjectXmlName.SECRET
        )

        return newItemEditTypes.contains(type)
    }

    override fun goToBiometricOnboarding(context: Context) {
        if (biometricRecovery.isFeatureAvailable()) {
            val successIntent = MasterPasswordResetIntroActivity.newIntent(context)
            context.startActivity(HardwareAuthActivationActivity.newIntent(context, successIntent).singleTop())
        } else {
            context.startActivity(DashlaneIntent.newInstance(context, OnboardingHardwareAuthActivity::class.java))
        }
    }

    companion object {
        private const val UNLOCK_EVENT_CODE = 178
        const val NEW_SHARE_REQUEST_CODE = 6243
    }
}
