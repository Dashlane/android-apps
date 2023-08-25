package com.dashlane.navigation

import android.app.Activity
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
import com.dashlane.autofill.api.util.AutofillNavigationService
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
import com.dashlane.useractivity.log.usage.UsageLogCode80
import com.dashlane.util.DeviceUtils
import com.dashlane.util.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.util.launchUrl
import com.dashlane.util.logPageView
import com.dashlane.util.safelyStartBrowserActivity
import com.dashlane.util.startActivityForResult
import com.dashlane.util.usagelogs.ViewLogger
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.util.userfeatures.UserFeaturesChecker.Capability
import com.dashlane.util.userfeatures.canShowVpn
import com.dashlane.util.userfeatures.canUpgradeToGetVpn
import com.dashlane.vpn.thirdparty.VpnThirdPartyFragmentDirections
import com.dashlane.xml.domain.SyncObjectType.AUTHENTIFIANT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.Serializable
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

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
    private val viewLoggerDestinationChangedListener: ViewLoggerDestinationChangedListener,
    private val viewLogger: ViewLogger
) : Navigator, AbstractActivityLifecycleListener() {
    private val dataQuery: GenericDataQuery
        get() = mainDataAccessor.getGenericDataQuery()

    override val currentDestination: NavDestination?
        get() = navigationController?.currentDestination
    private var menuIconDestinationChangedListener: MenuIconDestinationChangedListener? = null
    private var deepLinkFound: Boolean = false

    private var currentActivity: WeakReference<Activity>? = null
        set(value) {
            currentActivity?.get()?.apply {
                
                val controller = navigationController ?: return@apply
                controller.removeOnDestinationChangedListener(viewLoggerDestinationChangedListener)
                menuIconDestinationChangedListener?.let {
                    controller.removeOnDestinationChangedListener(it)
                }
            }
            field = value
            navigationController?.apply {
                val topLevelDestinations = menuIconDestinationChangedListener?.topLevelDestinations
                    ?: TOP_LEVEL_DESTINATIONS
                setup(activity, deepLinkFound, checklistHelper, topLevelDestinations)
                addOnDestinationChangedListener(viewLoggerDestinationChangedListener)

                menuIconDestinationChangedListener = MenuIconDestinationChangedListener(
                    activity,
                    this@NavigatorImpl,
                    topLevelDestinations
                )
                addOnDestinationChangedListener(menuIconDestinationChangedListener!!)
            }
        }

    private val activity: Activity
        get() = currentActivity!!.get()!!
    private val navigationController
        get() = try {
            activity.findNavController(R.id.nav_host_fragment)
        } catch (e: IllegalArgumentException) {
            
            null
        }
    private val navDeepLinkHelper = NavDeepLinkHelper(navigator = this)

    override fun goToHome(origin: String?, filter: String?) {
        val action =
            DrawerNavigationDirections.goToHome(filter = filter, origin = origin.originOrDefault())
        navigate(action)
    }

    override fun goToActionCenter(origin: String?) {
        val action =
            DrawerNavigationDirections.goToNotificationCenter(origin = origin.originOrDefault())
        navigate(action)
    }

    override fun goToPasswordGenerator(origin: String?) {
        val action =
            DrawerNavigationDirections.goToPasswordGenerator(origin = origin.originOrDefault())
        navigate(action)
    }

    override fun goToPasswordSharing(origin: String?) {
        val action = DrawerNavigationDirections.goToSharingCenter(origin = origin.originOrDefault())
        navigate(action)
    }

    override fun goToIdentityDashboard(origin: String?) {
        val action =
            DrawerNavigationDirections.goToIdentityDashboard(origin = origin.originOrDefault())
        navigate(action)
    }

    override fun goToDarkWebMonitoring(origin: String?) {
        val action =
            when (userFeaturesChecker.has(Capability.DATA_LEAK)) {
                true -> DrawerNavigationDirections.goToDarkWebMonitoring(origin = origin.originOrDefault())
                else -> DrawerNavigationDirections.goToPaywall(
                    origin = origin,
                    paywallIntroType = PaywallIntroType.DARK_WEB_MONITORING
                )
            }
        navigate(action)
    }

    override fun goToVpn(origin: String?) {
        val action = when {
            !userFeaturesChecker.canShowVpn() -> {
                
                return
            }

            userFeaturesChecker.has(Capability.VPN_ACCESS) -> {
                DrawerNavigationDirections.goToThirdPartyVpn()
            }

            userFeaturesChecker.canUpgradeToGetVpn() -> {
                DrawerNavigationDirections.goToPaywall(
                    origin = origin,
                    paywallIntroType = PaywallIntroType.VPN
                )
            }

            else -> {
                DrawerNavigationDirections.goToVpnB2bIntro()
            }
        }
        navigate(action)
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

    override fun goToSettings(settingsId: String?, origin: String?) {
        val action = DrawerNavigationDirections.goToSettings(
            id = settingsId,
            origin = origin.originOrDefault()
        )
        navigate(action)
    }

    override fun goToHelpCenter(origin: String?) {
        activity.logPageView(AnyPage.HELP)
        val intent = HelpCenterLink.Base.newIntent(activity, viewLogger, true)
        activity.safelyStartBrowserActivity(intent)
    }

    override fun goToPersonalPlanOrHome(origin: String?) {
        if (checklistHelper.shouldDisplayChecklist()) {
            goToPersonalPlan(origin = origin)
        } else {
            goToHome(origin = origin)
        }
    }

    private fun goToPersonalPlan(origin: String?) {
        val action = DrawerNavigationDirections.goToPersonalPlan(origin = origin.originOrDefault())
        navigate(action)
    }

    override fun goToOffers(origin: String?, offerType: String?) {
        if (navigationController == null) {
            
            val extras = OffersActivityArgs(offerType = offerType, origin = origin).toBundle()
            activity.startActivity(
                Intent(activity, OffersActivity::class.java).apply {
                putExtras(extras)
            }
            )
            return
        }
        val action = DrawerNavigationDirections.goToOffers(offerType = offerType, origin = origin)
        
        navigate(
            action,
            closeMainActivity = AutofillNavigationService.ORIGIN_PASSWORD_LIMIT == origin
        )
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

    override fun goToBreachAlertDetail(breachWrapper: Parcelable, origin: String?) {
        val breach = breachWrapper as BreachWrapper
        if (breachWrapper.publicBreach.isDarkWebBreach() && !userFeaturesChecker.has(Capability.DATA_LEAK)) {
            goToPaywall(type = PaywallIntroType.DARK_WEB_MONITORING.toString(), origin = origin)
            return
        }
        val action = when (navigationController?.currentDestination?.id) {
            R.id.nav_dark_web_monitoring ->
                DarkWebMonitoringFragmentDirections.darkWebMonitoringToBreachAlertDetail(
                    breach = breach,
                    origin = origin.originOrDefault()
                )

            R.id.nav_notif_center ->
                NotificationCenterFragmentDirections.actionCenterToBreachAlertDetail(
                    breach = breach,
                    origin = origin.originOrDefault()
                )

            R.id.nav_action_center_section_details ->
                NotificationCenterSectionDetailsFragmentDirections.actionCenterSectionToBreachAlertDetail(
                    breach = breach,
                    origin = origin.originOrDefault()
                )

            else -> DrawerNavigationDirections.goToBreachAlertDetail(breach = breach)
        }
        navigate(action)
    }

    @OptIn(DelicateCoroutinesApi::class)
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
        sender: String?,
        url: String,
        requestCode: Int?,
        successIntent: Intent?,
        otp: Parcelable?
    ) {
        val action = DrawerNavigationDirections.goToItemEdit(
            dataType = AUTHENTIFIANT.xmlObjectName,
            url = url,
            sender = sender,
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
        sender: String?,
        expandImportOptions: Boolean,
        successIntent: Intent?
    ) {
        val action = DrawerNavigationDirections.goToCredentialAddStep1(
            expandImportOptions = expandImportOptions,
            paramSuccessIntent = successIntent,
            paramSender = sender
        )
        navigate(action)
    }

    override fun goToInAppLoginIntro(origin: String) {
        val action = DrawerNavigationDirections.goToInAppLoginIntro(origin = origin)
        navigate(action)
    }

    override fun goToInAppLogin(origin: String?, onBoardingType: Serializable?) {
        val type = onBoardingType as? OnboardingType ?: OnboardingType.AUTO_FILL_API
        val action =
            DrawerNavigationDirections.goToInAppLogin(origin = origin, extraOnboardingType = type)
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

    override fun goToPasswordAnalysisFromBreach(breachId: String, origin: String?) {
        
        val action = DrawerNavigationDirections.goToPasswordAnalysis(
            breachFocus = breachId,
            origin = origin.originOrDefault()
        )
        navigate(action)
    }

    override fun goToPasswordAnalysisFromIdentityDashboard(origin: String?, tab: String?) {
        val action =
            IdentityDashboardFragmentDirections.identityDashboardToPasswordAnalysis(
                tab = tab,
                origin = origin.originOrDefault()
            )
        navigate(action)
    }

    override fun goToNewShare(origin: String) {
        val action = DrawerNavigationDirections.goToNewShare(argsUsageLogFrom = origin)
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
            from = UsageLogCode80.From.SHARING_CENTER.code
        )
        navigate(action)
    }

    override fun goToPasswordSharingFromActionCenter(origin: String?, needsRefresh: Boolean) {
        val action: NavDirections = when (navigationController?.currentDestination?.id) {
            R.id.nav_notif_center ->
                NotificationCenterFragmentDirections.actionCenterToSharingCenter(
                    needsRefresh = needsRefresh,
                    origin = origin.originOrDefault()
                )

            R.id.nav_action_center_section_details ->
                NotificationCenterSectionDetailsFragmentDirections.actionCenterSectionToSharingCenter(
                    needsRefresh = needsRefresh,
                    origin = origin.originOrDefault()
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

    override fun goToAutofillPauseAndLinkedFromSettings(origin: String?) {
        val action =
            SettingsFragmentDirections.settingsToAutofillPausedAndLinked(origin = origin.originOrDefault())
        navigate(action)
    }

    override fun goToDashlaneLabs() {
        val action = SettingsFragmentDirections.settingsToDashlaneLabs()
        navigate(action)
    }

    override fun goToPaywall(type: String, origin: String?) {
        val introType = PaywallIntroType.values().firstOrNull { it.name == type } ?: return
        if (navigationController == null) {
            val args = PaywallActivityArgs(origin = origin, paywallIntroType = introType).toBundle()
            activity.startActivity(
                Intent(activity, PaywallActivity::class.java).apply {
                putExtras(args)
            }
            )
            return
        }
        val action =
            DrawerNavigationDirections.goToPaywall(origin = origin, paywallIntroType = introType)
        navigate(action)
    }

    override fun goToGuidedPasswordChange(
        itemId: String,
        domain: String,
        username: String?,
        origin: String?
    ) {
        val action =
            DrawerNavigationDirections.goToGuidedPasswordChange(
                websiteDomain = domain,
                itemId = itemId,
                username = username,
                origin = origin
            )
        navigate(action)
    }

    override fun goToCsvImportIntro() {
        val action = DrawerNavigationDirections.goToCsvImportIntro(fromCompetitor = false)
        navigate(action)
    }

    override fun goToChromeImportIntro(origin: String) {
        val action = DrawerNavigationDirections.goToChromeImportIntro(origin = origin)
        navigate(action)
    }

    override fun goToM2wImportIntro(origin: String) {
        val action = DrawerNavigationDirections.goToM2wIntro(origin = origin)
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
                username = username,
                origin = null
            ).toBundle()
        activity.startActivityForResult(
            Intent(activity, OnboardingGuidedPasswordChangeActivity::class.java).apply {
                putExtras(args)
            },
            requestCode
        )
    }

    override fun goToCurrentPlan(origin: String) {
        val action = DrawerNavigationDirections.goToCurrentPlan(origin = origin)
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

    override fun goToSecretTransfer(settingsId: String?, origin: String?) {
        val action = DrawerNavigationDirections.goToSecretTransfer(id = settingsId, origin = origin.originOrDefault())
        navigate(action)
    }

    override fun goToAccountRecoveryKey(settingsId: String?, origin: String?) {
        val action =
            DrawerNavigationDirections.goToAccountRecoveryKey(id = settingsId, origin = origin.originOrDefault())
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

    override fun logoutAndCallLoginScreen() {
        NavigationUtils.logoutAndCallLoginScreen(activity)
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

    override fun setupActionBar(topLevelDestinations: Set<Int>) {
        navigationController!!.setupActionBar(activity, topLevelDestinations)
        menuIconDestinationChangedListener?.topLevelDestinations = topLevelDestinations
    }

    private fun String?.originOrDefault() = this ?: "mainMenu"

    companion object {
        private const val UNLOCK_EVENT_CODE = 178
    }
}
