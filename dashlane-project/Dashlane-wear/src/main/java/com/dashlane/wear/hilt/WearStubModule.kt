package com.dashlane.wear.hilt

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.dashlane.accountstatus.AccountStatus
import com.dashlane.accountstatus.AccountStatusPostUpdateManager
import com.dashlane.hermes.AnalyticsInfo
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.navigation.Navigator
import com.dashlane.navigation.paywall.PaywallIntroType
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.teams.ActivityLog
import com.dashlane.user.Username
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.UUID

@Module
@InstallIn(SingletonComponent::class)
object WearStubModule {
    @Provides
    fun providesNavigator() = object : Navigator {
        override val currentDestination: NavDestination?
            get() = null

        override fun launchHomeActivity() = Unit

        override fun goToHome(filter: String?) = Unit

        override fun goToActionCenter() = Unit

        override fun goToPasswordGenerator() = Unit

        override fun goToPasswordSharing() = Unit

        override fun goToIdentityDashboard() = Unit

        override fun goToDarkWebMonitoring() = Unit

        override fun goToVpn() = Unit

        override fun goToCollectionsList() = Unit

        override fun goToSettings() = Unit

        override fun goToHelpCenter() = Unit

        override fun goToSecretTransfer(
            settingsId: String?,
            id: String?,
            key: String?,
        ) = Unit

        override fun goToAccountRecoveryKey(
            settingsId: String?,
            showIntro: Boolean,
            userCanExitFlow: Boolean,
        ) = Unit

        override fun goToPersonalPlanOrHome() = Unit

        override fun goToOffers(offerType: String?) = Unit

        override fun goToChromeInAppLogin() = Unit

        override fun goToInAppLogin() = Unit

        override fun goToSearch(query: String?) = Unit

        override fun goToSearchCompose() = Unit

        override fun goToPaywall(type: PaywallIntroType) = Unit

        override fun goToGuidedPasswordChange(
            itemId: String,
            domain: String,
            username: String?,
        ) = Unit

        override fun goToCurrentPlan() = Unit

        override fun goToAuthenticator(otpUri: Uri?) = Unit

        override fun goToAuthenticatorSuggestions(hasSetupOtpCredentials: Boolean) = Unit

        override fun goToActionCenterSectionDetails(section: String) = Unit

        override fun goToPasswordAnalysisFromBreach(breachId: String) = Unit

        override fun goToPasswordAnalysisFromIdentityDashboard(tab: String?) = Unit

        override fun goToItem(uid: String, type: String, editMode: Boolean) = Unit

        override fun goToItemHistoryFromItemEdit(uid: String) = Unit

        override fun goToGeneratedPasswordHistoryFromPasswordGenerator() = Unit

        override fun goToCreateItem(type: String) = Unit

        override fun goToCreateAuthentifiant(
            url: String,
            requestCode: Int?,
            successIntent: Intent?,
            otp: Parcelable?,
        ) = Unit

        override fun goToDeleteVaultItem(itemId: String, isShared: Boolean) = Unit

        override fun goToCollectionSelectorFromItemEdit(itemId: String) = Unit

        override fun goToImportLogins(showAddLoginsManually: Boolean) = Unit

        override fun goToCredentialAddStep1(
            expandImportOptions: Boolean,
            successIntent: Intent?,
        ) = Unit

        override fun goToCredentialFromPasswordAnalysis(uid: String) = Unit

        override fun goToCollectionDetails(
            collectionId: String,
            sharedCollection: Boolean,
        ) = Unit

        override fun goToCollectionDetailsFromCollectionsList(
            collectionId: String,
            sharedCollection: Boolean,
        ) = Unit

        override fun goToCollectionSharedAccessFromCollectionsList(collectionId: String) = Unit

        override fun goToCollectionAddFromCollectionsList() = Unit

        override fun goToCollectionEditFromCollectionsList(
            collectionId: String,
            sharedCollection: Boolean,
        ) = Unit

        override fun goToCollectionShareFromCollectionList(collectionId: String) = Unit

        override fun goToCollectionEditFromCollectionDetail(
            collectionId: String,
            sharedCollection: Boolean,
        ) = Unit

        override fun goToCollectionShareFromCollectionDetail(collectionId: String) = Unit

        override fun goToCollectionSharedAccessFromCollectionDetail(collectionId: String) = Unit

        override fun goToSectionDetailsFromActionCenter(section: String) = Unit

        override fun goToBreachAlertDetail(breachWrapper: Parcelable) = Unit

        override fun goToNewShare() = Unit

        override fun goToSharePeopleSelection(
            selectedPasswords: Array<String>,
            selectedNotes: Array<String>,
            selectedSecrets: Array<String>,
            origin: String?,
        ) = Unit

        override fun goToShareUsersForItems(uid: String) = Unit

        override fun goToPasswordSharingFromActionCenter(needsRefresh: Boolean) = Unit

        override fun goToPasswordSharingFromPeopleSelection() = Unit

        override fun goToPeopleSelectionFromNewShare(
            selectedPasswords: Array<String>,
            selectedNotes: Array<String>,
            selectedSecrets: Array<String>,
        ) = Unit

        override fun goToPeopleSelectionFromNewShareItemSelection(
            selectedPasswords: Array<String>,
            selectedNotes: Array<String>,
            selectedSecrets: Array<String>,
        ) = Unit

        override fun goToUserGroupFromPasswordSharing(
            groupId: String,
            groupName: String,
        ) = Unit

        override fun goToItemsForUserFromPasswordSharing(memberEmail: String) = Unit

        override fun goToGuidedPasswordChangeFromCredential(
            itemId: String,
            domain: String,
            username: String?,
        ) = Unit

        override fun goToActivateAccountFromVpnThirdParty(
            defaultEmail: String?,
            suggestions: List<String>?,
        ) = Unit

        override fun goToCsvImportIntro() = Unit

        override fun goToM2wImportIntro() = Unit

        override fun goToCompetitorImportIntro() = Unit

        override fun goToFollowUpNotificationDiscoveryScreen(isReminder: Boolean) = Unit

        override fun goToQuickActions(
            itemId: String,
            itemListContext: Parcelable,
        ) = Unit

        override fun popBackStack() = Unit

        override fun navigateUp(): Boolean = false

        override fun handleDeepLink(intent: Intent) = Unit

        override fun addOnDestinationChangedListener(listener: NavController.OnDestinationChangedListener) = Unit

        override fun removeOnDestinationChangedListener(listener: NavController.OnDestinationChangedListener) = Unit

        override fun goToManageDashlaneNotificationsSystem() = Unit

        override fun logoutAndCallLoginScreen(
            context: Context?,
            allowSkipEmail: Boolean,
        ) = Unit

        override fun goToWebView(url: String) = Unit

        override fun setupActionBar(topLevelDestinations: Set<Int>) = Unit

        override fun goToLinkedWebsites(
            itemId: String,
            fromViewOnly: Boolean,
            addNew: Boolean,
            temporaryWebsites: List<String>,
            temporaryApps: List<String>?,
            urlDomain: String?,
        ) = Unit

        override fun showAttachments(
            id: String,
            xmlObjectName: String,
            attachments: String?,
        ) = Unit

        override fun showNewSharing(
            id: String,
            fromAuthentifiant: Boolean,
            fromSecureNote: Boolean,
            fromSecrets: Boolean,
        ) = Unit

        override fun openWebsite(
            url: String?,
            packageNames: Set<String>,
        ) = Unit

        override fun goToBiometricOnboarding(context: Context) = Unit

        override fun goToBiometricRecoveryOnboarding(context: Context) = Unit
    }

    @Provides
    fun providesLogRepositoryStub() = object : LogRepository {
        override fun sessionStarted(authorization: Authorization.User) = Unit

        override fun sessionEnded() = Unit

        override fun queueEvent(event: TrackingLog) = Unit

        override fun queueEvent(event: ActivityLog) = Unit

        override fun queuePageView(component: BrowseComponent, page: AnyPage) = Unit

        override suspend fun flushLogs(authorization: Authorization.User?) = Unit

        override fun setUserAnalyticsInfo(userId: UUID, deviceId: UUID) = Unit

        override fun getAnalyticsInfo(): AnalyticsInfo = TODO()

        override fun resetAnalyticsInfo() = Unit
    }

    @Provides
    fun providesAccountStatusPostUpdateManager() = object : AccountStatusPostUpdateManager {
        override suspend fun onUpdate(
            username: Username,
            newStatus: AccountStatus,
            oldStatus: AccountStatus?,
        ) = Unit
    }
}