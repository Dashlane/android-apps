package com.dashlane.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import androidx.navigation.NavController.OnDestinationChangedListener
import androidx.navigation.NavDestination
import com.dashlane.navigation.paywall.PaywallIntroType

interface Navigator {
    val currentDestination: NavDestination?

    fun launchHomeActivity()

    
    fun goToHome(filter: String? = null)
    fun goToActionCenter()
    fun goToPasswordGenerator()

    fun goToPasswordSharing()
    fun goToIdentityDashboard()
    fun goToDarkWebMonitoring()
    fun goToVpn()
    fun goToCollectionsList()
    fun goToSettings(settingsId: String? = null)
    fun goToHelpCenter()
    fun goToSecretTransfer(settingsId: String? = null)
    fun goToAccountRecoveryKey(
        settingsId: String? = null,
        showIntro: Boolean = false,
        userCanExitFlow: Boolean = true
    )

    fun goToPersonalPlanOrHome()

    fun goToOffers(offerType: String? = null)

    fun goToInAppLoginIntro()
    fun goToInAppLogin()
    fun goToSearch(query: String? = null)

    fun goToPaywall(type: PaywallIntroType)

    fun goToGuidedPasswordChange(
        itemId: String,
        domain: String,
        username: String? = null
    )

    fun goToCurrentPlan()

    fun goToAuthenticator(otpUri: Uri? = null)
    fun goToAuthenticatorSuggestions(hasSetupOtpCredentials: Boolean)

    fun goToAuthenticatorIntro(
        credentialName: String,
        credentialId: String,
        topDomain: String,
        packageName: String?,
        proSpace: Boolean
    )

    
    
    fun goToActionCenterSectionDetails(section: String)

    
    fun goToPasswordAnalysisFromBreach(breachId: String)
    fun goToPasswordAnalysisFromIdentityDashboard(tab: String? = null)

    
    fun goToItem(uid: String, type: String, editMode: Boolean = false)

    
    fun goToItemHistory(uid: String)
    fun goToCreateItem(type: String)
    fun goToCreateAuthentifiant(
        url: String,
        requestCode: Int? = null,
        successIntent: Intent? = null,
        otp: Parcelable? = null
    )

    fun goToDeleteVaultItem(itemId: String, isShared: Boolean)

    fun goToCollectionSelectorFromItemEdit(
        fromViewOnly: Boolean,
        temporaryPrivateCollectionsName: List<String>,
        temporarySharedCollectionsId: List<String>,
        spaceId: String?,
        isLimited: Boolean
    )

    fun goToCredentialAddStep1(expandImportOptions: Boolean = false, successIntent: Intent? = null)
    fun goToCredentialFromPasswordAnalysis(uid: String)

    
    fun goToCollectionDetails(
        collectionId: String,
        businessSpace: Boolean,
        sharedCollection: Boolean,
        shareAllowed: Boolean
    )

    fun goToCollectionDetailsFromCollectionsList(
        collectionId: String,
        businessSpace: Boolean,
        sharedCollection: Boolean,
        shareAllowed: Boolean
    )

    fun goToCollectionSharedAccessFromCollectionsList(collectionId: String)

    fun goToCollectionAddFromCollectionsList()
    fun goToCollectionEditFromCollectionsList(collectionId: String, sharedCollection: Boolean = false)
    fun goToCollectionShareFromCollectionList(collectionId: String)
    fun goToCollectionEditFromCollectionDetail(collectionId: String, sharedCollection: Boolean = false)
    fun goToCollectionShareFromCollectionDetail(collectionId: String)
    fun goToCollectionSharedAccessFromCollectionDetail(collectionId: String)

    
    fun goToSectionDetailsFromActionCenter(section: String)

    
    fun goToManageDevicesFromSettings()
    fun goToAutofillPauseAndLinkedFromSettings()
    fun goToDashlaneLabs()
    fun goToGuidedWebCsvExport()
    fun goToAccountStatus()
    fun goToChangeContactEmail()

    
    fun goToBreachAlertDetail(breachWrapper: Parcelable)

    
    fun goToNewShare()
    fun goToSharePeopleSelection(
        selectedPasswords: Array<String> = emptyArray(),
        selectedNotes: Array<String> = emptyArray(),
        origin: String? = null
    )

    fun goToShareUsersForItems(uid: String)
    fun goToPasswordSharingFromActionCenter(needsRefresh: Boolean = false)
    fun goToPasswordSharingFromPeopleSelection()
    fun goToPeopleSelectionFromNewShare(
        selectedPasswords: Array<String> = emptyArray(),
        selectedNotes: Array<String> = emptyArray()
    )

    fun goToUserGroupFromPasswordSharing(groupId: String, groupName: String)
    fun goToItemsForUserFromPasswordSharing(memberEmail: String)

    fun goToGuidedPasswordChangeFromCredential(itemId: String, domain: String, username: String?)

    
    fun goToLearnMoreAboutVpnFromVpnThirdParty()
    fun goToGetStartedFromVpnThirdParty()
    fun goToActivateAccountFromVpnThirdParty(
        defaultEmail: String? = null,
        suggestions: List<String>? = null
    )

    
    fun goToCsvImportIntro()
    fun goToM2wImportIntro()
    fun goToCompetitorImportIntro()

    
    fun goToFollowUpNotificationDiscoveryScreen(isReminder: Boolean)

    
    fun goToQuickActions(itemId: String, itemListContext: Parcelable)

    fun popBackStack()
    fun navigateUp(): Boolean
    fun handleDeepLink(intent: Intent)

    fun addOnDestinationChangedListener(listener: OnDestinationChangedListener)
    fun removeOnDestinationChangedListener(listener: OnDestinationChangedListener)
    fun goToManageDashlaneNotificationsSystem()

    
    fun logoutAndCallLoginScreen(context: Context? = null)

    
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
    fun showAttachments(
        id: String,
        xmlObjectName: String,
        attachments: String?,
    )
    fun showNewSharing(
        id: String,
        fromAuthentifiant: Boolean,
        fromSecureNote: Boolean
    )

    fun openWebsite(url: String?, packageNames: Set<String>)

    fun goToBiometricOnboarding(context: Context)
}
