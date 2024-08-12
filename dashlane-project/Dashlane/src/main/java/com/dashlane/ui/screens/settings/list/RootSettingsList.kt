package com.dashlane.ui.screens.settings.list

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import com.dashlane.R
import com.dashlane.user.UserAccountInfo
import com.dashlane.account.UserAccountStorage
import com.dashlane.accountrecoverykey.setting.AccountRecoveryKeySettingStateHolder
import com.dashlane.accountstatus.AccountStatus
import com.dashlane.accountstatus.premiumstatus.isAdvancedPlan
import com.dashlane.accountstatus.premiumstatus.isFamilyAdmin
import com.dashlane.accountstatus.premiumstatus.isFamilyPlan
import com.dashlane.accountstatus.premiumstatus.isPremiumPlan
import com.dashlane.accountstatus.subscriptioncode.SubscriptionCodeRepository
import com.dashlane.activatetotp.ActivateTotpLogger
import com.dashlane.autofill.phishing.AutofillPhishingLogger
import com.dashlane.biometricrecovery.BiometricRecovery
import com.dashlane.crashreport.CrashReporter
import com.dashlane.debug.DaDaDa
import com.dashlane.followupnotification.domain.FollowUpNotificationSettings
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.events.user.Logout
import com.dashlane.inapplogin.InAppLoginManager
import com.dashlane.invites.InviteFriendsIntentHelper
import com.dashlane.login.lock.LockManager
import com.dashlane.masterpassword.ChangeMasterPasswordFeatureAccessChecker
import com.dashlane.navigation.Navigator
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.premium.enddate.EndDateFormatter
import com.dashlane.premium.enddate.FormattedEndDateProviderImpl
import com.dashlane.premium.utils.PlansUtils
import com.dashlane.search.SearchableSettingItem
import com.dashlane.securearchive.BackupCoordinator
import com.dashlane.security.SecurityHelper
import com.dashlane.server.api.endpoints.invitation.GetSharingLinkService
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.session.UserDataRepository
import com.dashlane.session.repository.UserCryptographyRepository
import com.dashlane.storage.userdata.RichIconsSettingProvider
import com.dashlane.sync.DataSync
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.ui.TeamSpaceRestrictionNotificator
import com.dashlane.ui.ScreenshotPolicy
import com.dashlane.ui.activities.debug.DebugActivity
import com.dashlane.ui.screens.settings.Use2faSettingStateHolder
import com.dashlane.ui.screens.settings.item.SearchableSettingItemImpl
import com.dashlane.ui.screens.settings.item.SensibleSettingsClickHelper
import com.dashlane.ui.screens.settings.item.SettingHeader
import com.dashlane.ui.screens.settings.item.SettingItem
import com.dashlane.ui.screens.settings.item.SettingScreenItem
import com.dashlane.ui.screens.settings.item.SettingSocialMediaLinks
import com.dashlane.ui.screens.settings.list.general.RootSettingsGeneralList
import com.dashlane.ui.screens.settings.list.help.RootSettingsHelpList
import com.dashlane.ui.screens.settings.list.security.RootSettingsSecurityList
import com.dashlane.ui.util.DialogHelper
import com.dashlane.ui.common.compose.components.socialmedia.DashlaneSocialMedia
import com.dashlane.featureflipping.FeatureFlip
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.util.DarkThemeHelper
import com.dashlane.util.IntentFactory
import com.dashlane.util.NetworkStateProvider
import com.dashlane.util.PackageUtilities.getAppVersionName
import com.dashlane.util.Toaster
import com.dashlane.util.clipboard.ClipboardCopy
import com.dashlane.util.hardwaresecurity.BiometricAuthModule
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject

