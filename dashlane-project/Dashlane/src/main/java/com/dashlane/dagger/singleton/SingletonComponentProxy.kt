package com.dashlane.dagger.singleton

import com.dashlane.abtesting.OfflineExperimentReporter
import com.dashlane.account.UserAccountStorage
import com.dashlane.accountrecovery.AccountRecovery
import com.dashlane.announcements.AnnouncementCenter
import com.dashlane.authenticator.AuthenticatorAppConnection
import com.dashlane.autofill.AutofillAnalyzerDef.IAutofillUsageLog
import com.dashlane.autofill.AutofillAnalyzerDef.IUserPreferencesAccess
import com.dashlane.autofill.LinkedServicesHelper
import com.dashlane.autofill.api.changepassword.AutofillApiChangePasswordComponent
import com.dashlane.autofill.api.changepause.AutofillApiChangePauseComponent
import com.dashlane.autofill.api.common.AutofillApiGeneratePasswordComponent
import com.dashlane.autofill.api.createaccount.AutofillApiCreateAccountComponent
import com.dashlane.autofill.api.internal.AutofillApiComponent
import com.dashlane.autofill.api.linkedservices.AppMetaDataToLinkedAppsMigration
import com.dashlane.autofill.api.linkedservices.RememberToLinkedAppsMigration
import com.dashlane.autofill.api.monitorlog.MonitorAutofillIssuesComponent
import com.dashlane.autofill.api.pause.AutofillApiPauseComponent
import com.dashlane.autofill.api.rememberaccount.AutofillApiRememberAccountComponent
import com.dashlane.autofill.api.totp.AutofillApiTotpComponent
import com.dashlane.autofill.api.unlinkaccount.AutofillApiUnlinkAccountsComponent
import com.dashlane.autofill.api.viewallaccounts.AutofillApiViewAllAccountsComponent
import com.dashlane.braze.BrazeWrapper
import com.dashlane.breach.BreachManager
import com.dashlane.core.DataSync
import com.dashlane.core.sharing.SharingItemUpdater
import com.dashlane.core.xmlconverter.DataIdentifierSharingXmlConverter
import com.dashlane.crashreport.CrashReporter
import com.dashlane.crashreport.CrashReporterComponent
import com.dashlane.cryptography.CryptographyComponent
import com.dashlane.cryptography.SharingCryptographyComponent
import com.dashlane.dagger.sync.SyncSingletonComponent
import com.dashlane.dagger.sync.SyncSingletonDataStorageComponent
import com.dashlane.database.DatabaseProvider
import com.dashlane.device.DeviceInfoRepository
import com.dashlane.device.DeviceInfoUpdater
import com.dashlane.device.DeviceUpdateManager
import com.dashlane.device.component.DeviceInfoRepositoryComponent
import com.dashlane.endoflife.EndOfLifeObserver
import com.dashlane.events.AppEvents
import com.dashlane.featureflipping.FeatureFlipManager
import com.dashlane.followupnotification.FollowUpNotificationComponent
import com.dashlane.guidedonboarding.GuidedOnboardingComponent
import com.dashlane.hermes.LogFlush
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.inject.HermesComponent
import com.dashlane.inappbilling.BillingManager
import com.dashlane.inapplogin.InAppLoginManager
import com.dashlane.logger.AdjustWrapper
import com.dashlane.login.lock.LockManager
import com.dashlane.masterpassword.dagger.ChangeMasterPasswordComponent
import com.dashlane.navigation.Navigator
import com.dashlane.network.inject.RetrofitComponent
import com.dashlane.network.webservices.authentication.GetTokenService
import com.dashlane.notification.FcmHelper
import com.dashlane.notification.badge.NotificationBadgeActor
import com.dashlane.notification.model.TokenJsonProvider
import com.dashlane.passwordstrength.PasswordStrengthCache
import com.dashlane.passwordstrength.PasswordStrengthEvaluator
import com.dashlane.performancelogger.TimeToLoadLocalLogger
import com.dashlane.performancelogger.TimeToLoadRemoteLogger
import com.dashlane.permission.PermissionsManager
import com.dashlane.preference.DashlanePreferencesComponent
import com.dashlane.premium.current.dagger.CurrentPlanComponent
import com.dashlane.premium.offer.common.FormattedPremiumStatusManager
import com.dashlane.premium.offer.common.OffersLogger
import com.dashlane.security.SecurityHelper
import com.dashlane.security.identitydashboard.breach.BreachLoader
import com.dashlane.security.identitydashboard.password.AuthentifiantSecurityEvaluator
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionRestorer
import com.dashlane.session.SessionRetrieverComponent
import com.dashlane.session.observer.CryptographyMigrationObserver
import com.dashlane.session.repository.AccountStatusRepository
import com.dashlane.session.repository.LockRepository
import com.dashlane.session.repository.SessionCoroutineScopeRepository
import com.dashlane.session.repository.TeamspaceManagerRepository
import com.dashlane.session.repository.UserAccountInfoRepository
import com.dashlane.session.repository.UserCryptographyRepository
import com.dashlane.session.repository.UserDataRepository
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.sharing.SharingKeysHelperComponent
import com.dashlane.storage.DataStorageProvider
import com.dashlane.storage.securestorage.LocalKeyRepository
import com.dashlane.storage.securestorage.UserSecureStorageManager
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.injection.DataAccessComponent
import com.dashlane.teamspaces.db.TeamspaceForceCategorizationManager
import com.dashlane.ui.GlobalActivityLifecycleListener
import com.dashlane.ui.M2xIntentFactory
import com.dashlane.ui.ScreenshotPolicy
import com.dashlane.ui.component.EndOfLifeComponent
import com.dashlane.ui.component.UiPartComponent
import com.dashlane.ui.menu.MenuComponent
import com.dashlane.ui.premium.inappbilling.BillingVerification
import com.dashlane.ui.premium.inappbilling.service.StoreOffersCache
import com.dashlane.ui.screens.activities.onboarding.hardwareauth.HardwareAuthActivationActivity
import com.dashlane.ui.screens.fragments.SharingPolicyDataProvider
import com.dashlane.ui.screens.fragments.search.dagger.SearchComponent
import com.dashlane.ui.screens.settings.list.RootSettingsList
import com.dashlane.ui.util.DialogHelper
import com.dashlane.update.AppUpdateInstaller
import com.dashlane.url.icon.UrlDomainIconComponent
import com.dashlane.useractivity.AggregateUserActivity
import com.dashlane.useractivity.RacletteLogger
import com.dashlane.useractivity.log.UserActivityFlush
import com.dashlane.useractivity.log.inject.UserActivityComponent
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.usersupportreporter.UserSupportFileLogger
import com.dashlane.debug.DaDaDa
import com.dashlane.util.DarkThemeHelper
import com.dashlane.util.ThreadHelper
import com.dashlane.util.Toaster
import com.dashlane.util.clipboard.CopyComponent
import com.dashlane.util.clipboard.vault.VaultItemFieldContentService
import com.dashlane.util.hardwaresecurity.CryptoObjectHelper
import com.dashlane.util.inject.ApplicationComponent
import com.dashlane.util.log.UserSupportFileLoggerApplicationCreated
import com.dashlane.util.notification.NotificationHelper
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.vault.VaultReportLogger
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock



