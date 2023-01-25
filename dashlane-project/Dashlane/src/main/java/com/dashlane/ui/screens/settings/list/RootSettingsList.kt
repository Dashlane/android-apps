package com.dashlane.ui.screens.settings.list

import android.content.Context
import android.content.Intent
import com.dashlane.R
import com.dashlane.account.UserAccountStorage
import com.dashlane.accountrecovery.AccountRecovery
import com.dashlane.activatetotp.ActivateTotpLogger
import com.dashlane.core.DataSync
import com.dashlane.core.premium.FamilyMembership
import com.dashlane.crashreport.CrashReporter
import com.dashlane.followupnotification.domain.FollowUpNotificationSettings
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.events.user.Logout
import com.dashlane.inapplogin.InAppLoginManager
import com.dashlane.invites.InviteFriendsIntentHelper
import com.dashlane.login.lock.LockManager
import com.dashlane.masterpassword.ChangeMasterPasswordFeatureAccessChecker
import com.dashlane.navigation.NavigationUtils
import com.dashlane.navigation.Navigator
import com.dashlane.network.inject.LegacyWebservicesApi
import com.dashlane.network.webservices.GetSharingLinkService
import com.dashlane.plans.ui.PlansUtils
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.premium.enddate.EndDateFormatter
import com.dashlane.search.SearchableSettingItem
import com.dashlane.securearchive.BackupCoordinator
import com.dashlane.security.SecurityHelper
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.AccountStatusRepository
import com.dashlane.session.repository.UserCryptographyRepository
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.ui.ScreenshotPolicy
import com.dashlane.ui.activities.debug.DebugActivity
import com.dashlane.ui.screens.settings.Use2faSettingStateHolder
import com.dashlane.ui.screens.settings.item.SearchableSettingItemImpl
import com.dashlane.ui.screens.settings.item.SensibleSettingsClickHelper
import com.dashlane.ui.screens.settings.item.SettingHeader
import com.dashlane.ui.screens.settings.item.SettingItem
import com.dashlane.ui.screens.settings.item.SettingScreenItem
import com.dashlane.ui.screens.settings.list.general.RootSettingsGeneralList
import com.dashlane.ui.screens.settings.list.help.RootSettingsHelpList
import com.dashlane.ui.screens.settings.list.security.RootSettingsSecurityList
import com.dashlane.ui.util.DialogHelper
import com.dashlane.useractivity.log.usage.UsageLogCode35
import com.dashlane.useractivity.log.usage.UsageLogConstant
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.usersupportreporter.UserSupportFileLogger
import com.dashlane.usersupportreporter.UserSupportFileUploader
import com.dashlane.debug.DaDaDa
import com.dashlane.util.DarkThemeHelper
import com.dashlane.util.IntentFactory
import com.dashlane.util.PackageUtilities.getAppVersionName
import com.dashlane.util.ThreadHelper
import com.dashlane.util.Toaster
import com.dashlane.util.clipboard.ClipboardUtils
import com.dashlane.util.hardwaresecurity.BiometricAuthModule
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.util.inject.qualifiers.GlobalCoroutineScope
import com.dashlane.util.userfeatures.UserFeaturesChecker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import retrofit2.Retrofit