class RootSettingsList @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationCoroutineScope private val coroutineScope: CoroutineScope,
    userPreferencesManager: UserPreferencesManager,
    private val daDaDa: DaDaDa,
    private val navigator: Navigator,
    private val toaster: Toaster,
    private val sharingLinkService: GetSharingLinkService,
    private val subscriptionCodeRepository: SubscriptionCodeRepository,
    private val accountStatusProvider: OptionalProvider<AccountStatus>,
    userFeaturesChecker: UserFeaturesChecker,
    lockManager: LockManager,
    inAppLoginManager: InAppLoginManager,
    biometricAuthModule: BiometricAuthModule,
    sessionManager: SessionManager,
    teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>,
    screenshotPolicy: ScreenshotPolicy,
    globalPreferencesManager: GlobalPreferencesManager,
    dialogHelper: DialogHelper,
    userAccountStorage: UserAccountStorage,
    sessionCredentialsSaver: SessionCredentialsSaver,
    cryptographyRepository: UserCryptographyRepository,
    securityHelper: SecurityHelper,
    masterPasswordFeatureAccessChecker: ChangeMasterPasswordFeatureAccessChecker,
    backupCoordinator: BackupCoordinator,
    crashReporter: CrashReporter,
    darkThemeHelper: DarkThemeHelper,
    biometricRecovery: BiometricRecovery,
    sensibleSettingsClickHelper: SensibleSettingsClickHelper,
    followUpNotificationSettings: FollowUpNotificationSettings,
    endDateFormatter: EndDateFormatter,
    logRepository: LogRepository,
    use2faSettingStateHolder: Use2faSettingStateHolder,
    activateTotpLogger: ActivateTotpLogger,
    dataSync: DataSync,
    networkStateProvider: NetworkStateProvider,
    teamspaceRestrictionNotificator: TeamSpaceRestrictionNotificator,
    clipboardCopy: ClipboardCopy,
    clock: Clock,
    autofillPhishingLogger: AutofillPhishingLogger,
    accountRecoveryKeySettingStateHolder: AccountRecoveryKeySettingStateHolder,
    userDataRepository: UserDataRepository,
    richIconsSettingProvider: RichIconsSettingProvider,
    frozenStateManager: FrozenStateManager,
) {

    private val paramHeader = SettingHeader(context.getString(R.string.settings_category_advanced))

    private val accountStatus: AccountStatus?
        get() = accountStatusProvider.get()

    private val premiumItem = object : SettingItem {
        override val id = "premium"
        override val header = SettingHeader(context.getString(R.string.setting_premium_category))
        override val title: String
            get() = accountStatus?.premiumStatus
                ?.let { PlansUtils.getTitle(context, it) }
                ?: context.getString(
                    R.string.plan_action_bar_title,
                    context.getString(R.string.plan_free_action_bar_title)
                )
        override val description: String
            get() = when {
                frozenStateManager.isAccountFrozen -> context.getString(R.string.frozen_state_subtitle)
                else -> {
                    accountStatus?.let {
                        endDateFormatter.getLabel(FormattedEndDateProviderImpl(it, clock))
                    } ?: context.getString(R.string.no_date_plan_subtitle)
                }
            }

        val isFamilyInvitee: Boolean
            get() = accountStatus?.premiumStatus
                ?.let { status ->
                    status.isFamilyPlan && !status.isFamilyAdmin
                } ?: false

        val isInTeam: Boolean
            get() = teamSpaceAccessorProvider.get()?.currentBusinessTeam != null

        override fun isEnable() = true
        override fun isVisible() = !isInTeam && !isFamilyInvitee

        override fun onClick(context: Context) {
            navigator.goToCurrentPlan()
        }
    }

    private val settingsSecurityList = RootSettingsSecurityList(
        context = context,
        coroutineScope = coroutineScope,
        lockManager = lockManager,
        securityHelper = securityHelper,
        biometricAuthModule = biometricAuthModule,
        navigator = navigator,
        screenshotPolicy = screenshotPolicy,
        userPreferencesManager = userPreferencesManager,
        teamSpaceAccessorProvider = teamSpaceAccessorProvider,
        sessionManager = sessionManager,
        userAccountStorage = userAccountStorage,
        sessionCredentialsSaver = sessionCredentialsSaver,
        dialogHelper = dialogHelper,
        cryptographyRepository = cryptographyRepository,
        rootHeader = paramHeader,
        sensibleSettingsClickHelper = sensibleSettingsClickHelper,
        masterPasswordFeatureAccessChecker = masterPasswordFeatureAccessChecker,
        biometricRecovery = biometricRecovery,
        toaster = toaster,
        use2faSettingStateHolder = use2faSettingStateHolder,
        activateTotpLogger = activateTotpLogger,
        userFeaturesChecker = userFeaturesChecker,
        teamspaceRestrictionNotificator = teamspaceRestrictionNotificator,
        subscriptionCodeRepository = subscriptionCodeRepository,
        accountRecoveryKeySettingStateHolder = accountRecoveryKeySettingStateHolder,
        userDataRepository = userDataRepository,
        richIconsSettingProvider = richIconsSettingProvider
    )

    private val settingsGeneralList = RootSettingsGeneralList(
        context = context,
        coroutineScope = coroutineScope,
        userFeaturesChecker = userFeaturesChecker,
        lockManager = lockManager,
        inAppLoginManager = inAppLoginManager,
        navigator = navigator,
        rootHeader = paramHeader,
        backupCoordinator = backupCoordinator,
        darkThemeHelper = darkThemeHelper,
        logRepository = logRepository,
        sensibleSettingsClickHelper = sensibleSettingsClickHelper,
        userPreferencesManager = userPreferencesManager,
        globalPreferencesManager = globalPreferencesManager,
        followUpNotificationSettings = followUpNotificationSettings,
        dataSync = dataSync,
        dialogHelper = dialogHelper,
        teamSpaceAccessorProvider = teamSpaceAccessorProvider,
        autofillPhishingLogger = autofillPhishingLogger,
        frozenStateManager = frozenStateManager,
    )

    private val settingsHelpList = RootSettingsHelpList(
        context = context,
        navigator = navigator,
        rootHeader = paramHeader,
        crashReporter = crashReporter,
        clipboardCopy = clipboardCopy
    )

    private val settingsSecretTransfer = object : SettingItem {
        override val id = "secret-transfer"
        override val header = paramHeader
        override val title = context.getString(R.string.secret_transfer_settings_entry_title)
        override val description = context.getString(R.string.secret_transfer_settings_entry_description)
        override fun isEnable() = true
        override fun isVisible() = true
        override fun onClick(context: Context) {
            navigator.goToSecretTransfer(id)
        }
    }

    private val miscHeader =
        SettingHeader(context.getString(R.string.settings_category_sharing))

    private val inviteFriendItem = object : SettingItem {
        override val id = "invite-friend"
        override val header = miscHeader
        override val title = context.getString(R.string.invites)
        override val description = context.getString(
            if (isPremiumRewardCandidate()) {
                R.string.setting_send_invites_description
            } else {
                R.string.setting_send_invites_description_premium_user
            }
        )

        override fun isEnable() = true
        override fun isVisible() = true
        override fun onClick(context: Context) {
            coroutineScope.launch {
                InviteFriendsIntentHelper.launchInviteFriendsIntent(
                    context = context,
                    toaster = toaster,
                    sharingLinkService = sharingLinkService,
                    subscriptionCodeRepository = subscriptionCodeRepository,
                    networkStateProvider = networkStateProvider,
                )
            }
        }

        private fun isPremiumRewardCandidate(): Boolean {
            val premiumStatus = accountStatus?.premiumStatus ?: return true
            return !premiumStatus.isPremiumPlan || premiumStatus.isAdvancedPlan
        }
    }

    private val rateDashlaneItem = object : SettingItem {
        override val id = "rate-dashlane"
        override val header = miscHeader
        override val title = context.getString(R.string.setting_send_love)
        override val description = context.getString(R.string.setting_send_love_description)
        override fun isEnable() = true
        override fun isVisible() = true
        override fun onClick(context: Context) = IntentFactory.sendMarketIntent(context)
    }

    private val dashlaneLabs = object : SettingItem {
        override val id = "dashlane-labs"
        override val header = miscHeader
        override val title = context.getString(R.string.setting_dashlane_labs)
        override val description = context.getString(R.string.setting_dashlane_labs_description)
        override fun isEnable() = true
        override fun isVisible() = userFeaturesChecker.has(FeatureFlip.DASHLANE_LABS)

        override fun onClick(context: Context) = navigator.goToDashlaneLabs()
    }

    private val logoutItem = object : SettingItem {
        override val id = "logout"
        override val header = miscHeader
        override val title = context.getString(R.string.logout)
        override val description = ""
        override fun isEnable() = true
        override fun isVisible() = true

        override fun onClick(context: Context) {
            val isPasswordlessAccount = sessionManager.session?.username
                ?.let { username -> userAccountStorage[username]?.accountType }
                .let { accountType -> accountType is UserAccountInfo.AccountType.InvisibleMasterPassword }

            val onPositiveClick: (DialogInterface, Int) -> Unit = { dialogInterface, _ ->
                dialogInterface.dismiss()
                logRepository.queueEvent(Logout())
                navigator.logoutAndCallLoginScreen(context)
            }

            if (isPasswordlessAccount) {
                dialogHelper.builder(context, R.style.ThemeOverlay_Dashlane_DashlaneWarningDialog)
                    .setTitle(R.string.log_out_dialog_title)
                    .setMessage(R.string.log_out_dialog_description)
                    .setPositiveButton(R.string.log_out_dialog_positive_button, onPositiveClick)
                    .setNegativeButton(R.string.log_out_dialog_negative_button, null)
            } else {
                dialogHelper.builder(context, R.style.ThemeOverlay_Dashlane_DashlaneAlertDialog)
                    .setTitle(R.string.logout)
                    .setMessage(R.string.log_out_of_dashlane_)
                    .setPositiveButton(R.string.yes, onPositiveClick)
                    .setNegativeButton(R.string.no, null)
            }
                .setCancelable(true)
                .show()
        }
    }

    private val appVersionItem = object : SettingItem {

        override val id = "app-version"
        override val header = miscHeader
        override val title = context.getString(R.string.setting_about_version)
        override val description = daDaDa.appVersionName ?: context.getAppVersionName()

        override fun isEnable() = true
        override fun isVisible() = true
        override fun onClick(context: Context) {
            clipboardCopy.copyToClipboard(description, sensitiveData = false, autoClear = false)
        }
    }

    private val socialMediaHeader =
        SettingHeader(context.getString(R.string.settings_social_media_header))

    private val socialMediaItem = object : SettingSocialMediaLinks {
        override val id = "social-links"
        override val header = socialMediaHeader
        override val socialMediaList: List<DashlaneSocialMedia> = DashlaneSocialMedia.entries
    }

    private val debugItem = object : SettingItem {
        override val id = "debug"
        override val header = miscHeader
        override val title = context.getString(R.string.setting_debug_link)
        override val description = ""

        override fun isEnable() = true
        override fun isVisible() = daDaDa.isEnabled
        override fun onClick(context: Context) = context.startActivity(Intent(context, DebugActivity::class.java))
    }

    private val root = SettingScreenItem(
        navigator,
        AnyPage.SETTINGS,
        item = object : SettingItem {
            
            override val id = ""
            override val header: SettingHeader? = null
            override val title = ""
            override val description = ""
            override fun isEnable() = true
            override fun isVisible() = true
            override fun onClick(context: Context) {}
        },
        subItems = listOf(
            premiumItem,
            settingsSecurityList.root,
            settingsGeneralList.root,
            settingsHelpList.root,
            settingsSecretTransfer,
            inviteFriendItem,
            rateDashlaneItem,
            dashlaneLabs,
            logoutItem,
            appVersionItem,
            debugItem,
            socialMediaItem,
        )
    )

    fun getScreenForId(id: String?): SettingScreenItem {
        
        return id?.let { root.findId(it) } ?: root
    }

    fun getSearchableItems(): List<SearchableSettingItem> {
        return root.getSearchableItems(listOf())
    }

    private fun SettingScreenItem.findId(id: String): SettingScreenItem? {
        if (this.id == id) {
            
            return this
        }
        this.subItems.forEach { subItem ->
            when (subItem) {
                is SettingScreenItem -> subItem.findId(id)?.let { return it } 
                
                else -> if (subItem.id == id) return this
            }
        }
        return null
    }

    private fun SettingItem.getSearchableItems(parents: List<SettingScreenItem>): List<SearchableSettingItem> {
        return when (this) {
            is SettingScreenItem -> {
                val superParents = parents.plus(this)
                subItems.map { it.getSearchableItems(superParents) }.flatten()
            }

            else -> listOf(SearchableSettingItemImpl(this, parents))
        }
    }
}