@InstallIn(SingletonComponent::class)
@EntryPoint
interface SingletonComponentProxy : ApplicationComponent, SessionRetrieverComponent,
    DeviceInfoRepositoryComponent, UiPartComponent, RetrofitComponent,
    DashlanePreferencesComponent, DataAccessComponent,
    SharingKeysHelperComponent, ChangeMasterPasswordComponent, AutofillApiComponent,
    AutofillApiViewAllAccountsComponent, AutofillApiPauseComponent,
    AutofillApiGeneratePasswordComponent, AutofillApiCreateAccountComponent,
    AutofillApiChangePasswordComponent, AutofillApiTotpComponent,
    AutofillApiRememberAccountComponent,
    AutofillApiChangePauseComponent, AutofillApiUnlinkAccountsComponent,
    MonitorAutofillIssuesComponent,
    GuidedOnboardingComponent, UserActivityComponent, CrashReporterComponent,
    UrlDomainIconComponent, MenuComponent, CryptographyComponent,
    SharingCryptographyComponent, HermesComponent, FollowUpNotificationComponent, CopyComponent,
    CurrentPlanComponent, SyncSingletonComponent,
    SyncSingletonDataStorageComponent, SearchComponent, EndOfLifeComponent {
    override val lockManager: LockManager
    override val permissionsManager: PermissionsManager
    override val screenshotPolicy: ScreenshotPolicy
    val globalActivityLifecycleListener: GlobalActivityLifecycleListener
    override val toaster: Toaster
    val dialogHelper: DialogHelper
    val notificationHelper: NotificationHelper
    val threadHelper: ThreadHelper
    override val crashReporter: CrashReporter
    val daDaDa: DaDaDa
    val userSupportFileLogger: UserSupportFileLogger
    val passwordStrengthEvaluator: PasswordStrengthEvaluator
    val passwordStrengthEvaluatorCache: PasswordStrengthCache
    val featureFlipManager: FeatureFlipManager
    override val userFeaturesChecker: UserFeaturesChecker
    val breachManager: BreachManager
    val breachLoader: BreachLoader
    val securityHelper: SecurityHelper
    val billingManager: BillingManager
    val billingVerificator: BillingVerification
    override val mainDataAccessor: MainDataAccessor
    val adjustWrapper: AdjustWrapper
    val brazeWrapper: BrazeWrapper
    val dataSync: DataSync
    val announcementCenter: AnnouncementCenter
    val accessibleOffersCache: StoreOffersCache
    val aggregateUserActivity: AggregateUserActivity
    val inAppLoginManager: InAppLoginManager
    val rootSettingsList: RootSettingsList
    val deviceUpdateManager: DeviceUpdateManager
    val secureDataStoreManager: UserSecureStorageManager
    val localKeyRepository: LocalKeyRepository
    val deviceInfoUpdater: DeviceInfoUpdater
    override val deviceInfoRepository: DeviceInfoRepository
    val tokenJsonProvider: TokenJsonProvider
    val fcmHelper: FcmHelper
    val userAccountStorage: UserAccountStorage
    val offlineExperimentsReporter: OfflineExperimentReporter
    val notificationBadgeActor: NotificationBadgeActor
    val cryptoObjectHelper: CryptoObjectHelper
    val authentifiantSecurityEvaluator: AuthentifiantSecurityEvaluator
    override val sessionManager: SessionManager
    val sessionRestorer: SessionRestorer
    val accountStatusRepository: AccountStatusRepository
    val teamspaceRepository: TeamspaceManagerRepository
    val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
    override val logRepository: LogRepository
    val sessionCoroutineScopeRepository: SessionCoroutineScopeRepository
    val userAccountInfoRepository: UserAccountInfoRepository
    override val userDataRepository: UserDataRepository
    override val userDatabaseRepository: UserDatabaseRepository
    val lockRepository: LockRepository
    val darkThemeHelper: DarkThemeHelper
    val userSupportFileLoggerApplicationCreated: UserSupportFileLoggerApplicationCreated
    val accountRecovery: AccountRecovery
    val tokenService: GetTokenService
    val appEvents: AppEvents
    val m2xIntentFactory: M2xIntentFactory
    val autofillUsageLog: IAutofillUsageLog
    override val userPreferencesAccess: IUserPreferencesAccess
    val sessionCredentialsSaver: SessionCredentialsSaver
    val appUpdateInstaller: AppUpdateInstaller
    val timeToLoadRemoteLogger: TimeToLoadRemoteLogger
    override val timeToLoadLocalLogger: TimeToLoadLocalLogger
    val userActivityFlush: UserActivityFlush
    val logFlush: LogFlush
    override val navigator: Navigator
    val offersLogger: OffersLogger
    val userCryptographyRepository: UserCryptographyRepository
    val cryptographyMigrationObserver: CryptographyMigrationObserver
    val teamspaceForceCategorizationManager: TeamspaceForceCategorizationManager
    val databaseProvider: DatabaseProvider
    override val dataStorageProvider: DataStorageProvider
    val sharingItemUpdater: SharingItemUpdater
    override val dataIdentifierSharingXmlConverter: DataIdentifierSharingXmlConverter
    val vaultReportLogger: VaultReportLogger
    val clock: Clock
    override val endOfLife: EndOfLifeObserver
    val premiumStatusManager: FormattedPremiumStatusManager
    val vaultItemFieldContentService: VaultItemFieldContentService
    val authenticatorAppConnection: AuthenticatorAppConnection
    val sharingPolicyDataProvider: SharingPolicyDataProvider
    val appMetaDataToLinkedAppsMigration: AppMetaDataToLinkedAppsMigration
    val rememberToLinkedAppsMigration: RememberToLinkedAppsMigration
    val linkedServicesHelper: LinkedServicesHelper
    override val racletteLogger: RacletteLogger
    fun inject(dataSync: DataSync)
    fun inject(activity: HardwareAuthActivationActivity)
}