class RootSettingsList @Inject constructor(
    @ApplicationContext
    private val context: Context,
    @GlobalCoroutineScope
    private val coroutineScope: CoroutineScope,
    userFeaturesChecker: UserFeaturesChecker,
    lockManager: LockManager,
    inAppLoginManager: InAppLoginManager,
    biometricAuthModule: BiometricAuthModule,
    sessionManager: SessionManager,
    teamspaceAccessorProvider: OptionalProvider<TeamspaceAccessor>,
    screenshotPolicy: ScreenshotPolicy,
    private val userPreferencesManager: UserPreferencesManager,
    globalPreferencesManager: GlobalPreferencesManager,
    @LegacyWebservicesApi retrofit: Retrofit,
    dialogHelper: DialogHelper,
    userAccountStorage: UserAccountStorage,
    sessionCredentialsSaver: SessionCredentialsSaver,
    cryptographyRepository: UserCryptographyRepository,
    threadHelper: ThreadHelper,
    private val daDaDa: DaDaDa,
    securityHelper: SecurityHelper,
    masterPasswordFeatureAccessChecker: ChangeMasterPasswordFeatureAccessChecker,
    backupCoordinator: BackupCoordinator,
    accountStatusRepository: AccountStatusRepository,
    crashReporter: CrashReporter,
    darkThemeHelper: DarkThemeHelper,
    accountRecovery: AccountRecovery,
    userSupportFileUploader: UserSupportFileUploader,
    dataSync: DataSync,
    sensibleSettingsClickHelper: SensibleSettingsClickHelper,
    userSupportFileLogger: UserSupportFileLogger,
    private val navigator: Navigator,
    private val toaster: Toaster,
    bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val sharingLinkService: GetSharingLinkService,
    followUpNotificationSettings: FollowUpNotificationSettings,
    endDateFormatter: EndDateFormatter,
    logRepository: LogRepository,
    use2faSettingStateHolder: Use2faSettingStateHolder,
    activateTotpLogger: ActivateTotpLogger
) {

    private val paramHeader = SettingHeader(context.getString(R.string.settings_category_advanced))

    private val premiumItem = object : SettingItem {
        override val id = "premium"
        override val header = SettingHeader(context.getString(R.string.setting_premium_category))
        override val title: String
            get() = sessionManager.session?.let { accountStatusRepository.getPremiumStatus(it) }
                ?.let { PlansUtils.getTitle(context, it) }
                ?: context.getString(
                    R.string.plan_action_bar_title,
                    context.getString(R.string.plan_free_action_bar_title)
                )
        override val description: String
            get() = sessionManager.session?.let { accountStatusRepository.getPremiumStatus(it) }
                ?.let { endDateFormatter.getLabel(it) }
                ?: context.getString(R.string.no_date_plan_subtitle)

        val isFamilyInvitee: Boolean
            get() = sessionManager.session?.let { accountStatusRepository.getPremiumStatus(it) }
                ?.let { status ->
                    status.isFamilyUser && status.familyMemberships!!.all { it == FamilyMembership.REGULAR }
                } ?: false
        val isInTeam: Boolean
            get() = teamspaceAccessorProvider.get()?.canChangeTeamspace() ?: false

        override fun isEnable(context: Context) = true
        override fun isVisible(context: Context) = !isInTeam && !isFamilyInvitee

        override fun onClick(context: Context) {
            bySessionUsageLogRepository[sessionManager.session]
                ?.enqueue(
                    UsageLogCode35(
                        type = UsageLogConstant.ViewType.goPremium,
                        action = UsageLogConstant.PremiumAction.goPremiumFromSettings
                    )
                )
            navigator.goToCurrentPlan(origin = ORIGIN_SETTINGS)
        }
    }

    private val settingsSecurityList =
        RootSettingsSecurityList(
            context = context,
            lockManager = lockManager,
            securityHelper = securityHelper,
            biometricAuthModule = biometricAuthModule,
            navigator = navigator,
            screenshotPolicy = screenshotPolicy,
            userPreferencesManager = userPreferencesManager,
            teamspaceAccessorProvider = teamspaceAccessorProvider,
            retrofit = retrofit,
            sessionManager = sessionManager,
            userAccountStorage = userAccountStorage,
            sessionCredentialsSaver = sessionCredentialsSaver,
            dialogHelper = dialogHelper,
            cryptographyRepository = cryptographyRepository,
            threadHelper = threadHelper,
            rootHeader = paramHeader,
            sensibleSettingsClickHelper = sensibleSettingsClickHelper,
            masterPasswordFeatureAccessChecker = masterPasswordFeatureAccessChecker,
            accountRecovery = accountRecovery,
            toaster = toaster,
            bySessionUsageLogRepository = bySessionUsageLogRepository,
            use2faSettingStateHolder = use2faSettingStateHolder,
            activateTotpLogger = activateTotpLogger,
            userFeaturesChecker = userFeaturesChecker,
        )

    private val settingsGeneralList =
        RootSettingsGeneralList(
            context = context,
            coroutineScope = coroutineScope,
            userFeaturesChecker = userFeaturesChecker,
            lockManager = lockManager,
            inAppLoginManager = inAppLoginManager,
            navigator = navigator,
            rootHeader = paramHeader,
            backupCoordinator = backupCoordinator,
            sessionManager = sessionManager,
            darkThemeHelper = darkThemeHelper,
            bySessionUsageLogRepository = bySessionUsageLogRepository,
            logRepository = logRepository,
            sensibleSettingsClickHelper = sensibleSettingsClickHelper,
            userPreferencesManager = userPreferencesManager,
            globalPreferencesManager = globalPreferencesManager,
            followUpNotificationSettings = followUpNotificationSettings
        )

    private val settingsHelpList =
        RootSettingsHelpList(
            context = context,
            navigator = navigator,
            rootHeader = paramHeader,
            crashReporter = crashReporter,
            userSupportFileUploader = userSupportFileUploader,
            dataSync = dataSync,
            sessionManager = sessionManager,
            bySessionUsageLogRepository = bySessionUsageLogRepository
        )

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

        override fun isEnable(context: Context) = true
        override fun isVisible(context: Context) = true
        override fun onClick(context: Context) {
            InviteFriendsIntentHelper.launchInviteFriendsIntent(
                context,
                toaster,
                sharingLinkService,
                sessionManager,
                userPreferencesManager.referralId
            )
        }

        private fun isPremiumRewardCandidate(): Boolean {
            val premiumStatus = sessionManager.session
                ?.let { accountStatusRepository.getPremiumStatus(it) }
                ?: return true
            return !premiumStatus.isPremium || premiumStatus.premiumPlan.isEssentials
        }
    }

    private val rateDashlaneItem = object : SettingItem {
        override val id = "rate-dashlane"
        override val header = miscHeader
        override val title = context.getString(R.string.setting_send_love)
        override val description = context.getString(R.string.setting_send_love_description)
        override fun isEnable(context: Context) = true
        override fun isVisible(context: Context) = true
        override fun onClick(context: Context) = IntentFactory.sendMarketIntent(context)
    }

    private val dashlaneLabs = object : SettingItem {
        override val id = "dashlane-labs"
        override val header = miscHeader
        override val title = context.getString(R.string.setting_dashlane_labs)
        override val description = context.getString(R.string.setting_dashlane_labs_description)
        override fun isEnable(context: Context) = true
        override fun isVisible(context: Context) =
            userFeaturesChecker.has(UserFeaturesChecker.FeatureFlip.DASHLANE_LABS)

        override fun onClick(context: Context) = navigator.goToDashlaneLabs()
    }

    private val logoutItem = object : SettingItem {
        override val id = "logout"
        override val header = miscHeader
        override val title = context.getString(R.string.logout)
        override val description = ""
        override fun isEnable(context: Context) = true
        override fun isVisible(context: Context) = true

        override fun onClick(context: Context) {
            dialogHelper.builder(context)
                .setTitle(R.string.logout)
                .setMessage(R.string.log_out_of_dashlane_)
                .setPositiveButton(R.string.yes) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                    bySessionUsageLogRepository[sessionManager.session]
                        ?.enqueue(
                            UsageLogCode35(
                                type = UsageLogConstant.ViewType.MainMenu,
                                action = UsageLogConstant.ActionType.logoutNow
                            )
                        )
                    logRepository.queueEvent(Logout())
                    userSupportFileLogger.add("Settings Logout")
                    NavigationUtils.logoutAndCallLoginScreen(context)
                }
                .setNegativeButton(R.string.no, null)
                .setCancelable(true)
                .show()
        }
    }

    private val appVersionItem = object : SettingItem {

        override val id = "app-version"
        override val header = miscHeader
        override val title = context.getString(R.string.setting_about_version)
        override val description = daDaDa.appVersionName ?: context.getAppVersionName()

        override fun isEnable(context: Context) = true
        override fun isVisible(context: Context) = true
        override fun onClick(context: Context) {
            ClipboardUtils.copyToClipboard(description, sensitiveData = false, autoClear = false)
        }
    }

    private val debugItem = object : SettingItem {
        override val id = "debug"
        override val header = miscHeader
        override val title = context.getString(R.string.setting_debug_link)
        override val description = ""

        override fun isEnable(context: Context) = true
        override fun isVisible(context: Context) = daDaDa.isEnabled
        override fun onClick(context: Context) =
            context.startActivity(Intent(context, DebugActivity::class.java))
    }

    private val root = SettingScreenItem(
        navigator,
        AnyPage.SETTINGS,
        item = object : SettingItem {
            
            override val id = ""
            override val header: SettingHeader? = null
            override val title = ""
            override val description = ""
            override fun isEnable(context: Context) = true
            override fun isVisible(context: Context) = true
            override fun onClick(context: Context) {}
        },
        subItems = listOf(
            premiumItem,
            settingsSecurityList.root,
            settingsGeneralList.root,
            settingsHelpList.root,
            inviteFriendItem,
            rateDashlaneItem,
            dashlaneLabs,
            logoutItem,
            appVersionItem,
            debugItem
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
                is SettingScreenItem -> subItem.findId(id)
                    ?.let { return it } 
                
                else -> if (subItem.id == id) return this
            }
        }
        return null
    }

    private fun SettingItem.getSearchableItems(parents: List<SettingScreenItem>): List<SearchableSettingItem> {
        return when {
            this is SettingScreenItem -> {
                val superParents = parents.plus(this)
                subItems.map { it.getSearchableItems(superParents) }.flatten()
            }
            else -> listOf(SearchableSettingItemImpl(this, parents))
        }
    }

    companion object {
        private const val ORIGIN_SETTINGS = "settings"
    }
}
