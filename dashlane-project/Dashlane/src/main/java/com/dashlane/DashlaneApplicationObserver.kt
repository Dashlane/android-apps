package com.dashlane

import android.app.Application
import android.app.backup.BackupManager
import android.content.Context
import com.dashlane.abtesting.OfflineExperimentReporter
import com.dashlane.account.UserAccountStorage
import com.dashlane.announcements.AnnouncementCenter
import com.dashlane.async.BroadcastManager
import com.dashlane.authenticator.AuthenticatorAppConnection
import com.dashlane.autofill.api.linkedservices.AppMetaDataToLinkedAppsMigration
import com.dashlane.autofill.api.linkedservices.RememberToLinkedAppsMigration
import com.dashlane.braze.BrazeWrapper
import com.dashlane.breach.BreachManager
import com.dashlane.callbacks.AdjustActivityCallback
import com.dashlane.core.DataSync
import com.dashlane.core.DataSyncNotification
import com.dashlane.crashreport.CrashReporter
import com.dashlane.debug.DaDaDa
import com.dashlane.debug.DeveloperUtilities.systemIsInDebug
import com.dashlane.device.DeviceInfoUpdater
import com.dashlane.device.DeviceUpdateManager
import com.dashlane.events.AppEvents
import com.dashlane.hermes.LogRepository
import com.dashlane.inappbilling.BillingManager
import com.dashlane.logger.AdjustWrapper
import com.dashlane.logger.utils.LogsSender
import com.dashlane.notification.FcmHelper
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.server.api.DashlaneApi
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionRestorer
import com.dashlane.session.observer.AnnouncementCenterObserver
import com.dashlane.session.observer.BackupTokenObserver
import com.dashlane.session.observer.BroadcastManagerObserver
import com.dashlane.session.observer.CryptographyMigrationObserver
import com.dashlane.session.observer.DeviceUpdateManagerObserver
import com.dashlane.session.observer.FcmRegistrationObserver
import com.dashlane.session.observer.LinkedAppsMigrationObserver
import com.dashlane.session.observer.LogsObserver
import com.dashlane.session.observer.OfflineExperimentObserver
import com.dashlane.session.observer.SystemUpdateObserver
import com.dashlane.session.observer.UserSettingsLogObserver
import com.dashlane.session.repository.AccountStatusRepository
import com.dashlane.session.repository.LockRepository
import com.dashlane.session.repository.SessionCoroutineScopeRepository
import com.dashlane.session.repository.TeamspaceManagerRepository
import com.dashlane.session.repository.UserAccountInfoRepository
import com.dashlane.session.repository.UserCryptographyRepository
import com.dashlane.session.startRestoreSession
import com.dashlane.ui.GlobalActivityLifecycleListener
import com.dashlane.ui.endoflife.EndOfLife
import com.dashlane.update.AppUpdateInstaller
import com.dashlane.useractivity.AggregateUserActivity
import com.dashlane.util.DarkThemeHelper
import com.dashlane.util.ThreadHelper
import com.dashlane.util.log.UserSupportFileLoggerApplicationCreated
import com.dashlane.util.notification.NotificationHelper
import com.dashlane.util.strictmode.StrictModeUtil.init
import com.dashlane.vault.VaultReportLogger
import com.google.firebase.FirebaseApp
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface ApplicationObserver {
    fun onCreate(application: DashlaneApplication)
    fun onTerminate(application: DashlaneApplication)
}

