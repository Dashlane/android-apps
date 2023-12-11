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
import com.dashlane.authenticator.AuthenticatorDashboardFragmentDirections
import com.dashlane.authenticator.AuthenticatorSuggestionsFragmentDirections
import com.dashlane.authenticator.Otp
import com.dashlane.collections.details.CollectionDetailsFragmentDirections
import com.dashlane.collections.list.CollectionsListFragmentDirections
import com.dashlane.collections.sharing.CollectionNewShareActivity
import com.dashlane.collections.sharing.CollectionNewShareActivity.Companion.SHARE_COLLECTION
import com.dashlane.collections.sharing.CollectionNewShareActivityArgs
import com.dashlane.events.AppEvents
import com.dashlane.events.clearLastEvent
import com.dashlane.followupnotification.discovery.FollowUpNotificationDiscoveryActivity
import com.dashlane.followupnotification.discovery.FollowUpNotificationDiscoveryActivityArgs
import com.dashlane.guidedpasswordchange.OnboardingGuidedPasswordChangeActivity
import com.dashlane.guidedpasswordchange.OnboardingGuidedPasswordChangeActivityArgs
import com.dashlane.help.HelpCenterLink
import com.dashlane.help.newIntent
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.item.ItemEditViewActivity
import com.dashlane.item.collection.CollectionSelectorActivity
import com.dashlane.item.delete.DeleteVaultItemFragment
import com.dashlane.item.delete.DeleteVaultItemFragmentArgs
import com.dashlane.item.linkedwebsites.LinkedServicesActivity
import com.dashlane.lock.LockHelper
import com.dashlane.lock.UnlockEvent
import com.dashlane.login.LoginActivity
import com.dashlane.login.lock.unlockItemIfNeeded
import com.dashlane.navigation.NavControllerUtils.TOP_LEVEL_DESTINATIONS
import com.dashlane.navigation.NavControllerUtils.setup
import com.dashlane.navigation.NavControllerUtils.setupActionBar
import com.dashlane.notificationcenter.NotificationCenterFragmentDirections
import com.dashlane.notificationcenter.details.NotificationCenterSectionDetailsFragmentDirections
import com.dashlane.premium.offer.list.view.OffersActivity
import com.dashlane.premium.offer.list.view.OffersActivityArgs
import com.dashlane.premium.paywall.common.PaywallActivity
import com.dashlane.premium.paywall.common.PaywallActivityArgs
import com.dashlane.premium.paywall.common.PaywallIntroType
import com.dashlane.security.DashlaneIntent
import com.dashlane.security.darkwebmonitoring.DarkWebMonitoringFragmentDirections
import com.dashlane.security.identitydashboard.IdentityDashboardFragmentDirections
import com.dashlane.security.identitydashboard.breach.BreachWrapper
import com.dashlane.security.identitydashboard.password.PasswordAnalysisFragmentDirections
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.LockRepository
import com.dashlane.session.repository.getLockManager
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.ui.AbstractActivityLifecycleListener
import com.dashlane.ui.activities.fragments.checklist.ChecklistHelper
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.ui.screens.activities.onboarding.inapplogin.OnboardingType
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingCenterFragmentDirections
import com.dashlane.ui.screens.fragments.userdata.sharing.itemselection.SharingItemSelectionTabFragmentDirections
import com.dashlane.ui.screens.settings.SettingsFragmentDirections
import com.dashlane.ui.screens.sharing.SharingNewSharePeopleFragmentDirections
import com.dashlane.util.DeviceUtils
import com.dashlane.util.clearTask
import com.dashlane.util.clearTop
import com.dashlane.util.getBaseActivity
import com.dashlane.util.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.util.inject.qualifiers.MainCoroutineDispatcher
import com.dashlane.util.launchUrl
import com.dashlane.util.logPageView
import com.dashlane.util.safelyStartBrowserActivity
import com.dashlane.util.startActivityForResult
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.util.userfeatures.UserFeaturesChecker.Capability
import com.dashlane.util.userfeatures.canShowVpn
import com.dashlane.util.userfeatures.canUpgradeToGetVpn
import com.dashlane.vpn.thirdparty.VpnThirdPartyFragmentDirections
import com.dashlane.xml.domain.SyncObjectType.AUTHENTIFIANT
import java.io.Serializable
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
    private val lockRepository: LockRepository,
    private val mainDataAccessor: MainDataAccessor,
    private val checklistHelper: ChecklistHelper,
    private val appEvents: AppEvents,
    @ApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope,
    @MainCoroutineDispatcher
    private val mainDispatcher: CoroutineDispatcher
) : Navigator, AbstractActivityLifecycleListener() {
    private val dataQuery: GenericDataQuery
        get() = mainDataAccessor.getGenericDataQuery()

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
            when (userFeaturesChecker.has(Capability.DATA_LEAK)) {
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

            userFeaturesChecker.has(Capability.VPN_ACCESS) -> {
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

    override fun goToGetStartedFromAuthenticator() {
        navigate(AuthenticatorDashboardFragmentDirections.goToGetStartedFromAuthenticatorDashboard())
    }

    override fun goToGetStartedFromAuthenticatorSuggestions() {
        navigate(AuthenticatorSuggestionsFragmentDirections.goToGetStartedFromAuthenticatorSuggestions())
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

    override fun goToQuickActions(itemId: String, itemListContext: Parcelable, originPage: AnyPage?) {
        navigate(
            DrawerNavigationDirections.goToQuickActions(
                itemId = itemId,
                itemListContext = itemListContext as ItemListContext,
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
        if (breachWrapper.publicBreach.isDarkWebBreach() && !userFeaturesChecker.has(Capability.DATA_LEAK)) {
            goToPaywall(type = PaywallIntroType.DARK_WEB_MONITORING.toString())
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

    override fun goToItem(uid: String, type: String) {
        val lockManager =
            lockRepository.getLockManager(sessionManager) ?: return 
        applicationCoroutineScope.launch(Dispatchers.Main.immediate) {
            
            if (!lockManager.unlockItemIfNeeded(activity, dataQuery, uid, type)) return@launch
            val action = DrawerNavigationDirections.goToItemEdit(
                uid = uid,
                dataType = type
            )
            navigate(action)
            appEvents.clearLastEvent<UnlockEvent>()
        }
    }

    override fun goToCreateItem(type: String) {
        navigate(DrawerNavigationDirections.goToItemEdit(dataType = type))
    }

    override fun goToCreateAuthentifiant(
        url: String,
        requestCode: Int?,
        successIntent: Intent?,
        otp: Parcelable?
    ) {
        val action = DrawerNavigationDirections.goToItemEdit(
            dataType = AUTHENTIFIANT.xmlObjectName,
            url = url,
            successIntent = successIntent,
            otp = otp as? Otp
        )
        if (requestCode == null) {
            navigate(action)
        } else {
            activity.startActivityForResult(
                Intent(activity, ItemEditViewActivity::class.java).apply {
                    putExtras(action.arguments)
                },
                requestCode
            )
        }
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
        temporaryCollections: List<String>,
        spaceId: String
    ) {
        val action = DrawerNavigationDirections.itemEditViewToCollectionSelector(
            temporaryCollections = temporaryCollections.toTypedArray(),
            fromView = fromViewOnly,
            spaceId = spaceId
        )
        
        
        activity.startActivityForResult<CollectionSelectorActivity>(CollectionSelectorActivity.SHOW_COLLECTION_SELECTOR) {
            putExtras(action.arguments)
        }
        if (!fromViewOnly) {
            activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.no_animation)
        }
    }

    override fun goToCredentialAddStep1(
        expandImportOptions: Boolean,
        successIntent: Intent?
    ) {
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
        val action = CollectionsListFragmentDirections.collectionsListToCollectionEdit(null)
        navigate(action)
    }

    override fun goToCollectionEditFromCollectionsList(collectionId: String) {
        val action = CollectionsListFragmentDirections.collectionsListToCollectionEdit(collectionId)
        navigate(action)
    }

    override fun goToCollectionShareFromCollectionList(collectionId: String) {
        val args = CollectionNewShareActivityArgs(collectionId).toBundle()
        activity.startActivityForResult<CollectionNewShareActivity>(SHARE_COLLECTION) {
            putExtras(args)
        }
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

    override fun goToCollectionEditFromCollectionDetail(collectionId: String) {
        val action =
            CollectionDetailsFragmentDirections.collectionDetailsToCollectionEdit(collectionId)
        navigate(action)
    }

    override fun goToCollectionShareFromCollectionDetail(collectionId: String) {
        val args = CollectionNewShareActivityArgs(collectionId).toBundle()
        activity.startActivityForResult<CollectionNewShareActivity>(SHARE_COLLECTION) {
            putExtras(args)
        }
    }

    override fun goToInAppLoginIntro() {
        val action = DrawerNavigationDirections.goToInAppLoginIntro()
        navigate(action)
    }

    override fun goToInAppLogin(onBoardingType: Serializable?) {
        val type = onBoardingType as? OnboardingType ?: OnboardingType.AUTO_FILL_API
        val action =
            DrawerNavigationDirections.goToInAppLogin(extraOnboardingType = type)
        navigate(action)
    }

    override fun goToSearch(query: String?) {
        val action = DrawerNavigationDirections.goToSearch(argsQuery = query)
        navigate(action, keepKeyboardOpen = true)
    }

    override fun goToCredentialFromPasswordAnalysis(uid: String) {
        val action = PasswordAnalysisFragmentDirections.passwordAnalysisToItemEdit(
            uid = uid,
            dataType = AUTHENTIFIANT.xmlObjectName
        )
        navigate(action)
    }

    override fun goToPasswordAnalysisFromBreach(breachId: String) {
        
        val action = DrawerNavigationDirections.goToPasswordAnalysis(
            breachFocus = breachId,
        )
        navigate(action)
    }

    override fun goToPasswordAnalysisFromIdentityDashboard(tab: String?) {
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

    override fun goToPaywall(type: String) {
        val introType = PaywallIntroType.values().firstOrNull { it.name == type } ?: return
        if (navigationController == null) {
            val args = PaywallActivityArgs(paywallIntroType = introType).toBundle()
            activity.startActivity(
                Intent(activity, PaywallActivity::class.java).apply {
                    putExtras(args)
                }
            )
            return
        }
        val action =
            DrawerNavigationDirections.goToPaywall(paywallIntroType = introType)
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
        username: String?,
        requestCode: Int
    ) {
        val args =
            OnboardingGuidedPasswordChangeActivityArgs(
                websiteDomain = domain,
                itemId = itemId,
                username = username
            ).toBundle()
        activity.startActivityForResult(
            Intent(activity, OnboardingGuidedPasswordChangeActivity::class.java).apply {
                putExtras(args)
            },
            requestCode
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

    @OptIn(DelicateCoroutinesApi::class)
    override fun handleDeepLink(intent: Intent) {
        intent.data ?: intent.extras ?: return
        applicationCoroutineScope.launch(Dispatchers.Main.immediate) {
            val lockManager = lockRepository.getLockManager(sessionManager)
            if (lockManager?.isLocked == true) {
                val event = lockManager.showAndWaitLockActivityForReason(
                    activity,
                    UnlockEvent.Reason.WithCode(UNLOCK_EVENT_CODE),
                    LockHelper.PROMPT_LOCK_REGULAR,
                    null
                )
                if (event?.isSuccess() != true) return@launch
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
        activity.startActivityForResult<LinkedServicesActivity>(
            LinkedServicesActivity.SHOW_LINKED_SERVICES
        ) {
            putExtra(LinkedServicesActivity.PARAM_ITEM_ID, itemId)
            putExtra(LinkedServicesActivity.PARAM_FROM_VIEW_ONLY, fromViewOnly)
            putExtra(LinkedServicesActivity.PARAM_ADD_NEW, addNew)
            putExtra(LinkedServicesActivity.PARAM_TEMPORARY_WEBSITES, temporaryWebsites.toTypedArray())
            putExtra(LinkedServicesActivity.PARAM_TEMPORARY_APPS, temporaryApps?.toTypedArray())
            putExtra(LinkedServicesActivity.PARAM_URL_DOMAIN, urlDomain)
        }
        if (!fromViewOnly) {
            activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.no_animation)
        }
    }

    override fun goToSecretTransfer(settingsId: String?) {
        val action = DrawerNavigationDirections.goToSecretTransfer(id = settingsId)
        navigate(action)
    }

    override fun goToAccountRecoveryKey(settingsId: String?) {
        val action =
            DrawerNavigationDirections.goToAccountRecoveryKey(id = settingsId)
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

    override fun logoutAndCallLoginScreen(context: Context?, allowSkipEmail: Boolean) {
        val intent = if (context is Activity) {
            context.intent
        } else {
            null
        }
        logoutAndCallLoginScreen(context ?: activity, intent, allowSkipEmail)
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
        (mainActivity as? FragmentActivity)?.let { NavigationUtils.hideDialogs(it) }
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

    private fun logoutAndCallLoginScreen(context: Context, originalIntent: Intent?, allowSkipEmail: Boolean) {
        val appContext = context.applicationContext
        
        
        
        
        
        val contextIsLogin = context is LoginActivity
        val baseContext = context.getBaseActivity() 
        if (baseContext is Activity && !contextIsLogin) {
            baseContext.finish()
        }

        appEvents.clearLastEvent<UnlockEvent>()
        applicationCoroutineScope.launch(mainDispatcher) {
            sessionManager.session?.let { sessionManager.destroySession(it, true) }
            val loginIntent = DashlaneIntent.newInstance(appContext, LoginActivity::class.java)
            if (contextIsLogin) loginIntent.clearTop() else loginIntent.clearTask()
            if (originalIntent != null && originalIntent.hasExtra(NavigationConstants.LOGIN_CALLED_FROM_INAPP_LOGIN)) {
                loginIntent.putExtra(
                    NavigationConstants.LOGIN_CALLED_FROM_INAPP_LOGIN,
                    originalIntent.getBooleanExtra(NavigationConstants.LOGIN_CALLED_FROM_INAPP_LOGIN, false)
                )
            }

            if (allowSkipEmail) {
                loginIntent.putExtra(LoginActivity.ALLOW_SKIP_EMAIL, true)
            }
            context.startActivity(loginIntent)
        }
    }

    override fun setupActionBar(topLevelDestinations: Set<Int>) {
        navigationController!!.setupActionBar(activity, topLevelDestinations)
        menuIconDestinationChangedListener?.topLevelDestinations = topLevelDestinations
    }

    companion object {
        private const val UNLOCK_EVENT_CODE = 178
    }
}
