package com.dashlane.navigation

import android.annotation.TargetApi
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import androidx.navigation.NavController.OnDestinationChangedListener
import androidx.navigation.NavDestination
import com.dashlane.hermes.generated.definitions.AnyPage
import java.io.Serializable

interface Navigator {
    

    val currentDestination: NavDestination?

    
    fun goToHome(origin: String? = null, filter: String? = null)
    fun goToActionCenter(origin: String? = null)
    fun goToPasswordGenerator(origin: String? = null)

    fun goToPasswordSharing(origin: String? = null)
    fun goToIdentityDashboard(origin: String? = null)
    fun goToDarkWebMonitoring(origin: String? = null)
    fun goToVpn(origin: String? = null)
    fun goToSettings(settingsId: String? = null, origin: String? = null)
    fun goToHelpCenter(origin: String? = null)
    fun goToPersonalPlanOrHome(origin: String? = null)
    fun goToOffers(origin: String? = null, offerType: String? = null)

    
    fun goToInAppLoginIntro(origin: String = "autofill_extensions")
    fun goToInAppLogin(origin: String? = null, onBoardingType: Serializable? = null)
    fun goToSearch(query: String? = null)
    fun goToPaywall(type: String, origin: String? = null)

    

    @TargetApi(Build.VERSION_CODES.P)
    fun goToGuidedPasswordChange(
        itemId: String,
        domain: String,
        username: String? = null,
        origin: String? = null
    )

    fun goToCurrentPlan(origin: String)

    fun goToAuthenticator(otpUri: Uri? = null)
    fun goToAuthenticatorSuggestions(hasSetupOtpCredentials: Boolean)
    fun goToGetStartedFromAuthenticator()
    fun goToGetStartedFromAuthenticatorSuggestions()

    
    
    fun goToActionCenterSectionDetails(section: String)

    
    fun goToPasswordAnalysisFromBreach(breachId: String, origin: String? = null)
    fun goToPasswordAnalysisFromIdentityDashboard(origin: String? = null, tab: String? = null)

    
    fun goToItem(uid: String, type: Int)
    fun goToCreateItem(type: Int)
    fun goToCreateAuthentifiant(
        sender: String?,
        url: String,
        requestCode: Int? = null,
        successIntent: Intent? = null,
        otp: Parcelable? = null
    )

    fun goToDeleteVaultItem(itemId: String, isShared: Boolean)

    fun goToCredentialAddStep1(sender: String?, expandImportOptions: Boolean = false, successIntent: Intent? = null)
    fun goToCredentialFromPasswordAnalysis(uid: String)

    
    fun goToSectionDetailsFromActionCenter(section: String)

    
    fun goToManageDevicesFromSettings()
    fun goToAutofillPauseAndLinkedFromSettings(origin: String? = null)
    fun goToDashlaneLabs()

    
    fun goToBreachAlertDetail(breachWrapper: Parcelable, origin: String? = null)

    
    fun goToNewShare(origin: String)
    fun goToSharePeopleSelection(
        selectedPasswords: Array<String> = emptyArray(),
        selectedNotes: Array<String> = emptyArray(),
        origin: String? = null
    )

    fun goToShareUsersForItems(uid: String)
    fun goToPasswordSharingFromActionCenter(origin: String? = null, needsRefresh: Boolean = false)
    fun goToPasswordSharingFromPeopleSelection()
    fun goToPeopleSelectionFromNewShare(
        selectedPasswords: Array<String> = emptyArray(),
        selectedNotes: Array<String> = emptyArray()
    )

    fun goToUserGroupFromPasswordSharing(groupId: String, groupName: String)
    fun goToItemsForUserFromPasswordSharing(memberEmail: String)

    @TargetApi(Build.VERSION_CODES.P)
    fun goToGuidedPasswordChangeFromCredential(itemId: String, domain: String, username: String?, requestCode: Int)

    
    fun goToLearnMoreAboutVpnFromVpnThirdParty()
    fun goToGetStartedFromVpnThirdParty()
    fun goToActivateAccountFromVpnThirdParty(
        defaultEmail: String? = null,
        suggestions: List<String>? = null
    )

    
    fun goToCsvImportIntro()
    fun goToChromeImportIntro(origin: String)
    fun goToM2wImportIntro(origin: String)
    fun goToCompetitorImportIntro()

    
    fun goToFollowUpNotificationDiscoveryScreen(isReminder: Boolean)

    
    fun goToQuickActions(itemId: String, itemListContext: Parcelable, originPage: AnyPage?)

    fun popBackStack()
    fun navigateUp(): Boolean
    fun handleDeepLink(intent: Intent)

    fun addOnDestinationChangedListener(listener: OnDestinationChangedListener)
    fun removeOnDestinationChangedListener(listener: OnDestinationChangedListener)
    fun goToManageDashlaneNotificationsSystem()

    
    fun logoutAndCallLoginScreen()

    
    fun goToWebView(url: String)

    fun setupActionBar(topLevelDestinations: Set<Int>)
    fun goToLinkedWebsites(
        itemId: String,
        fromViewOnly: Boolean,
        addNew: Boolean,
        temporaryWebsites: List<String>,
        temporaryApps: List<String>?,
        urlDomain: String?
    )
}