class DashlaneApplicationObserver @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager,
    private val userCryptographyRepository: UserCryptographyRepository,
    private val sessionCoroutineScopeRepository: SessionCoroutineScopeRepository,
    private val userAccountInfoRepository: UserAccountInfoRepository,
    private val accountStatusRepository: AccountStatusRepository,
    private val lockRepository: LockRepository,
    private val teamspaceManagerRepository: TeamspaceManagerRepository,
    private val dataSync: DataSync,
    private val offlineExperimentReporter: OfflineExperimentReporter,
    private val aggregateUserActivity: AggregateUserActivity,
    private val announcementCenter: AnnouncementCenter,
    private val deviceUpdateManager: DeviceUpdateManager,
    private val fcmHelper: FcmHelper,
    private val dashlaneApi: DashlaneApi,
    private val userAccountStorage: UserAccountStorage,
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val appUpdateInstaller: AppUpdateInstaller,
    private val userPreferencesManager: UserPreferencesManager,
    private val cryptographyMigrationObserver: CryptographyMigrationObserver,
    private val endOfLife: EndOfLife,
    private val appMetaDataToLinkedAppsMigration: AppMetaDataToLinkedAppsMigration,
    private val rememberToLinkedAppsMigration: RememberToLinkedAppsMigration,
    private val logsSender: LogsSender,
    private val deviceInfoUpdater: DeviceInfoUpdater,
    private val threadHelper: ThreadHelper,
    private val daDaDa: DaDaDa,
    private val globalActivityLifecycleListener: GlobalActivityLifecycleListener,
    private val billingManager: BillingManager,
    private val notificationHelper: NotificationHelper,
    private val brazeWrapper: BrazeWrapper,
    private val darkThemeHelper: DarkThemeHelper,
    private val userSupportFileLoggerApplicationCreated: UserSupportFileLoggerApplicationCreated,
    private val vaultReportLogger: VaultReportLogger,
    private val authenticatorAppConnection: AuthenticatorAppConnection,
    private val dataSyncNotification: DataSyncNotification,
    private val appEvents: AppEvents,
    private val sessionRestorer: SessionRestorer,
    private val crashReporter: CrashReporter,
    private val adjustWrapper: AdjustWrapper,
    private val breachManager: BreachManager,
    private val logRepository: LogRepository
) : ApplicationObserver {
    override fun onCreate(application: DashlaneApplication) {
        init()
        FirebaseApp.initializeApp(application)
        deviceInfoUpdater.refreshIfNeeded()
        threadHelper.init()
        daDaDa.listen(application)
        globalActivityLifecycleListener.register(application)
        announcementCenter.start(application)
        if (!systemIsInDebug(application.applicationContext)) {
            application.registerActivityLifecycleCallbacks(AdjustActivityCallback())
        }
        initialize(application)
        billingManager.connect()
        notificationHelper.initChannels()
        brazeWrapper.configureBrazeNotificationFactory()
        darkThemeHelper.onApplicationCreate()
        userSupportFileLoggerApplicationCreated.onApplicationCreated(application)
        vaultReportLogger.start()
        authenticatorAppConnection.loadOtpsForBackup()
    }

    override fun onTerminate(application: DashlaneApplication) {
        sessionManager.detachAll()
        dataSyncNotification.hideSyncNotification()
        appEvents.unregisterAll()
        globalActivityLifecycleListener.unregister(application)
        announcementCenter.stop(application)
        billingManager.disconnect()
        vaultReportLogger.stop()
        breachManager.onTerminate()
    }

    private fun initialize(application: Application) {
        setupSessionObservers()
        BroadcastManager.removeAllBufferedIntent()

        
        
        sessionRestorer
            .startRestoreSession(globalPreferencesManager.getDefaultUsername())
        setupMarketingStuff()

        
        crashReporter.init(application)
    }

    private fun setupMarketingStuff() {
        if (systemIsInDebug(context)) {
            return
        }
        adjustWrapper.initIfNeeded(context)
    }

    private fun setupSessionObservers() {
        sessionManager.attach(sessionCoroutineScopeRepository)
        sessionManager.attach(lockRepository)
        sessionManager.attach(LogsObserver(aggregateUserActivity, logsSender))
        sessionManager.attach(teamspaceManagerRepository)
        sessionManager.attach(AnnouncementCenterObserver(announcementCenter))
        sessionManager.attach(accountStatusRepository)
        sessionManager.attach(userAccountInfoRepository)
        sessionManager.attach(cryptographyMigrationObserver)
        sessionManager.attach(BroadcastManagerObserver())
        sessionManager.attach(OfflineExperimentObserver(offlineExperimentReporter))
        sessionManager.attach(DeviceUpdateManagerObserver(deviceUpdateManager))
        sessionManager.attach(FcmRegistrationObserver(fcmHelper))
        sessionManager.attach(appUpdateInstaller)
        sessionManager.attach(
            BackupTokenObserver(
                dashlaneApi,
                userAccountStorage,
                globalPreferencesManager,
                BackupManager(context),
                userCryptographyRepository
            )
        )
        sessionManager.attach(SystemUpdateObserver(userPreferencesManager))
        sessionManager.attach(UserSettingsLogObserver(logRepository))
        sessionManager.attach(endOfLife)
        sessionManager.attach(
            LinkedAppsMigrationObserver(
                appMetaDataToLinkedAppsMigration,
                rememberToLinkedAppsMigration
            )
        )
        sessionManager.attach(dataSync)
    }
}